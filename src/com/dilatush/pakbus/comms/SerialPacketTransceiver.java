package com.dilatush.pakbus.comms;

import com.dilatush.pakbus.util.Checks;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Instances of this class are packet transceivers that send and receive raw packets through a serial transceiver, handling framing, deframing,
 * quoting, dequoting, signature verification, and signature generation.  This class relies on the transmit queue in the serial transceiver that it is
 * dependent on and maintains no transmit queue itself.  It does maintain a small queue of received packets.  This class creates and manages a daemon
 * reader thread.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class SerialPacketTransceiver implements PacketTransceiver {


    final static private Logger LOGGER = Logger.getLogger( SerialPacketTransceiver.class.getSimpleName() );

    final static private Duration LONG_SYNC_THRESHOLD = Duration.ofSeconds( 15 );  // time since last tx when we use a long sync instead of short...
    final static private int      SHORT_SYNC          = 3;  // number of sync bytes when we're using a short sync...
    final static private int      LONG_SYNC           = 25;  // number of sync bytes when we're using a long sync...
    final static private byte     SYNC                = (byte) 0xBD;
    final static private byte     QUOTE               = (byte) 0xBC;
    final static private int      QUOTE_OFFSET        = 0x20;
    final static private int      RX_QUEUE_SIZE       = 10;
    final static private int      MAX_PACKET_BYTES    = 1000 + 8 + 2;
    final static private int      MIN_PACKET_BYTES    = 4 + 2;


    final private SerialTransceiver transceiver;
    final private Reader            reader;

    private boolean stop;


    /**
     * Creates a new instance of this class using the given serial transceiver.
     *
     * @param _transceiver the serial transceiver this instance will use
     */
    public SerialPacketTransceiver( final SerialTransceiver _transceiver ) {

        // sanity check...
        Checks.required( _transceiver );

        // some setup...
        transceiver = _transceiver;
        reader = new Reader();
        stop =false;
    }



    /**
     * Adds the given packet to the ordered queue of packets to be transmitted from this packet transceiver, returning true if the packet was
     * successfully added.  A false return indicates that the transmission queue is full and the packet could not be added.
     *
     * @param _packet the packet to be added to the transmission queue
     * @return true if the packet was successfully added
     */
    @Override
    public boolean tx( final RawPacket _packet ) {

        // first we transmit the sync preamble...
        if( !txSync( (LONG_SYNC_THRESHOLD.compareTo( transceiver.sinceTx() ) < 0) ? LONG_SYNC : SHORT_SYNC ) )
            return false;

        // now we transmit the bytes in the packet, quoting as we go...
        for( int i = 0; i < _packet.packetBytes.limit(); i++ ) {

            // get the next byte and send it, quoted or not...
            byte b = _packet.packetBytes.get( i );
            if( (b == SYNC) || (b == QUOTE) ) {
                if( !transceiver.tx( QUOTE ) )
                    return false;
                if( !transceiver.tx( (byte)(b + QUOTE_OFFSET) ) )
                    return false;
            }
            else {
                if( !transceiver.tx( b ) )
                    return false;
            }
        }

        // then the postamble...
        return txSync( SHORT_SYNC );
    }


    /**
     * Transmits the given number of sync bytes, returning true if successful (meaning that the serial transceiver accepted them).
     *
     * @param _count the number of sync bytes to transmit
     * @return true if the sync bytes were successfully transmitted
     */
    private boolean txSync( final int _count ) {
        int remaining = _count;
        while( remaining > 0 ) {
            if( !transceiver.tx( SYNC ) )
                return false;
            remaining--;
        }
        return true;
    }


    /**
     * Returns the next packet received from this packet transceiver, blocking for up to one second until it becomes available.
     *
     * @return the next packet received, or null if none were received
     * @throws InterruptedException if interrupted while blocked
     */
    @Override
    public RawPacket rx() throws InterruptedException {
        return reader.queue.pollFirst( 1, TimeUnit.SECONDS  );
    }


    /**
     * Returns true if this instance has been stopped (because of a serial port error).
     *
     * @return true if this instance has been stopped
     */
    public boolean isStopped() {
        return stop;
    }


    /**
     * Stops this instance when an error occurs on the port.
     */
    private void stop() {
        stop = true;
    }


    private class Reader extends Thread {


        final private LinkedBlockingDeque<RawPacket> queue;


        private Reader() {
            setName( "SerialPacketTransceiver.Reader" );
            setDaemon( true );
            start();
            queue = new LinkedBlockingDeque<>( RX_QUEUE_SIZE );
        }


        public void run() {

            while( !stop ) {

                try {

                    byte b = waitForInPacket();   // read bytes until we get past any preamble...

                    // make a place to hold our packet's bytes...
                    ByteBuffer packetBytes = ByteBuffer.allocate( MAX_PACKET_BYTES );

                    // loop until we get our entire packet...
                    while( true ) {

                        // if we got a sync, we've got the whole packet...
                        if( SYNC == b ) {

                            // now let's see if we've got a valid packet...
                            packetBytes.flip();

                            // if it's too short to be a real packet, ignore this monstrosity...
                            if( packetBytes.limit() < MIN_PACKET_BYTES ) {
                                LOGGER.finest( "Packet too short, ignoring packet" );
                                break;
                            }

                            // get the signature...
                            packetBytes.limit( packetBytes.limit() - 2 );
                            Signature signature = new Signature( packetBytes );
                            packetBytes.limit( packetBytes.limit() + 2 );

                            // if the nullifier doesn't match, ignore this grotesque monstrosity...
                            if( ((short)signature.getNullifier()) != packetBytes.getShort( packetBytes.limit() - 2 ) ) {
                                LOGGER.finest( "Bad nullifier, ignoring packet" );
                                break;
                            }

                            // we have a good packet, so queue it up...
                            RawPacket rawPacket = new RawPacket( packetBytes, signature );
                            LOGGER.finest( "Received good packet" );
                            queue.putFirst( rawPacket );

                            break;
                        }

                        // if we're out of room, we've got to end this...
                        if( packetBytes.position() == MAX_PACKET_BYTES ) {
                            LOGGER.finest( "Oversized packet, ignoring packet" );
                            waitForOutPacket();  // read bytes until we got a sync, so we're done with this packet...
                            break;
                        }

                        // getting here means we've got a byte that belongs to the packet - but it might be a quote...
                        if( QUOTE == b ) {

                            // yup, it was, so get another byte and dequote it...
                            b = (byte)(transceiver.rx() - QUOTE_OFFSET);
                        }

                        // time to add this to our packet's bytes...
                        packetBytes.put( b );

                        // and get another one...
                        b = transceiver.rx();
                    }
                }
                catch( InterruptedException _e ) {
                    SerialPacketTransceiver.this.stop();
                }
            }
        }


        /**
         * Returns the first non-sync byte read from the serial transceiver.
         *
         * @return the first non-sync byte read from the serial transceiver
         */
        private byte waitForInPacket() throws InterruptedException {
            byte result = SYNC;
            while( result == SYNC )
                result = transceiver.rx();
            return result;
        }


        /**
         * Reads bytes from the serial transceiver until it receives a sync.
         */
        private void waitForOutPacket() throws InterruptedException {
            byte result = 0;
            while( result != SYNC )
                result = transceiver.rx();
        }
    }
}
