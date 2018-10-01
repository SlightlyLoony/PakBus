package com.dilatush.pakbus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Represents a Campbell Scientific PakBus packet, the lowest level of their datalogger communication protocol.  This class provides methods to
 * decode a PakBus packet from a data stream and to encode packets to a data stream.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Packet {

    private static final byte SYNC = (byte) 0xBD;
    private static final byte QUOTE = (byte) 0xBC;
    private static final int QUOTE_OFFSET = 0x20;
    private static final int SHORTEST_POSSIBLE_PACKET = 10;
    private static final int LONGEST_POSSIBLE_PACKET = 1010;

    private final LinkState  linkState;
    private final Address    dstPhysicalAddr;
    private final ExpectMore expectMore;
    private final Priority   priority;
    private final Address    srcPhysicalAddr;
    private final Protocol   protocol;
    private final Address    dstNodeID;
    private final HopCount   hopCount;
    private final Address    srcNodeID;
    private final ByteBuffer message;
    private final Signature  signature;


    private Packet( final LinkState _linkState, final Address _dstPhysicalAddr, final ExpectMore _expectMore, final Priority _priority,
                    final Address _srcPhysicalAddr, final Protocol _protocol, final Address _dstNodeID, final HopCount _hopCount,
                    final Address _srcNodeID, final ByteBuffer _message, final Signature _signature ) {

        linkState       = _linkState;
        dstPhysicalAddr = _dstPhysicalAddr;
        expectMore      = _expectMore;
        priority        = _priority;
        srcPhysicalAddr = _srcPhysicalAddr;
        protocol        = _protocol;
        dstNodeID       = _dstNodeID;
        hopCount        = _hopCount;
        srcNodeID       = _srcNodeID;
        message         = _message;
        signature       = _signature;
    }


    /**
     * Returns a buffer containing the bytes representing the encoded packet.
     *
     * @return the encoded buffer
     */
    public ByteBuffer encode() {

        // first we get the unquoted bytes...
        ByteBuffer unquoted = ByteBuffer.allocate( 10 + message.limit() );
        unquoted.order( ByteOrder.BIG_ENDIAN );
        unquoted.putShort( (short)((linkState.getCode() << 12) | dstPhysicalAddr.getAddress()) );
        unquoted.putShort( (short)((expectMore.getCode() << 14) |(priority.getCode() << 12) | srcPhysicalAddr.getAddress()) );
        unquoted.putShort( (short)((protocol.getCode() << 12) | dstNodeID.getAddress()) );
        unquoted.putShort( (short)((hopCount.getHops() << 12) | srcNodeID.getAddress()) );
        message.position( 0 );
        unquoted.put( message );
        message.position( 0 );
        unquoted.putShort( (short)(signature.getNullifier()) );
        unquoted.flip();

        // then we how many bytes need quoting...
        int quoteCount = 0;
        for( int i = 0; i < unquoted.limit(); i++ ) {
            byte b = unquoted.get( i );
            if( (b == SYNC) || (b == QUOTE) )
                quoteCount++;
        }

        // if we didn't need to quote any, just leave with the buffer we already have...
        if( quoteCount == 0 )
            return unquoted;

        // otherwise quote them...
        ByteBuffer result = ByteBuffer.allocate( unquoted.capacity() + quoteCount );
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

        // if the length isn't a possible packet, we're outta here...
        ByteBuffer dequoted = dequote( _bytes, _start, _end );
        if( dequoted == null ) return null;
        int len = dequoted.limit();
        if( (len < SHORTEST_POSSIBLE_PACKET) || (len > LONGEST_POSSIBLE_PACKET) ) return null;

        // now check for a good signature...
        dequoted.limit( len - 2 );  // back off two bytes to leave out the signature nullifier...
        Signature signature = new Signature( dequoted );
        dequoted.limit( len );  // and put the limit back where it was...
        if( signature.getNullifier() != (int) dequoted.getShort( len - 2 ) ) return null;

        // now we'll attempt to decode the deframed and dequoted packet...
        LinkState linkState = LinkState.decode( 0x0F & dequoted.get( 0 ) >>> 4 );
        Address dstPhysAddr = new Address( 0x0FFF & dequoted.getShort( 0 ) );
        ExpectMore expectMore = ExpectMore.decode( 0x3 & dequoted.get( 2 ) >>> 6 );
        Priority priority = Priority.decode( 0x3 & (dequoted.get( 2 ) >>> 4 ) );
        Address srcPhysAddr = new Address( 0x0FFF & dequoted.getShort( 2 ) );
        Protocol protocol = Protocol.decode( 0x0F & dequoted.get( 4 ) >>> 4 );
        Address dstNodeID = new Address( 0x0FFF & dequoted.getShort( 4 ) );
        HopCount hopCount = new HopCount( 0x0F & dequoted.get( 6 ) >>> 4 );
        Address srcNodeID = new Address( 0x0FFF & dequoted.getShort( 6 ) );
        dequoted.flip();

        // if we had any invalid decodings, chuck it all...
        if( !(linkState.isValid() && expectMore.isValid() && priority.isValid() && protocol.isValid()) ) return null;

        // get our message bytes into a buffer...
        int msgLen = len - 10;
        ByteBuffer msg = ByteBuffer.allocate( msgLen );
        dequoted.position( 8 );
        dequoted.limit( len - 2 );
        msg.put( dequoted );
        msg.flip();

        // and now at last we're ready to make our packet...
        return new Packet( linkState, dstPhysAddr, expectMore, priority, srcPhysAddr, protocol, dstNodeID, hopCount, srcNodeID, msg, signature );
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
                if( i >= _end ) return null;
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
        return linkState == packet.linkState &&
                Objects.equals( dstPhysicalAddr, packet.dstPhysicalAddr ) &&
                expectMore == packet.expectMore &&
                priority == packet.priority &&
                Objects.equals( srcPhysicalAddr, packet.srcPhysicalAddr ) &&
                protocol == packet.protocol &&
                Objects.equals( dstNodeID, packet.dstNodeID ) &&
                Objects.equals( hopCount, packet.hopCount ) &&
                Objects.equals( srcNodeID, packet.srcNodeID ) &&
                Objects.equals( message, packet.message ) &&
                Objects.equals( signature, packet.signature );
    }


    @Override
    public int hashCode() {
        return Objects.hash( linkState, dstPhysicalAddr, expectMore, priority, srcPhysicalAddr, protocol, dstNodeID, hopCount, srcNodeID, message, signature );
    }


    public LinkState getLinkState() {
        return linkState;
    }


    public Address getDstPhysicalAddr() {
        return dstPhysicalAddr;
    }


    public ExpectMore getExpectMore() {
        return expectMore;
    }


    public Priority getPriority() {
        return priority;
    }


    public Address getSrcPhysicalAddr() {
        return srcPhysicalAddr;
    }


    public Protocol getProtocol() {
        return protocol;
    }


    public Address getDstNodeID() {
        return dstNodeID;
    }


    public HopCount getHopCount() {
        return hopCount;
    }


    public Address getSrcNodeID() {
        return srcNodeID;
    }


    public ByteBuffer getMessage() {
        return message;
    }


    public Signature getSignature() {
        return signature;
    }
}
