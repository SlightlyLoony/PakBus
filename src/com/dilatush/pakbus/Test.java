package com.dilatush.pakbus;

import com.dilatush.pakbus.comms.*;
import com.dilatush.pakbus.messages.*;
import com.dilatush.pakbus.messages.bmp5.*;
import com.dilatush.pakbus.messages.pakctrl.*;
import com.dilatush.pakbus.messages.serpkt.ReadyMsg;
import com.dilatush.pakbus.messages.serpkt.RingMsg;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Test {


    public static void main( String[] _args ) throws InterruptedException {

        // some setup...

        SerialTransceiver st = PortSerialTransceiver
                .getSerialTransceiver( "/dev/cu.usbserial-AC01R521", 9600, 8,
                        PortSerialTransceiver.STOP_BITS_ONE, PortSerialTransceiver.PARITY_NONE, PortSerialTransceiver.FLOW_CTRL_NONE );

        PacketTransceiver pt = new SerialPacketTransceiver( st );

        Context cx = new SimpleContext( new Address(4010), new Address( 1027 ), 1, 1027, HopCount.ZERO, 0 );
        Context bcx = new SimpleContext( new Address(4010), Address.BROADCAST, 1, 1, HopCount.ZERO, 0 );

        Msg msg = request( pt, new RingMsg( bcx ), ReadyMsg.class );

        oneWay( pt, new ResetRouterMsg( 0, cx ) );

        msg = request( pt, new ClockReqMsg( NSec.ZERO, cx ), ClockRspMsg.class );
        Instant i = ((ClockRspMsg)msg).oldTime.asInstant();
        Duration d = Duration.between( Instant.now(), i );
        Log.logLn( "Clock difference: " + d.toMillis() + " milliseconds" );

        msg = request( pt, new GetStringSettingsReqMsg( "", cx ), GetStringSettingsRspMsg.class );

        msg = request( pt, new HelloReqMsg( 0, 10, 60, cx ), HelloRspMsg.class );

        msg = request( pt, new GetProgrammingStatisticsReqMsg( 0, cx ), GetProgrammingStatisticsRspMsg.class );

        ByteBuffer data = readFile( "SiteVal.TDF", pt, cx );

        msg = request( pt, new FileControlReqMsg( 0, "argyle.dac", 18, "awfully.cad", cx ), FileControlRspMsg.class );

        st.hashCode();
    }


    private static ByteBuffer readFile( final String _fileName, final PacketTransceiver _packetTransceiver, final Context _context ) throws InterruptedException {

        boolean done = false;
        int swath = 400;
        ByteBuffer result = ByteBuffer.allocate( 1000 );
        int offset = 0;
        while( !done ) {

            if( result.remaining() < (swath << 1)  ) {
                ByteBuffer bb = ByteBuffer.allocate( result.capacity() + 1000 );
                result.flip();
                bb.put( result );
                result = bb;
            }

            Msg msg = request( _packetTransceiver, new FileReceiveReqMsg( 0, _fileName, 0, offset, swath, _context ), FileReceiveRspMsg.class );
            FileReceiveRspMsg rmsg = (FileReceiveRspMsg) msg;
            offset += rmsg.fileData.limit();
            result.put( rmsg.fileData );
            if( rmsg.fileData.limit() < swath ) {
                done = true;
            }
        }
        result.flip();
        return result;
    }


    private static void oneWay( final PacketTransceiver _packetTransceiver, final Msg _msg ) {
        Packet packet = _msg.encode();
        _packetTransceiver.tx( new RawPacket( packet.encode() ) );
    }


    private static Msg request( final PacketTransceiver _packetTransceiver, final Msg _msg, final Class _expRsp ) throws InterruptedException {
        Packet packet = _msg.encode();
        _packetTransceiver.tx( new RawPacket( packet.encode() ) );
        packet = Packet.decode( _packetTransceiver.rx() );
        Msg msg = packet.getMsg();
        if( !_expRsp.isInstance( msg ) )
            throw new IllegalStateException( "Message returned is unexpected type" );
        return msg;
    }


    private static ByteBuffer getBytes( final String _s ) {

        String parts[] = _s.split( " " );
        ByteBuffer result = ByteBuffer.allocate( parts.length );
        for( String part : parts ) {
            result.put( (byte) Integer.parseInt( part, 16 ) );
        }
        result.flip();
        return result;
    }
}
