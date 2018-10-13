package com.dilatush.pakbus;

import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.messages.Msg;
import com.dilatush.pakbus.messages.MsgFactory;
import com.dilatush.pakbus.util.BitBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import static com.dilatush.pakbus.Protocol.BMP5;
import static com.dilatush.pakbus.Protocol.PakCtrl;

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
    final private Signature   signature;
    final private Msg         message;


    /**
     * Create a new instance of this class from the given datum (representing a PakBus packet) and computed signature.  If the packet contains a
     * PakCtrl or BMP5 message, that message is also decoded.
     *
     * @param _datum the decoded low-level packet datum
     * @param _message the decoded message
     * @param _signature the computed signature
     */
    private Packet( final PacketDatum _datum, final Msg _message, final Signature _signature ) {

        datum     = _datum;
        signature = _signature;
        message   = _message;
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
        if( (_message == null) || (_options == null) )
            throw new IllegalArgumentException( "Missing required argument" );

        // some setup...
        message = _message;
        Context cx = _message.context();

        // build our base datum (all three protocols need this)...
        datum = new PacketDatum();
        datum.at( "LinkState"   ).setTo( _options.state.getCode()             );
        datum.at( "DstPhyAddr"  ).setTo( cx.dataloggerAddress().getAddress()  );
        datum.at( "ExpMoreCode" ).setTo( _options.expectMore.getCode()        );
        datum.at( "Priority"    ).setTo( _options.priority.getCode()          );
        datum.at( "SrcPhyAddr"  ).setTo( cx.applicationAddress().getAddress() );

        // if we have a PakCtrl or BMP5 message, we need to add some more...
        if( (_message.protocol() == PakCtrl) || (_message.protocol() == BMP5) ) {

            datum.at( "HiLevel.HiProtoCode" ).setTo( _message.protocol().getCode() );
            datum.at( "HiLevel.DstNodeId"   ).setTo( cx.dataloggerNode()           );
            datum.at( "HiLevel.HopCnt"      ).setTo( cx.hopCount().getHops()       );
            datum.at( "HiLevel.SrcNodeId"   ).setTo( cx.applicationNode()          );
            datum.at( "HiLevel.Message"     ).setTo( _message.bytes()              );
        }

        // get the signature...
        datum.finish();
        ByteBuffer bytes = datum.getAsByteBuffer();
        signature = new Signature( bytes );
    }


    /**
     * Returns a buffer containing the bytes representing the encoded packet.  The packet's bytes are first appended with a signature nullifier, then
     * "quoted" (with Campbell Scientific's scheme to escape sync bytes).
     *
     * @return the encoded buffer
     */
    public ByteBuffer encode() {

        // first we get the unquoted bytes...
        ByteBuffer rawBytes = datum.get().getByteBuffer();
        Signature signature = new Signature( rawBytes );
        ByteBuffer unquoted = ByteBuffer.allocate( rawBytes.capacity() + 2 );
        unquoted.put( rawBytes );
        unquoted.putShort( (short)signature.getNullifier() );

        // then we see how many bytes need quoting...
        int quoteCount = 0;
        for( int i = 0; i < unquoted.limit(); i++ ) {
            byte b = unquoted.get( i );
            if( (b == SYNC) || (b == QUOTE) )
                quoteCount++;
        }
        unquoted.flip();

        // allocate a buffer for our result, with enough room for the quotes...
        ByteBuffer result = ByteBuffer.allocate( unquoted.capacity() + quoteCount );

        // move our bytes into the result, quoting as necessary...
        while( unquoted.hasRemaining() ) {
            byte b = unquoted.get();
            if( (b == SYNC) || (b == QUOTE) ) {
                result.put( QUOTE );
                b += QUOTE_OFFSET;
            }
            result.put( b );
        }
        result.flip();

        return result;
    }


    /**
     * Attempts to deframe and decode a packet from the given buffer.  On invocation, the buffer's position must be at the first byte available for
     * deframing/decoding, and the limit must be at the last available byte.  This method first moves the position past any sync bytes and invalid
     * data.  Then if there is a deframed packet, an attempt can be made to decode it.  If the attempt was successful, the decoded packet is returned
     * and the buffer's position is updated to be at the first byte following the decoded packet.  If there is no packet that can be decoded (meaning,
     * usually, that more bytes are needed), then a null is returned.
     *
     * @param _bytes a buffer filled with bytes to be decoded
     * @return the decoded packet, or null if there was none
     */
    public static Packet deframeAndDecode( final ByteBuffer _bytes ) {

        // first we scan until we hit a sync byte...
        int i = _bytes.position();
        while( (_bytes.get( i ) != SYNC) && (i < _bytes.limit()) ) i++;

        // then scan until we hit something that's NOT a sync...
        while( (_bytes.get( i ) == SYNC) && (i < _bytes.limit()) ) i++;

        // if we've hit the limit, then we can't have the start of a packet...
        if( i >= _bytes.limit() ) return null;

        // remember the start and scan 'till we find the end...
        int start = i;
        while( (_bytes.get( i ) != SYNC) && (i < _bytes.limit()) ) i++;

        // if we've hit the limit, then we can't have a complete packet
        if( i >= _bytes.limit() ) return null;

        // ok, we have a framed packet, so update our position...
        _bytes.position( i );

        return decode( _bytes, start, i );
    }


    /**
     * Attempts to decode a packet from the given buffer.  On invocation, the buffer's position must be at the first byte available for decoding, and
     * the limit must be at the last available byte.  This method attempts to decode the packet from the given buffer, using the bytes from the given
     * start position up to (but not including) the given end position.  This method does not change the position or limit of the given buffer.
     *
     * @param _bytes a buffer filled with bytes to be decoded
     * @param _start the position of the first byte to decode
     * @param _end the position of the first byte after the last byte to decode
     * @return the decoded packet, or null if there was none
     */
    public static Packet decode( final ByteBuffer _bytes, final int _start, final int _end ) {

        // sanity checks...
        if( _bytes == null )
            throw new IllegalArgumentException( "Missing required buffer argument" );
        if( (_start < 0) || (_start >= _bytes.limit()) )
            throw new IllegalArgumentException( "Start index is out of range" );
        if( (_end <= _start) || (_end > _bytes.limit()) )
            throw new IllegalArgumentException( "End index is out of range" );

        // if the length isn't a possible packet, we're outta here...
        ByteBuffer dequoted = dequote( _bytes, _start, _end );
        int len = dequoted.limit();
        if( (len < SHORTEST_POSSIBLE_PACKET) || (len > LONGEST_POSSIBLE_PACKET) )
            throw new IllegalStateException( "Length of packet is impossible: " + len );

        // now check for a good signature...
        dequoted.limit( len - 2 );  // back off two bytes to leave out the signature nullifier...
        Signature signature = new Signature( dequoted );
        dequoted.limit( len );  // and put the limit back where it was...
        if( signature.getNullifier() != ( 0xFFFF & dequoted.getShort( len - 2 ) ) )
            throw new IllegalStateException( "Nullifer did not match" );

        // now we'll attempt to decode the deframed and dequoted packet...
        dequoted.position( 0 );
        dequoted.limit( len - 2 );  // back off two bytes to leave out the signature nullifier...
        PacketDatum datum = new PacketDatum();
        datum.set( new BitBuffer( dequoted ) );
        dequoted.flip();

        // decode our message...
        Msg msg = MsgFactory.from( datum );

        // and now at last we're ready to make our packet...
        return new Packet( datum, msg, signature );
    }


    /**
     * Returns a buffer containing the bytes [start..) in the given buffer with any "quoted" bytes dequoted.  The quoting is Campbell Scientific's
     * proprietary escaping mechanism, required for any sync bytes (0xBD) and for the escape byte (0xBC) itself.  The escaped character has 0x20 added
     * to it.  Thus, 0xBD becomes 0xBC 0xDD, and 0xBC becomes 0xBC 0xDC.  Note that while the escape mechanism is only actually required for bytes of
     * those two values, it is actually valid for any byte.
     *
     * @param _bytes the buffer of bytes to dequote
     * @param _start the starting position in the buffer for the bytes to dequote
     * @param _end the position after the last position for the bytes to dequote
     * @return a buffer with the dequoted bytes, or null if the given buffer was invalid
     */
    private static ByteBuffer dequote( final ByteBuffer _bytes, final int _start, final int _end ) {

        ByteBuffer result = ByteBuffer.allocate( _end - _start );

        for( int i = _start; i < _end; i++ ) {

            byte b = _bytes.get( i );
            if( b == QUOTE ) {
                i++;
                if( i >= _end )
                    throw new IllegalStateException( "Last byte of buffer was the quote character" );
                b = (byte)(_bytes.get( i ) - QUOTE_OFFSET);
            }
            result.put( b );
        }

        result.flip();
        result.order( ByteOrder.BIG_ENDIAN );
        return result;
    }


    @Override
    public boolean equals( final Object _o ) {
        if( this == _o ) return true;
        if( _o == null || getClass() != _o.getClass() ) return false;
        Packet packet = (Packet) _o;
        return Objects.equals( datum, packet.datum ) &&
                Objects.equals( signature, packet.signature );
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
        return Objects.hash( datum, signature );
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
        return new Address( datum.at( "SrcPhysAddr" ).getAsInt() );
    }


    public Protocol getProtocol() {
        return Protocol.decode( datum.at( "HiLevel.HiProtoCode" ).getAsInt() );
    }


    public int getDstNodeID() {
        return datum.at( "HiLevel.DstNodeId" ).getAsInt();
    }


    public HopCount getHopCount() {
        return new HopCount( datum.at( "HiLevel.HopCnt" ).getAsInt() );
    }


    public int getSrcNodeID() {
        return datum.at( "HiLevel.SrcNodeId" ).getAsInt();
    }


    public ByteBuffer getMessage() {
        return datum.at( "HiLevel.Message" ).get().getByteBuffer();
    }


    public Signature getSignature() {
        return signature;
    }


    public Msg getMsg() {
        return message;
    }
}
