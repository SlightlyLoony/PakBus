package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.*;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.comms.SimpleContext;

import java.nio.ByteBuffer;

import static com.dilatush.pakbus.Protocol.PakCtrl;
import static com.dilatush.pakbus.Protocol.SerPkt;

/**
 * Provides a factory method for creating a message ({@link Msg} instance) from a packet.  This would typically be used to decode a received packet.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MsgFactory {

    /**
     * Returns the message contained in the given packet, or null if there was none.
     *
     * @param _datum the datum for the low-level packet being decoded
     * @return the message in the packet
     */
    public static Msg from( final PacketDatum _datum ) {

        // sanity check...
        if( (_datum == null) )
            throw new IllegalArgumentException( "Required packet argument is missing" );

        // some setup...
        LinkState linkState = LinkState.decode( _datum.at( "LinkState" ).getAsInt() );
        Protocol protocol = _datum.at( "HiLevel" ).isSet()
                ? Protocol.decode( _datum.at( "HiLevel.HiProtoCode" ).getAsInt() )
                : SerPkt;
        ByteBuffer bytes = (protocol == SerPkt)
                ? null
                : _datum.at( "HiLevel.Message" ).getAsByteBuffer();
        Address appAddr = new Address( _datum.at( "DstPhyAddr" ).getAsInt() );
        Address logAddr = new Address( _datum.at( "SrcPhyAddr" ).getAsInt() );
        int appNode = (protocol == SerPkt) ? 0 : _datum.at( "HiLevel.DstNodeId" ).getAsInt();
        int logNode = (protocol == SerPkt) ? 0 : _datum.at( "HiLevel.SrcNodeId" ).getAsInt();
        HopCount hops = (protocol == SerPkt) ? null : new HopCount( _datum.at( "HiLevel.HopCnt" ).getAsInt() );
        int trnNbr = (protocol == SerPkt) ? 0 : 0xFF & bytes.get( 1 );
        Context cx = new SimpleContext( appAddr, logAddr, appNode, logNode, hops, trnNbr );

        // make up our selector code (PakCtrl is protocol 0, BMP5 is protocol 1, SerPkt is 2)...
        int code;
        if( protocol == SerPkt )
            code = 0x200 + linkState.getCode();
        else
            code = ((protocol == PakCtrl) ? 0x000 : 0x100) + (0xFF & bytes.get( 0 ));

        // instantiate the correct type of message class...
        Msg msg;
        switch( code ) {

            case 0x002: msg = new ClockNotificationMsg( bytes, cx ); break;
            case 0x209: msg = new RingMsg( cx );                     break;

            default: msg = new InvalidMsg( "Unknown message selector code: " + code, null );
        }

        // and we're done...
        return msg;
    }
}
