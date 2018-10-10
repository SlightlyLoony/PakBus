package com.dilatush.pakbus;

import com.dilatush.pakbus.messages.CollectDataReq;
import com.dilatush.pakbus.messages.Message;
import com.dilatush.pakbus.messages.MessageFactory;

import java.nio.ByteBuffer;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Test {


    public static void main( String[] _args ) {

        // test SerPkt protocol ring packet...
        PacketDatum pd = new PacketDatum();
        pd.at( "LinkState"   ).setTo( 9     );
        pd.at( "DstPhyAddr"  ).setTo( 0x123 );
        pd.at( "ExpMoreCode" ).setTo( 0     );
        pd.at( "Priority"    ).setTo( 1     );
        pd.at( "SrcPhyAddr"  ).setTo( 4010  );
        pd.finish();
        Packet packet = new Packet( pd );
        ByteBuffer ep = packet.encode();

        // test get settings request...
        ByteBuffer bb = getBytes( "BD AF FF 7F FE 0F FF 0F FE 07 07 50 61 6B 42 75 73 41 64 64 72 65 73 73 00 FA 01 BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        Message msg = MessageFactory.from( packet );
        if( msg == null ) throw new IllegalStateException(  );
        if( !"PakBusAddress".equals( msg.at( "NameList" ).getAsString() ) ) throw new IllegalStateException(  );

        // test clock transaction request...
        bb = getBytes( "BD A0 01 4F FE 10 01 0F FE 17 17 00 00 00 00 00 00 00 00 00 00 B2 B3 BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        msg = MessageFactory.from( packet );
        if( msg == null ) throw new IllegalStateException(  );
        int sc = msg.at( "SecurityCode" ).getAsInt();
        int sec = msg.at( "Adjustment.Seconds" ).getAsInt();
        int ns  = msg.at( "Adjustment.Nanoseconds" ).getAsInt();
        if( sc + sec + ns != 0 ) throw new IllegalStateException(  );

        // test clock transaction response...
        bb = getBytes( "BD BD AF FE 00 01 1F FE 00 01 97 17 00 1B FA 2A 61 C8 00 00 00 04 FA BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        msg = MessageFactory.from( packet );
        if( msg == null ) throw new IllegalStateException(  );
        int nss = msg.at( "OldTime.Seconds" ).getAsInt();
        int nsn = msg.at( "OldTime.Nanoseconds" ).getAsInt();

        // test clock transaction response with permission denied...
        bb = getBytes( "BD BD AF FE 00 01 1F FE 00 01 97 17 01 50 6F BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        msg = MessageFactory.from( packet );
        if( msg == null ) throw new IllegalStateException(  );
        if( msg.at( "OldTime" ).isSet() ) throw new IllegalStateException(  );

        // test get values request...
        bb = getBytes( "BD BD BD A0 01 40 04 10 01 00 04 1A 1A 00 00 50 75 62 6C 69 63 00 09 52 48 00 00 01 2A F9 BD BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        msg = MessageFactory.from( packet );
        if( msg == null ) throw new IllegalStateException(  );
        String tn = msg.at( "TableName" ).getAsString();
        String fn = msg.at( "FieldName" ).getAsString();
        int tc = msg.at( "TypeCode" ).getAsInt();
        int sw = msg.at( "Swath" ).getAsInt();

        // test get values response...
        bb = getBytes( "BD BD BD A0 04 00 01 10 04 00 01 9A 1A 00 42 34 1C 29 69 2A BD BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        msg = MessageFactory.from( packet );
        if( msg == null ) throw new IllegalStateException(  );
        bb = msg.at( "Values" ).getAsByteBuffer();

        // test file upload request...
        bb = getBytes( "BD BD BD A0 01 70 04 10 01 00 04 1D 1D 00 00 43 50 55 3A 44 65 66 2E 74 64 66 00 00 00 00 00 00 00 80 27 EA BD BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        msg = MessageFactory.from( packet );
        if( msg == null ) throw new IllegalStateException(  );
        fn = msg.at( "FileName" ).getAsString();
        int cf = msg.at( "CloseFlag" ).getAsInt();
        int fo = msg.at( "FileOffset" ).getAsInt();
        sw = msg.at( "Swath" ).getAsInt();

        // test file upload response...
        bb = getBytes( "BD BD BD A0 04 00 01 10 04 00 01 9D 1D 00 00 00 00 00 01 53 74 61 74 75 73 00 00 00 00 01 0C " +
                "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8B 4F 53 76 65 72 73 69 6F 6E 00 00 00 00 00 00 00 00 01 00 00 " +
                "00 08 00 00 00 08 00 00 00 00 8B 4F 53 44 61 74 65 00 00 00 00 00 00 00 00 01 00 00 00 0A 00 00 00 0A 00 00 00 " +
                "00 8B 50 72 6F 67 4E 61 6D 65 00 00 00 00 00 00 00 00 01 00 00 00 10 00 00 00 10 00 00 00 00 95 50 72 6F 67 53 " +
                "69 67 00 00 F1 67 BD BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        msg = MessageFactory.from( packet );
        if( msg == null ) throw new IllegalStateException(  );
        bb = msg.at( "FileData" ).getAsByteBuffer();

        // test collect data request...
        bb = getBytes( "BD BD BD A0 01 70 04 10 01 00 04 09 09 00 00 05 00 03 43 15 00 00 00 3C 00 00 C7 DF BD BD BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null ) throw new IllegalStateException(  );
        msg = MessageFactory.from( packet );
        if( msg == null ) throw new IllegalStateException(  );
        int cm = msg.at( "CollectMode" ).getAsInt();
        int tb = msg.at( "Specs.TableNbr", 0 ).getAsInt();
        int p1 = msg.at( "Specs.P1", 0 ).getAsInt();

        // test making a collect data request by setting properties...
        msg = new CollectDataReq();
        msg.at( "CollectMode" ).setTo( 5 );
        msg.finish();
        msg.finish();

        bb.hashCode();
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
