package com.dilatush.pakbus;

        import java.nio.ByteBuffer;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Test {


    public static void main( String[] _args ) {

        // test signature and nullifier...
        ByteBuffer bb = getBytes( "A0 01 40 04 10 01 00 04 1A 1A 00 00 50 75 62 6C 69 63 00 09 52 48 00 00 01" );
        Signature sig = new Signature( bb );
        if( sig.getNullifier() != 0x2af9 )
            throw new IllegalStateException( "nullifier failed" );

        bb = getBytes( "BD AF FE 00 01 1F FE 00 01 97 17 00 1B FA 2A 61 C8 00 00 00 04 FA BD" );
        Packet packet = Packet.deframeAndDecode( bb );
        if( packet == null )
            throw new IllegalStateException( "Bad packet" );
        ByteBuffer en = packet.encode();
        Packet packet2 = Packet.decode( en, 0, en.limit() );
        if( !packet.equals( packet2 ) )
            throw new IllegalStateException( "Packet encoding/decoding problem" );

        bb = getBytes("BD A0 04 00 01 10 04 00 01 89 09 00 00 03 00 00 00 19 00 0D 1B F3 AB 90 41 20 07 D7 " +
                "41 1F AE 71 41 B7 A8 CB 42 33 45 10 37 27 C5 C8 44 F0 05 9A 3F A9 6D 27 41 B4 B5 90 1B F3 A7 D0 41 B9 " +
                "45 10 1B F3 9E E8 37 27 C5 C8 42 87 AB 2C 37 27 C5 AC 1B F3 9D 8A 42 00 04 DD 00 00 00 00 1B F3 B9 A0 " +
                "41 1F F6 A6 41 1F B8 C2 41 B6 59 5C 42 33 5E 5A 37 27 C5 C8 44 F1 EC 5B 3F AA 68 18 41 B3 FE C0 1B F3 " +
                "AC A8 41 B7 94 A0 1B F3 B5 72 37 27 C5 C8 42 85 F6 D2 37 27 C5 AC 1B F3 AB 9A 42 00 AA 6B 00 00 00 00 " +
                "1B F3 C7 B0 41 20 3B 6A 41 1F 69 AD 41 B4 BB 66 42 33 D8 A6 37 27 C5 C8 44 F5 E5 94 3F 40 05 7D 41 B1 " +
                "BF D0 1B F3 C7 1A 41 B7 54 E0 1B F3 B9 AA 37 27 C5 C8 42 86 DA 20 37 27 C5 AC 1B F3 B9 AA 42 02 4B " +
                "A5 00 00 00 00 1B F3 D5 C0 41 20 00 F6 41 1F AE 71 41 B8 3B 61 42 34 DE 9D 37 27 C5 C8 44 FD 3B 18 3F 47 " +
                "77 B5 41 B1 DC 90 1B F3 C7 BA 41 B9 F9 50 1B F3 D5 C0 37 27 C5 C8 42 87 4A C4 37 27 C5 AC 1B F3 C7 BA " +
                "42 01 0C 20 00 00 00 00 1B F3 E3 D0 41 20 5A 5C 41 1F A4 21 41 B9 99 34 42 34 02 3F 37 27 C5 C8 44 F6 12 " +
                "D8 3F 42 05 97 41 B8 E7 B0 1B F3 D7 1E 41 BA 64 50 1B F3 DD CC 37 27 C5 C8 42 88 29 1E 37 27 C5 AC 1B " +
                "F3 D5 CA 42 01 57 07 00 00 00 00 1B F3 F1 E0 41 20 75 DE 41 1F 8F 80 41 B9 AB 92 42 33 DC 36 37 27 C5 C8 " +
                "44 F5 DC EB 3F 41 DE A8 41 B9 22 60 1B F3 E9 98 41 BA B4 B0 1B F3 E5 42 37 27 C5 C8 42 88 58 24 37 27 " +
                "C5 AC 1B F3 E3 DA 42 01 4F 81 00 00 00 00 1B F3 FF F0 41 1F E2 04 41 1F AE 71 41 B9 8E 48 42 33 CF DC 37 " +
                "27 C5 C8 44 F5 94 7A 3F 41 97 04 41 B8 A9 20 1B F3 F8 20 41 BA 44 30 1B F3 F8 DE 37 27 C5 C8 42 89 71 88 " +
                "37 27 C5 AC 1B F3 F1 EA 42 01 3D AD 00 00 00 00 1B F4 0E 00 41 20 79 4E 41 1F A4 21 41 B8 F6 AF 42 33 E2 " +
                "68 37 27 C5 C8 44 F5 B6 EE 3F 41 7A F8 41 B7 B5 D0 1B F4 0C 0C 41 B9 EA A0 1B F4 00 AE 37 27 C5 C8 42 " +
                "88 3A 45 37 27 C5 AC 1B F3 FF FA 42 00 B6 75 00 00 00 00 1B F4 1C 10 41 20 00 F6 41 1F AB 01 41 B8 00 CD " +
                "42 34 41 6B 37 27 C5 C8 44 F6 C7 AB 3F 42 02 B4 41 B7 01 60 1B F4 18 D2 41 B8 E6 70 1B F4 10 26 37 27 C5 " +
                "C8 42 87 D5 7B 37 27 C5 AC 1B F4 0E 0A 42 00 F7 DD 00 00 00 00 1B F4 2A 20 41 20 53 7C 41 1F AE 71 41 " +
                "B7 3D EC 42 34 A5 4E 37 27 C5 C8 44 FA 4A 73 3F 44 A3 A2 41 B6 41 70 1B F4 28 04 41 B8 53 F0 1B F4 1D " +
                "46 37 27 C5 C8 42 87 C7 91 37 27 C5 AC 1B F4 1C 1A 42 01 35 74 00 00 00 00 1B F4 38 30 41 20 75 DE 41 1F " +
                "BC DC 32 41 B6 7A 3E 42 35 1C 3D 37 27 C5 C8 44 FE BF 4A 3F 48 0A A1 41 B5 9E F0 1B F4 34 B6 41 B7 60 " +
                "D0 1B F4 2A 34 37 27 C5 C8 42 86 CE 17 37 27 C5 AC 1B F4 2A 2A 42 00 40 7C 00 00 00 00 1B F4 46 40 41 20 " +
                "7C BE 41 1F AB 01 41 B5 CA 2E 42 35 3C E4 37 27 C5 C8 45 03 3E 30 3F 4E 2A A0 41 B4 B0 50 1B F4 45 96 " +
                "41 B6 CC 60 1B F4 39 70 37 27 C5 C8 42 87 09 00 37 27 C5 AC 1B F4 38 3A 42 01 6A 5D 00 00 00 00 1B F4 54 " +
                "50 41 20 6B 8D 41 1F A0 B0 41 B5 17 7F 42 35 4C 28 37 27 C5 C8 45 06 95 7C 3F 53 65 75 41 B4 39 F0 1B F4 " +
                "4C 30 41 B6 00 A0 1B F4 46 5E 37 27 C5 C8 42 88 2C 02 37 27 C5 AC 1B F4 46 4A 42 01 D9 44 00 00 00 00 01 " +
                "17 3E BD" );
        packet = Packet.deframeAndDecode( bb );
        if( packet == null )
            throw new IllegalStateException( "Bad packet" );
        en = packet.encode();
        packet2 = Packet.decode( en, 0, en.limit() );
        if( !packet.equals( packet2 ) )
            throw new IllegalStateException( "Packet encoding/decoding problem" );

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
