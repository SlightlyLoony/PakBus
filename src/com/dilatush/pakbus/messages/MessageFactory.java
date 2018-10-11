package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.Packet;
import com.dilatush.pakbus.util.BitBuffer;

import java.nio.ByteBuffer;

/**
 * Contains convenience factory methods for decoding and instantiating messages in a packet.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MessageFactory {

    /**
     * Returns the higher protocol message contained in the given packet, or null if there was none.
     *
     * @param _packet the packet with a message
     * @return the message in the packet
     */
    public static Message from( final Packet _packet ) {

        // sanity check...
        if( _packet == null )
            throw new IllegalArgumentException( "Required packet argument is missing" );

        // get our message out of the packet...
        ByteBuffer bb = _packet.getMessage();

        // if there's no buffer or it has nothing in it, then leave with nothing...
        if( (bb == null) || (bb.capacity() == 0) )
            return null;

        // make up our selector code (PakCtrl is protocol 0, BMP5 is protocol 1)...
        int code = 0xFF & bb.get( 0 ) | (_packet.getProtocol().getCode() << 8);

        // instantiate the correct type of message class...
        Message msg;
        switch( code ) {

            case 0x007: msg = new GetStringSettingsReq();  break;
            case 0x117: msg = new ClockReq();              break;
            case 0x197: msg = new ClockRsp();              break;
            case 0x11A: msg = new GetValuesReq();          break;
            case 0x19A: msg = new GetValuesRsp();          break;
            case 0x11D: msg = new FileUpLoadReq();         break;
            case 0x19D: msg = new FileUpLoadRsp();         break;
            case 0x109: msg = new CollectDataReq();        break;

            default: throw new IllegalStateException( "Unknown message code: " + code );
        }

        // now decode our message bytes...
        msg.set( new BitBuffer( bb ) );

        // and we're done...
        return msg;
    }
}
