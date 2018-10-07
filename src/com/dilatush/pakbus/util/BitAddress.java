package com.dilatush.pakbus.util;

/**
 * Instances of this class represent the address of a particular bit within a bit buffer.  The address of the first bit in the buffer (the MSB) is
 * zero, the next bit is 1, etc.  Instances of this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BitAddress {

    public static final BitAddress ZERO = new BitAddress( 0 );

    final public int byteAddr;
    final public int bitAddr;
    final public int addr;


    /**
     * Creates a new instance of this class from the given bit address.
     *
     * @param _addr the bit address
     */
    public BitAddress( final int _addr ) {

        if( _addr < 0 )
            throw new IllegalArgumentException( "Address is out of range" );

        addr = _addr;
        bitAddr = _addr & 0x07;
        byteAddr = _addr >> 3;
    }

    /**
     * Creates a new instance of this class with the given byte and bit addresses.
     *
     * @param _byteAddr the byte offset (must be [0..0x10000000))
     * @param _bitAddr the bit offset (must be [0..7])
     */
    public BitAddress( final int _byteAddr, final int _bitAddr ) {

        if( ((_byteAddr & 0xF0000000) != 0) || ((_bitAddr & 0xFFFFFFF8) != 0) )
            throw new IllegalArgumentException( "Byte address or bit address is out of range" );

        byteAddr = _byteAddr;
        bitAddr = _bitAddr;
        addr = (_byteAddr << 3) | _bitAddr;
    }


    /**
     * Creates a new instance of this class that contains the result of this instance plus the given bit length.
     *
     * @param _bitLength bit length to add
     * @return the new instance of this class with the result of the addition
     */
    public BitAddress add( final int _bitLength ) {
        return new BitAddress( addr + _bitLength );
    }


    /**
     * Creates a new instance of this class that contains the result of this instance plus the given bit address.
     *
     * @param _bitAddress the bit address to add
     * @return the new instance of this class with the result of the addition
     */
    public BitAddress add( final BitAddress _bitAddress ) {
        return new BitAddress( addr + _bitAddress.addr );
    }


    /**
     * Creates a new instance of this class that contains the result of this instance minus the given bit length.
     *
     * @param _bitLength bit length to subtract
     * @return the new instance of this class with the result of the addition
     */
    public BitAddress sub( final int _bitLength ) {
        return new BitAddress( addr - _bitLength );
    }


    /**
     * Creates a new instance of this class that contains the result of this instance minus the given bit length.
     *
     * @param _bitAddress the bit address to subtract
     * @return the new instance of this class with the result of the addition
     */
    public BitAddress sub( final BitAddress _bitAddress ) {
        return new BitAddress( addr - _bitAddress.addr );
    }


    /**
     * Returns true if this address is on an even byte boundary (i.e., the bit address is 0).
     *
     * @returntrue if this address is on an even byte boundary
     */
    public boolean isEven() {
        return bitAddr == 0;
    }


    public String toString() {
        return "address: " + addr + " (byte " + byteAddr + ", bit " + bitAddr + ")";
    }
}
