package com.dilatush.pakbus.app;

import com.dilatush.pakbus.*;
import com.dilatush.pakbus.comms.*;
import com.dilatush.pakbus.messages.Msg;
import com.dilatush.pakbus.messages.MsgFactory;
import com.dilatush.pakbus.messages.pakctrl.ClockNotificationMsg;
import com.dilatush.pakbus.messages.serpkt.RingMsg;
import com.dilatush.pakbus.shims.DataQuery;
import com.dilatush.pakbus.shims.TableDefinition;
import com.dilatush.pakbus.shims.TableDefinitions;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.Datum;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * Instances of this class implement a PakBus application.  Instances of this class are mutable and stateful, but are nevertheless threadsafe through
 * synchronization.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Application {

    final public Node                    address;

    final private PacketTransceiver      transceiver;
    final private Map<String,Datalogger> loggersByName;
    final private Map<Node,Datalogger>   loggersByAddress;
    final private PacketReader           reader;
    final private Context                broadcastContext;


    /**
     * Creates a new instance of this class with the give packet transceiver, PakBus address, and PakBus node ID.
     *
     * @param _transceiver the packet transceiver for this instance to use for PakBus communications
     * @param _address the PakBus address and node ID of this application
     */
    public Application( final PacketTransceiver _transceiver, final Node _address ) {

        transceiver      = _transceiver;
        address          = _address;
        loggersByName    = new HashMap<>();
        loggersByAddress = new HashMap<>();
        reader           = new PacketReader();
        broadcastContext = new SimpleContext( this, null, 0  );
    }


    /**
     * Registers the given datalogger with this application.
     *
     * @param _datalogger the datalogger to register
     */
    public synchronized void register( final Datalogger _datalogger ) {

        Checks.required( _datalogger );

        loggersByAddress.put( _datalogger.address, _datalogger );
        loggersByName.put( _datalogger.name, _datalogger );
    }


    /**
     * Sends the given message, using the application's packet transceiver.
     *
     * @param _msg the message to send
     */
    public void send( final Msg _msg ) {

        Checks.required( _msg );

        Packet packet = _msg.encode();
        boolean sent = transceiver.tx( new RawPacket( packet.encode() ) );

        if( !sent )
            throw new IllegalStateException( "Cannot send packet" );
    }


    /**
     * Broadcasts a ring to the PakBus.
     */
    public void broadcastRing() {
        Msg msg = new RingMsg( broadcastContext );
        Packet packet = msg.encode();
        transceiver.tx( new RawPacket( packet.encode() ) );
    }


    /**
     * Broadcasts the current time to set all dataloggers.
     */
    public void setClock() {

        NSec now = NSec.now();
        now = now.add( new NSec( 268239600, 0) );
        Msg msg = new ClockNotificationMsg( NSec.now(), broadcastContext );
        Packet packet = msg.encode();
        transceiver.tx( new RawPacket( packet.encode() ) );
    }


    /**
     * Handle the receipt of a message broadcast to all PakBus destinations.
     *
     * @param _msg the broadcast message
     */
    private void broadcastHandler( final Msg _msg ) {
        Log.logLn( "Received broadcast message from " + _msg.getPacket().getSrcAddr() );
    }


    /**
     * Handle the receipt of a message that is not a broadcast, and is not addressed to this app.
     *
     * @param _msg the alien message
     */
    private void alienHandler( final Msg _msg ) {
        Log.logLn( "Received alien packet to " + _msg.getPacket().getDstAddr() + " from " + _msg.getPacket().getSrcAddr() );
    }


    /**
     * Handle the receipt of a message that is addressed to this app, but is from a source that this app doesn't know about.
     *
     * @param _msg the orphan message
     */
    private void orphanHandler( final Msg _msg ) {
        Log.logLn( "Received orphan packet from " + _msg.getPacket().getSrcAddr() );
    }


    /**
     * Return the logger with the given address that is registered to this app, or null if there is none.
     *
     * @param _address the address of the logger to get
     * @return the registered logger, or null if there is none
     */
    private synchronized Datalogger getLogger( final Node _address ) {
        return loggersByAddress.get( _address );
    }


    /**
     * Reads and dispatches packets.
     */
    private class PacketReader extends Thread {

        private PacketReader() {
            setName( "Application.PacketReader" );
            setDaemon( true );
            start();
        }


        @Override
        public void run() {

            // loop here forever...
            // noinspection InfiniteLoopStatement
            while( true ) {

                try {

                    // get a packet...
                    RawPacket rawPacket = transceiver.rx();

                    // if we didn't get anything, that means we timed out - tickle the loggers...
                    if( rawPacket == null ) {
                        synchronized( Application.this ) {
                            for( final Datalogger datalogger : loggersByName.values() ) {
                                datalogger.tickle();
                            }
                        }
                        continue;
                    }

                    // we got data - decode it...
                    Packet packet = Packet.decode( rawPacket );

                    // see if we have a logger for the source...
                    Datalogger logger = getLogger( packet.getSrcAddr() );

                    // decode the message...
                    Context cx = (logger == null) ? null : logger.context;
                    Msg msg = MsgFactory.from( packet, cx );

                    // if it's a broadcast, dispatch to the broadcast handler...
                    if( packet.getDstPhysAddr().isBroadcast() )
                        broadcastHandler( msg );

                    // if it's not addressed to us, dispatch to the alien handler...
                    else if( !packet.getDstAddr().equals( address ) )
                        alienHandler( msg );

                    // if we don't have a datalogger matching the source, dispatch it to the orphan handler...
                    else if( logger == null )
                        orphanHandler( msg );

                    // if we get here, we have a packet addressed directly to us, from a datalogger we know about...
                    else {

                        // dispatch the packet to that datalogger...
                        logger.handle( msg );
                    }
                }

                catch( InterruptedException _e ) {

                    // if we get interrupted, just end it all...
                    break;
                }

                catch( Exception _e ) {

                    // anything else goes wrong, we just log it and keep on trucking...
                    Log.logLn( "Unexpected exception: " + _e );
                }
            }
        }
    }


    static public void main( String[] _args ) throws InterruptedException {

        // some setup...
        SerialTransceiver st = PortSerialTransceiver
                .getSerialTransceiver( "/dev/cu.usbserial-AC01R521", 9600, 8,
                        PortSerialTransceiver.STOP_BITS_ONE, PortSerialTransceiver.PARITY_NONE, PortSerialTransceiver.FLOW_CTRL_NONE );
        PacketTransceiver pt = new SerialPacketTransceiver( st );
        Node appAddr = new Node( new Address( 4010 ), 1 );

        // get our app...
        Application app = new Application( pt, appAddr );

        // now get our WeatherHawk datalogger...
        Node loggerAddr = new Node( new Address( 1027 ), 1027 );
        Datalogger logger = new Datalogger( app, "WeatherHawk 621", loggerAddr );
        app.register( logger );


        // calibrate the clock...
        logger.calibrateClock();

        // get the time...
        Instant time = logger.getTime();
        Duration error = Duration.between( Instant.now(), time );

        // get the settings...
        Map<String,String> settings = logger.getAllSettings();
        List<String> settingNames = new ArrayList<>();

        // collect most recent record from data2...
        TableDefinitions defs = logger.getTableDefinitions();
        TableDefinition pub = defs.getTableDef( "Public" );
        DataQuery query = new DataQuery( pub.index, pub.signature );

        while( true ) {
            Datum datum = logger.collectMostRecent( query, 1 );
            if( datum == null ) break;

            pp( "AirTemp_C", datum );
            pp( "RH", datum );
            pp( "WindSpeed_ms", datum );
            pp( "WindDirect_deg", datum );
            pp( "Barometer_KPa", datum );
            pp( "Solar", datum );
            pp( "BatVolt_V", datum );
            Log.logLn( "--------------------------" );

            sleep(9000 );
        }

        st.hashCode();
    }


    private static void pp( final String _name, final Datum _datum ) {

        Datum value = _datum.at( _name, 0 );
        Log.logLn( _name + ": " + value.getAsDouble() );
    }
}
