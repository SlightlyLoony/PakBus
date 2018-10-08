package com.dilatush.pakbus.util;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static com.dilatush.pakbus.util.BitAddress.ZERO;

/**
 * Instances of this class are mutable buffers that are addressable at the bit level.  That is, they implement what appears to be an addressable
 * array of bits of an arbitrary length.  Methods are provided to store and set bit sequences.  Each instance has a capacity (the maximum number of
 * bits that it can hold), a position (the bit address at which the next operation will occur), and a limit (the last position that can be read).
 * Users with NIO experience will note that the API strongly resembles that of ByteBuffer.  Instances of this class are mutable and NOT threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BitBuffer {

    private static final int BITS_MASK[] = { 0x00, 0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF };

    private int        capacity;
    private int        position;
    private int        limit;
    private ByteBuffer buffer;


    /**
     * Creates a new instance of this class from the bytes remaining in the given byte buffer.
     *
     * @param _buffer the byte buffer to use as the source for the new bit buffer
     */
    public BitBuffer( final ByteBuffer _buffer ) {

        // sanity checks...
        if( (_buffer == null) || (_buffer.remaining() == 0) )
            throw new IllegalArgumentException( "Byte buffer is missing or empty." );

        capacity = _buffer.remaining() << 3;
        limit = capacity;
        position = 0;
        buffer = _buffer;
    }


    /**
     * Creates a new instance of this class with the given capacity. The new instance's position will be zero, and the limit will be equal to the
     * capacity.
     *
     * @param _capacity the capacity of the new buffer.
     */
    public BitBuffer( final int _capacity ) {

        if( _capacity < 0 )
            throw new IllegalArgumentException( "Invalid capacity: " + _capacity );

        // calculate the byte buffer size needed to hold these bits...
        int bytesNeeded = (_capacity + 7) >>> 3;

        // initialize...
        capacity = _capacity;
        position = 0;
        limit = _capacity;
        buffer = ByteBuffer.allocate( bytesNeeded );
    }


    /**
     * Creates a new instance of this class with the given capacity, and containing the given bits.  The source bits are LSB-aligned (meaning that
     * the LSB of the bits is in the LSB of _srcBits).  The new instance's position will be zero, and the limit will be equal to the capacity.
     *
     * @param _srcBits the bits to write, LSB-aligned
     * @param _capacity the capacity of the new buffer, which must be no greater than 64
     */
    public BitBuffer( final long _srcBits, final int _capacity ) {

        if( (_capacity < 1 ) || (_capacity > 64) )
            throw new IllegalArgumentException( "Invalid capacity: " + _capacity );

        // calculate the byte buffer size needed to hold these bits...
        int bytesNeeded = (_capacity + 7) >>> 3;

        // rotate the bits into the correct position...
        long srcBits = Long.rotateLeft( _srcBits, 72 - _capacity );

        // initialize...
        capacity = _capacity;
        position = 0;
        limit = _capacity;
        buffer = ByteBuffer.allocate( bytesNeeded );

        // stuff our bits away...
        for( int i = 0; i < buffer.capacity(); i++ ) {
            buffer.put( i, (byte) srcBits );
            srcBits = Long.rotateLeft( srcBits, 8 );
        }
    }


    /**
     * Writes the given number of bits at the given source address from the given source buffer to this buffer at the given destination address.
     *
     * @param _dstAddr the address to write the first of the given bits to
     * @param _srcBuffer the buffer containing the bits to write
     * @param _srcAddr the address to read the first bit from
     * @param _bits the number of sequential bits to write
     * @throws IndexOutOfBoundsException if _dstAddr is less than zero, or greater than or equal to this buffer's limit, or if the _srcAddr
     *         is less than zero, or greater than or equal to the _srcBuffer's limit.
     * @throws BufferOverflowException if this buffer's capacity is insufficient to write _bits at _dstAddr
     * @throws BufferUnderflowException if _srcBuffer's capacity is insufficient to read _bits at _srcAddr
     */
    public void put( final BitAddress _dstAddr, final BitBuffer _srcBuffer, final BitAddress _srcAddr, final int _bits ) {

        copyBits( this, _dstAddr, _srcBuffer, _srcAddr, _bits );
    }


    /**
     * Writes the given number of bits from the given source buffer at the given source address to this buffer at the current position.
     *
     * @param _srcBuffer the buffer containing the bits to write
     * @param _srcAddr the address to read the first bit from
     * @param _bits the number of sequential bits to write
     * @throws IndexOutOfBoundsException if _dstAddr is less than zero, or greater than or equal to this buffer's limit, or if the _srcAddr
     *         is less than zero, or greater than or equal to the _srcBuffer's limit.
     * @throws BufferOverflowException if this buffer's capacity is insufficient to write _bits at _dstAddr
     * @throws BufferUnderflowException if _srcBuffer's capacity is insufficient to read _bits at _srcAddr
     */
    public void put( final BitBuffer _srcBuffer, final BitAddress _srcAddr, final int _bits ) {
        put( new BitAddress( position ), _srcBuffer, _srcAddr, _bits );

        // update the destination position...
        adjustPosition( _bits );
        if( limit < position ) limit = position;
    }


    /**
     * Writes the given number of bits from the source buffer to this buffer at the given destination address.
     *
     * @param _dstAddr the address to write the first of the given bits to
     * @param _srcBuffer the buffer containing the bits to write
     * @param _bits the number of sequential bits to write
     * @throws IndexOutOfBoundsException if _dstAddr is less than zero, or greater than or equal to this buffer's limit, or if the _srcAddr
     *         is less than zero, or greater than or equal to the _srcBuffer's limit.
     * @throws BufferOverflowException if this buffer's capacity is insufficient to write _bits at _dstAddr
     * @throws BufferUnderflowException if _srcBuffer's capacity is insufficient to read _bits at _srcAddr
     */
    public void put( final BitAddress _dstAddr, final BitBuffer _srcBuffer, final int _bits ) {

        put( _dstAddr, _srcBuffer, new BitAddress( _srcBuffer.position ), _bits );

        // update the source position...
        _srcBuffer.adjustPosition( _bits );
    }


    /**
     * Writes all remaining bits in the source buffer to this buffer at the given destination address.
     *
     * @param _dstAddr the address to write the first of the given bits to
     * @param _srcBuffer the buffer containing the bits to write
     * @throws IndexOutOfBoundsException if _dstAddr is less than zero, or greater than or equal to this buffer's limit, or if the _srcAddr
     *         is less than zero, or greater than or equal to the _srcBuffer's limit.
     * @throws BufferOverflowException if this buffer's capacity is insufficient to write _bits at _dstAddr
     * @throws BufferUnderflowException if _srcBuffer's capacity is insufficient to read _bits at _srcAddr
     */
    public void put( final BitAddress _dstAddr, final BitBuffer _srcBuffer ) {

        int bits = _srcBuffer.remaining();
        put( _dstAddr, _srcBuffer, new BitAddress( _srcBuffer.position ), bits );

        // update the source position...
        _srcBuffer.adjustPosition( bits );
    }


    /**
     * Writes the given number of bits from the source buffer to this buffer at the current position.
     *
     * @param _srcBuffer the buffer containing the bits to write
     * @param _bits the number of sequential bits to write
     * @throws IndexOutOfBoundsException if _dstAddr is less than zero, or greater than or equal to this buffer's limit, or if the _srcAddr
     *         is less than zero, or greater than or equal to the _srcBuffer's limit.
     * @throws BufferOverflowException if this buffer's capacity is insufficient to write _bits at _dstAddr
     * @throws BufferUnderflowException if _srcBuffer's capacity is insufficient to read _bits at _srcAddr
     */
    public void put( final BitBuffer _srcBuffer, final int _bits ) {

        // first copy the bits...
        put( new BitAddress( position ), _srcBuffer, new BitAddress( _srcBuffer.position ), _bits );

        // then update the source and destination positions...
        _srcBuffer.adjustPosition( _bits );
        adjustPosition( _bits );
        if( limit < position ) limit = position;
    }


    /**
     * Writes all remaining bits in the source buffer to this buffer at the current position.
     *
     * @param _srcBuffer the buffer containing the bits to write
     * @throws IndexOutOfBoundsException if _dstAddr is less than zero, or greater than or equal to this buffer's limit, or if the _srcAddr
     *         is less than zero, or greater than or equal to the _srcBuffer's limit.
     * @throws BufferOverflowException if this buffer's capacity is insufficient to write _bits at _dstAddr
     * @throws BufferUnderflowException if _srcBuffer's capacity is insufficient to read _bits at _srcAddr
     */
    public void put( final BitBuffer _srcBuffer ) {

        if( _srcBuffer == null )
            throw new IllegalArgumentException( "Required source buffer parameter is missing" );

        if( _srcBuffer.remaining() == 0 )
            return;

        // first copy the bits...
        int bits = _srcBuffer.remaining();
        put( new BitAddress( position ), _srcBuffer, new BitAddress( _srcBuffer.position ), bits );

        // then update the source and destination positions...
        _srcBuffer.adjustPosition( bits );
        adjustPosition( bits );
        if( limit < position ) limit = position;
    }


    public void put( final BitAddress _dstAddr, final long _src, final int _bits ) {
        BitBuffer bb = new BitBuffer( _src, _bits );
        put( _dstAddr, bb );
    }


    public void put( final long _src, final int _bits ) {
        BitBuffer bb = new BitBuffer( _src, _bits );
        put( bb );
    }


    public void put( final BitAddress _dstAddr, final long _src ) {
        put( _dstAddr, _src, 64 );
    }


    public void put( final long _src ) {
        put( _src, 64 );
    }


    public void put( final BitAddress _dstAddr, final int _src ) {
        put( _dstAddr, _src, 32 );
    }


    public void put( final int _src ) {
        put( _src, 32 );
    }


    public void put( final BitAddress _dstAddr, final short _src ) {
        put( _dstAddr, _src, 16 );
    }


    public void put( final short _src ) {
        put(  _src, 16 );
    }


    public void put( final BitAddress _dstAddr, final byte _src ) {
        put( _dstAddr, _src, 8 );
    }

    public void put( final byte _src ) {
        put( _src, 8 );
    }


    public void put( final BitAddress _dstAddr, final boolean _src ) {
        put( _dstAddr, _src ? 1 : 0, 1 );
    }


    public void put( final boolean _src ) {
        put( _src ? 1 : 0, 1 );
    }


    /**
     * Reads the given number of bits from this buffer at the given source address, and returns them in a new bit buffer.
     *
     * @param _srcAddr the address to read the first bit from
     * @param _bits the number of sequential bits to write
     * @return the new bit buffer containing the bits read
     * @throws IndexOutOfBoundsException if _srcAddr is less than zero, or greater than or equal to this buffer's limit
     * @throws BufferUnderflowException if this buffer's capacity is insufficient to read _bits at _srcAddr
     */
    public BitBuffer get( final BitAddress _srcAddr, final int _bits ) {

        // sanity checks...
        if( _bits < 0 )
            throw new IllegalArgumentException( "Number of bits is invalid: " + _bits );

        BitBuffer result = new BitBuffer( _bits );
        copyBits( result, ZERO, this, _srcAddr, _bits );
        return result;
    }


    /**
     * Reads the given number of bits from this buffer from its current position, and returns them in a new bit buffer.
     *
     * @param _bits the number of sequential bits to write
     * @return the new bit buffer containing the bits read
     * @throws IndexOutOfBoundsException if _srcAddr is less than zero, or greater than or equal to this buffer's limit
     * @throws BufferUnderflowException if this buffer's capacity is insufficient to read _bits at _srcAddr
     */
    public BitBuffer get( final int _bits ) {

        // sanity checks...
        if( _bits < 0 )
            throw new IllegalArgumentException( "Number of bits is invalid: " + _bits );

        BitBuffer result = new BitBuffer( _bits );
        copyBits( result, ZERO, this, new BitAddress( position ), _bits );

        adjustPosition( _bits );

        return result;
    }


    /**
     * Reads the remaining bits from this buffers, and returns them in a new bit buffer.
     *
     * @return the new bit buffer containing the bits read
     * @throws IndexOutOfBoundsException if _srcAddr is less than zero, or greater than or equal to this buffer's limit
     * @throws BufferUnderflowException if this buffer's capacity is insufficient to read _bits at _srcAddr
     */
    public BitBuffer get() {

        BitBuffer result = new BitBuffer( remaining() );
        copyBits( result, ZERO, this, new BitAddress( position ), remaining() );

        adjustPosition( remaining() );

        return result;
    }


    /**
     * Returns a long containing the given number of bits from the given source address.  The bits returned are LSB-aligned (i.e., the LSB of the
     * bits read is in the LSB of the returned value).  Unused bits will be zero.
     *
     * @param _srcAddress source address for the MSB of the bits to read from this buffer
     * @param _bits the number of bits to read from this buffer
     * @return a long containing the LSB-aligned bits that were read
     * @throws IndexOutOfBoundsException if _srcAddr is less than zero, or greater than or equal to this buffer's limit
     * @throws BufferUnderflowException if this buffer's capacity is insufficient to read _bits at _srcAddr
     */
    public long getBits( final BitAddress _srcAddress, final int _bits ) {

        // sanity check...
        if( (_bits < 0) || (_bits > 64) )
            throw new IllegalArgumentException( "Invalid number of bits: " + _bits );

        // get the requested bits...
        BitBuffer bb = get( _srcAddress, _bits );

        // get them into a long, MSB-aligned...
        long bits = 0;
        for( int i = 0; i < 8; i++ ) {
            bits <<= 8;
            if( i < bb.buffer.capacity() )
                bits |= 0xFF & bb.buffer.get( i );
        }

        // convert to LSB-aligned...
        bits >>>= 64 - _bits;

        // and we're outta here...
        return bits;
    }


    /**
     * Returns a long containing the remaining bits in this buffer.  The bits returned are LSB-aligned (i.e., the LSB of the bits read is in the LSB
     * of the returned value).  Unused bits will be zero.
     *
     * @return a long containing the LSB-aligned bits that were read
     */
    public long getBits() {
        return getBits( new BitAddress( position ), remaining() );
    }


    public long getLong( final BitAddress _srcAddress ) {
        return getBits( _srcAddress, 64 );
    }


    public long getLong() {
        return getBits( new BitAddress( position ), 64 );
    }


    public int getInt( final BitAddress _srcAddress ) {
        return (int) getBits( _srcAddress, 32 );
    }


    public int getInt() {
        return (int) getBits( new BitAddress( position ), 32 );
    }


    public short getShort( final BitAddress _srcAddress ) {
        return (short) getBits( _srcAddress, 16 );
    }


    public short getShort() {
        return (short) getBits( new BitAddress( position ), 16 );
    }


    public byte getByte( final BitAddress _srcAddress ) {
        return (byte) getBits( _srcAddress, 8 );
    }


    public byte getByte() {
        return (byte) getBits( new BitAddress( position ), 8 );
    }


    public boolean getBoolean( final BitAddress _srcAddress ) {
        return getBits( _srcAddress, 1 ) != 0;
    }


    public boolean getBoolean() {
        return getBits( new BitAddress( position ), 1 ) != 0;
    }


    /**
     * Copies the given number of bits from the give source buffer from the given source address to the given destination buffer at the given
     * destination address.
     *
     * @param _dstBuffer the buffer the bits will be written to
     * @param _dstAddr the address to write the first of the given bits to
     * @param _srcBuffer the buffer containing the bits to write
     * @param _srcAddr the address to read the first bit from
     * @param _bits the number of sequential bits to write
     * @throws IndexOutOfBoundsException if _dstAddr is less than zero, or greater than or equal to this buffer's limit, or if the _srcAddr
     *         is less than zero, or greater than or equal to the _srcBuffer's limit.
     * @throws BufferOverflowException if this buffer's capacity is insufficient to write _bits at _dstAddr
     * @throws BufferUnderflowException if _srcBuffer's capacity is insufficient to read _bits at _srcAddr
     */
    private void copyBits( final BitBuffer _dstBuffer, final BitAddress _dstAddr, final BitBuffer _srcBuffer, final BitAddress _srcAddr, final int _bits ) {

        // sanity checks...
        if( (_dstAddr == null) || (_srcBuffer == null) || (_srcAddr == null) )
            throw new IllegalArgumentException( "Required argument missing" );
        if( _bits < 1 )
            throw new IllegalArgumentException( "Invalid number of bits: " + _bits );
        if( (_dstAddr.addr < 0) || (_dstAddr.addr >= _dstBuffer.limit) )
            throw new IndexOutOfBoundsException( "Destination index invalid: " + _dstAddr );
        if( (_srcAddr.addr < 0) || (_srcAddr.addr > _srcBuffer.limit) )
            throw new IndexOutOfBoundsException( "Source index invalid: " + _dstAddr );
        if( (_dstAddr.addr + _bits > _dstBuffer.capacity) )
            throw new BufferOverflowException();
        if( (_srcAddr.addr + _bits > _srcBuffer.capacity) )
            throw new BufferUnderflowException();

        // looks like we have enough bits and enough room to put them, so copy them...
        BitAddress srcAddr = _srcAddr;
        BitAddress dstAddr = _dstAddr;
        int bitsLeft = _bits;

        // loop until there are no more bits left to copy...
        while( bitsLeft > 0 ) {

            // figure out how many bits we can fit in the current byte...
            int fitBits = Math.min( bitsLeft, Math.min( 8 - srcAddr.bitAddr, 8 - dstAddr.bitAddr ) );

            // if we can't fit at least one bit, then we've got a problem...
            if( fitBits < 1 )
                throw new IllegalStateException( "Can't set at least one bit - computed " + fitBits );

            // if we can move an entire byte, do it at the byte level...
            if( fitBits == 8 ) {
                _dstBuffer.buffer.put( dstAddr.byteAddr, _srcBuffer.buffer.get( srcAddr.byteAddr ) );
            }

            // otherwise, set the bits we can...
            else {
                int setMask = Integer.rotateLeft( BITS_MASK[fitBits], 8 - (dstAddr.bitAddr + fitBits) );
                int srcBits = Integer.rotateLeft( _srcBuffer.buffer.get( srcAddr.byteAddr ) & 0xFF, srcAddr.bitAddr - dstAddr.bitAddr );
                _dstBuffer.buffer.put( dstAddr.byteAddr, (byte) ((_dstBuffer.buffer.get( dstAddr.byteAddr ) & ~setMask) | (srcBits & setMask)) );
            }

            // update our source and destination positions, and bit count...
            bitsLeft -= fitBits;
            srcAddr = srcAddr.add( fitBits );
            dstAddr = dstAddr.add( fitBits );
        }
    }


    public void flip() {
        limit = position;
        position = 0;
    }


    public int position() {
        return position;
    }


    public void position( final int _position ) {
        if( (_position < 0) || (_position > capacity) )
            throw new IllegalArgumentException( "Position out of range: " + _position );
        position = _position;
    }


    public void adjustPosition( final int _adjustment ) {
        int newPosition = position + _adjustment;
        if( (newPosition < 0) || (newPosition > capacity) )
            throw new IllegalArgumentException( "Position adjustment out of range: " + _adjustment );
        position = newPosition;
    }


    public int limit() {
        return limit;
    }


    public void limit( final int _limit ) {
        if( (_limit < 0) || (_limit > capacity) )
            throw new IllegalArgumentException( "Limit out of range: " + _limit );
        limit = _limit;
    }


    public void adjustLimit( final int _adjustment ) {
        int newLimit = limit + _adjustment;
        if( (newLimit < 0) || (newLimit > capacity) )
            throw new IllegalArgumentException( "Limit adjustment out of range: " + _adjustment );
        limit = newLimit;
    }


    public int remaining() {
        if( limit < position )
            throw new IllegalStateException( "Limit is less than the position" );
        return limit - position;
    }


    public int capacity() {
        return capacity;
    }
}
