package com.dilatush.pakbus.comms;

import com.dilatush.pakbus.app.Datalogger;
import com.dilatush.pakbus.messages.Msg;
import com.dilatush.pakbus.types.ExpectMore;
import com.dilatush.pakbus.types.LinkState;
import com.dilatush.pakbus.types.Priority;
import com.dilatush.pakbus.types.Protocol;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.*;

import java.nio.ByteBuffer;
import java.util.Objects;

import static com.dilatush.pakbus.types.Protocol.BMP5;
import static com.dilatush.pakbus.types.Protocol.PakCtrl;

/**
 * Instances of this class represent a Campbell Scientific PakBus packet, the lowest level of their datalogger communication protocol.  This class
 * provides methods to decode a PakBus packet from a data stream and to encode packets to a data stream.  Instances of this class are immutable and
 * threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Packet {

    final static private byte SYNC = (byte) 0xBD;
    final static private byte QUOTE = (byte) 0xBC;
    final static private int QUOTE_OFFSET = 0x20;
    final static private int SHORTEST_POSSIBLE_PACKET = 6;
    final static private int LONGEST_POSSIBLE_PACKET = 1010;

    final private PacketDatum datum;
//    final private Msg         message;


    /**
     * Create a new instance of this class from the given datum (representing a PakBus packet) and computed signature.  If the packet contains a
     * PakCtrl or BMP5 message, that message is also decoded.
     *
     * @param _datum the decoded low-level packet datum
     */
    private Packet( final PacketDatum _datum ) {

        datum     = _datum;
    }


    /**
     * Creates a new instance of this class from the given message and with the given options.  The packet created is formatted to be sent from the
     * application to a datalogger.
     *
     * @param _message the message to create the packet for
     * @param _options the packet options for this message
     */
    public Packet( final Msg _message, final PacketOptions _options ) {

        // sanity checks...
        Checks.required( _message, _options );

        // some setup...
        Context cx = _message.context();

        // build our base datum (all three protocols need this)...
        Datalogger logger = cx.datalogger();
        Address dstAddr = (logger == null) ? Address.BROADCAST : logger.address.address;
        int dstNode = (logger == null) ? 0 : logger.address.nodeID;
        HopCount dstHops = (logger == null ) ? HopCount.ZERO : logger.hopCount;
        datum = new PacketDatum();
        datum.at( "LinkState"   ).setTo( _options.state.getCode()                      );
        datum.at( "DstPhyAddr"  ).setTo( dstAddr.getAddress()                          );
        datum.at( "ExpMoreCode" ).setTo( _options.expectMore.getCode()                 );
        datum.at( "Priority"    ).setTo( _options.priority.getCode()                   );
        datum.at( "SrcPhyAddr"  ).setTo( cx.application().address.address.getAddress() );

        // if we have a PakCtrl or BMP5 message, we need to add some more...
        if( (_message.protocol() == PakCtrl) || (_message.protocol() == BMP5) ) {

            datum.at( "HiLevel.HiProtoCode" ).setTo( _message.protocol().getCode()      );
            datum.at( "HiLevel.DstNodeId"   ).setTo( dstNode                            );
            datum.at( "HiLevel.HopCnt"      ).setTo( dstHops.getHops()                  );
            datum.at( "HiLevel.SrcNodeId"   ).setTo( cx.application().address.nodeID    );
            datum.at( "HiLevel.Message"     ).setTo( _message.bytes()                   );
        }

        // get the signature...
        datum.finish();
        ByteBuffer bytes = datum.getAsByteBuffer();
    }


    /**
     * Returns a buffer containing the bytes representing the encoded packet.
     *
     * @return the encoded buffer
     */
    public ByteBuffer encode() {
        return datum.get().getByteBuffer();
    }


    /**
     * Attempts to decode a packet from the given raw packet, which must have the bytes of the packet plus the signature nullifier.
     */
    public static Packet decode( final RawPacket _packet ) {

        // sanity checks...
        Checks.required( _packet );

        // decode the packet datum...
        ByteBuffer bytes = _packet.packetBytes;
        bytes.position( 0 );
        bytes.limit( bytes.limit() - 2 );  // clip the signature nullifier
        PacketDatum datum = new PacketDatum();
        datum.set( new BitBuffer( bytes ) );
        bytes.flip();

        // and now at last we're ready to make our packet...
        return new Packet( datum );
    }


    @Override
    public boolean equals( final Object _o ) {
        if( this == _o ) return true;
        if( _o == null || getClass() != _o.getClass() ) return false;
        Packet packet = (Packet) _o;
        return Objects.equals( datum, packet.datum );
    }


    /**
     * Returns the number of bytes in this packet.
     *
     * @return the number of bytes in this packet
     */
    public int length() {
        return (datum.size() + 7) >>> 3;
    }


    @Override
    public int hashCode() {
        return Objects.hash( datum );
    }


    public LinkState getLinkState() {
        return LinkState.decode( datum.at( "LinkState" ).getAsInt() );
    }


    public Address getDstPhysAddr() {
        return new Address( datum.at( "DstPhyAddr" ).getAsInt() );
    }


    public ExpectMore getExpectMore() {
        return ExpectMore.decode( datum.at( "ExpMoreCode" ).getAsInt() );
    }


    public Priority getPriority() {
        return Priority.decode( datum.at( "Priority" ).getAsInt() );
    }


    public Address getSrcPhysAddr() {
        return new Address( datum.at( "SrcPhyAddr" ).getAsInt() );
    }


    public Protocol getProtocol() {
        return Protocol.decode( datum.at( "HiLevel.HiProtoCode" ).getAsInt() );
    }


    public int getDstNodeID() {
        Datum dat = datum.at( "HiLevel.DstNodeId" );
        return dat.isSet() ? dat.getAsInt() : 0;
    }


    public HopCount getHopCount() {
        return new HopCount( datum.at( "HiLevel.HopCnt" ).getAsInt() );
    }


    public int getSrcNodeID() {
        Datum dat = datum.at( "HiLevel.SrcNodeId" );
        return dat.isSet() ? dat.getAsInt() : 0;
    }


    public ByteBuffer getMessage() {
        return datum.at( "HiLevel.Message" ).get().getByteBuffer();
    }


    public PacketDatum getDatum() {
        return datum;
    }


    public Node getSrcAddr() {
        return new Node( getSrcPhysAddr(), getSrcNodeID() );
    }


    public Node getDstAddr() {
        return new Node( getDstPhysAddr(), getDstNodeID() );
    }
}
