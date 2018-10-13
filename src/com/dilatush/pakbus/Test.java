package com.dilatush.pakbus;

import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.comms.SimpleContext;
import com.dilatush.pakbus.messages.*;

import java.nio.ByteBuffer;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Test {


    public static void main( String[] _args ) {

        // test SerPkt protocol ring packet...
        Context cx = new SimpleContext( new Address(4010), new Address( 1029 ), 1, 1, HopCount.ZERO, 0 );
        test( new RingMsg( cx ) );
        test( new ClockNotificationMsg( NSec.now(), cx ) );

        // test get settings request...
        ByteBuffer bb = getBytes( "BD AF FF 7F FE 0F FF 0F FE 07 07 50 61 6B 42 75 73 41 64 64 72 65 73 73 00 FA 01 BD" );
        Packet packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        Message oldmsg = MessageFactory.from( packet );
        if( oldmsg == null ) throw new IllegalStateException(  );
        if( !"PakBusAddress".equals( oldmsg.at( "NameList" ).getAsString() ) ) throw new IllegalStateException(  );

        // test clock transaction request...
        bb = getBytes( "BD A0 01 4F FE 10 01 0F FE 17 17 00 00 00 00 00 00 00 00 00 00 B2 B3 BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        oldmsg = MessageFactory.from( packet );
        if( oldmsg == null ) throw new IllegalStateException(  );
        int sc = oldmsg.at( "SecurityCode" ).getAsInt();
        int sec = oldmsg.at( "Adjustment.Seconds" ).getAsInt();
        int ns  = oldmsg.at( "Adjustment.Nanoseconds" ).getAsInt();
        if( sc + sec + ns != 0 ) throw new IllegalStateException(  );

        // test clock transaction response...
        bb = getBytes( "BD BD AF FE 00 01 1F FE 00 01 97 17 00 1B FA 2A 61 C8 00 00 00 04 FA BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        oldmsg = MessageFactory.from( packet );
        if( oldmsg == null ) throw new IllegalStateException(  );
        int nss = oldmsg.at( "OldTime.Seconds" ).getAsInt();
        int nsn = oldmsg.at( "OldTime.Nanoseconds" ).getAsInt();

        // test clock transaction response with permission denied...
        bb = getBytes( "BD BD AF FE 00 01 1F FE 00 01 97 17 01 50 6F BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        oldmsg = MessageFactory.from( packet );
        if( oldmsg == null ) throw new IllegalStateException(  );
        if( oldmsg.at( "OldTime" ).isSet() ) throw new IllegalStateException(  );

        // test get values request...
        bb = getBytes( "BD BD BD A0 01 40 04 10 01 00 04 1A 1A 00 00 50 75 62 6C 69 63 00 09 52 48 00 00 01 2A F9 BD BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        oldmsg = MessageFactory.from( packet );
        if( oldmsg == null ) throw new IllegalStateException(  );
        String tn = oldmsg.at( "TableName" ).getAsString();
        String fn = oldmsg.at( "FieldName" ).getAsString();
        int tc = oldmsg.at( "TypeCode" ).getAsInt();
        int sw = oldmsg.at( "Swath" ).getAsInt();

        // test get values response...
        bb = getBytes( "BD BD BD A0 04 00 01 10 04 00 01 9A 1A 00 42 34 1C 29 69 2A BD BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        oldmsg = MessageFactory.from( packet );
        if( oldmsg == null ) throw new IllegalStateException(  );
        bb = oldmsg.at( "Values" ).getAsByteBuffer();

        // test file upload request...
        bb = getBytes( "BD BD BD A0 01 70 04 10 01 00 04 1D 1D 00 00 43 50 55 3A 44 65 66 2E 74 64 66 00 00 00 00 00 00 00 80 27 EA BD BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        oldmsg = MessageFactory.from( packet );
        if( oldmsg == null ) throw new IllegalStateException(  );
        fn = oldmsg.at( "FileName" ).getAsString();
        int cf = oldmsg.at( "CloseFlag" ).getAsInt();
        int fo = oldmsg.at( "FileOffset" ).getAsInt();
        sw = oldmsg.at( "Swath" ).getAsInt();

        // test file upload response...
        bb = getBytes( "BD BD BD A0 04 00 01 10 04 00 01 9D 1D 00 00 00 00 00 01 53 74 61 74 75 73 00 00 00 00 01 0C " +
                "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8B 4F 53 76 65 72 73 69 6F 6E 00 00 00 00 00 00 00 00 01 00 00 " +
                "00 08 00 00 00 08 00 00 00 00 8B 4F 53 44 61 74 65 00 00 00 00 00 00 00 00 01 00 00 00 0A 00 00 00 0A 00 00 00 " +
                "00 8B 50 72 6F 67 4E 61 6D 65 00 00 00 00 00 00 00 00 01 00 00 00 10 00 00 00 10 00 00 00 00 95 50 72 6F 67 53 " +
                "69 67 00 00 F1 67 BD BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        oldmsg = MessageFactory.from( packet );
        if( oldmsg == null ) throw new IllegalStateException(  );
        bb = oldmsg.at( "FileData" ).getAsByteBuffer();

        // test collect data request...
        bb = getBytes( "BD BD BD A0 01 70 04 10 01 00 04 09 09 00 00 05 00 03 43 15 00 00 00 3C 00 00 C7 DF BD BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        oldmsg = MessageFactory.from( packet );
        if( oldmsg == null ) throw new IllegalStateException(  );
        int cm = oldmsg.at( "CollectMode" ).getAsInt();
        int tb = oldmsg.at( "Specs.TableNbr", 0 ).getAsInt();
        int p1 = oldmsg.at( "Specs.P1", 0 ).getAsInt();

        // test making a collect data request by setting properties...
        oldmsg = new CollectDataReq( 9 );
        oldmsg.at( "SecurityCode" ).setTo( 0 );
        oldmsg.at( "CollectMode" ).setTo( 5 );
        oldmsg.phase();
        oldmsg.arrayAt( "Specs" ).add();
        oldmsg.at( "Specs.TableNbr", 0 ).setTo( 3 );
        oldmsg.at( "Specs.TableDefSig", 0 ).setTo( 0x4315 );
        oldmsg.at( "Specs.P1", 0 ).setTo( 60 );
        oldmsg.finish();
        bb = oldmsg.getAsByteBuffer();

        bb.hashCode();
    }


    private static void test( final Msg _msg ) {

        // first we encode the message...
        Packet p1 = _msg.encode();
        ByteBuffer b1 = p1.encode();

        // now we decode it...
        Packet p2 = Packet.decode( b1, 0, b1.limit() );

        // then we encode the decoded packet...
        ByteBuffer b2 = p2.encode();

        // now we see if the two encoded packets are equivalent...
        if( !b1.equals( b2 ) )
            throw new IllegalStateException( "Message mismatch!" );
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
