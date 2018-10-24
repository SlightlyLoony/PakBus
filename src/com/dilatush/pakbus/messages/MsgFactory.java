package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.*;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.messages.bmp5.*;
import com.dilatush.pakbus.messages.pakctrl.*;
import com.dilatush.pakbus.messages.serpkt.ReadyMsg;
import com.dilatush.pakbus.messages.serpkt.RingMsg;
import com.dilatush.pakbus.util.Checks;

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
     * @param _packet the low-level packet being decoded
     * @return the message in the packet
     */
    public static Msg from( final Packet _packet, final Context _cx ) {

        // sanity check...
        Checks.required( _packet );

        // some setup...
        PacketDatum datum = _packet.getDatum();
        LinkState linkState = LinkState.decode( datum.at( "LinkState" ).getAsInt() );
        Protocol protocol = datum.at( "HiLevel" ).isSet()
                ? Protocol.decode( datum.at( "HiLevel.HiProtoCode" ).getAsInt() )
                : SerPkt;
        ByteBuffer bytes = (protocol == SerPkt)
                ? null
                : datum.at( "HiLevel.Message" ).getAsByteBuffer();
        Address appAddr = new Address( datum.at( "DstPhyAddr" ).getAsInt() );
        Address logAddr = new Address( datum.at( "SrcPhyAddr" ).getAsInt() );
        int appNode = (protocol == SerPkt) ? 0 : datum.at( "HiLevel.DstNodeId" ).getAsInt();
        int logNode = (protocol == SerPkt) ? 0 : datum.at( "HiLevel.SrcNodeId" ).getAsInt();
        HopCount hops = (protocol == SerPkt) ? null : new HopCount( datum.at( "HiLevel.HopCnt" ).getAsInt() );
        int trnNbr = (protocol == SerPkt) ? 0 : 0xFF & bytes.get( 1 );

        // make up our selector code (PakCtrl is protocol 0, BMP5 is protocol 1, SerPkt is 2)...
        int code;
        if( protocol == SerPkt )
            code = 0x200 + linkState.getCode();
        else
            code = ((protocol == PakCtrl) ? 0x000 : 0x100) + (0xFF & bytes.get( 0 ));

        // instantiate the correct type of message class...
        AMsg msg;
        switch( code ) {

            case 0x002: msg = new ClockNotificationMsg( bytes, _cx );            break;
            case 0x007: msg = new GetStringSettingsReqMsg( bytes, _cx );         break;
            case 0x087: msg = new GetStringSettingsRspMsg( bytes, _cx );         break;
            case 0x008: msg = new SetStringSettingsReqMsg( bytes, _cx );         break;
            case 0x088: msg = new SetStringSettingsRspMsg( bytes, _cx );         break;
            case 0x009: msg = new HelloReqMsg( bytes, _cx );                     break;
            case 0x089: msg = new HelloRspMsg( bytes, _cx );                     break;
            case 0x117: msg = new ClockReqMsg( bytes, _cx );                     break;
            case 0x197: msg = new ClockRspMsg( bytes, _cx );                     break;
            case 0x19A: msg = new GetValuesRspMsg( bytes, _cx );                 break;
            case 0x19B: msg = new SetValuesRspMsg( bytes, _cx );                 break;
            case 0x11D: msg = new FileReceiveReqMsg( bytes, _cx );               break;
            case 0x19D: msg = new FileReceiveRspMsg( bytes, _cx );               break;
            case 0x11E: msg = new FileControlReqMsg( bytes, _cx );               break;
            case 0x19E: msg = new FileControlRspMsg( bytes, _cx );               break;
            case 0x118: msg = new GetProgrammingStatisticsReqMsg( bytes, _cx );  break;
            case 0x198: msg = new GetProgrammingStatisticsRspMsg( bytes, _cx );  break;
            case 0x189: msg = new CollectDataRspMsg( bytes, _cx );               break;
            case 0x209: msg = new RingMsg( _cx );                                break;
            case 0x20A: msg = new ReadyMsg( _cx );                               break;

            default: msg = new InvalidMsg( "Unknown message selector code: " + code, null );
        }

        // and we're done...
        msg.setPacket( _packet );
        return msg;
    }
}
