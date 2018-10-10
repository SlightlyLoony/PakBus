package com.dilatush.pakbus;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Represents a "signature" in a PakBus packet.  This is a sort of checksum over the bytes in a PakBus packet, using an algorithm that is unique in
 * my own experience.  The generated signature is two bytes long.  A method for generating a "signature nullifier" is also provided.  The nullifier
 * is a two-byte value which, if appended to the packet's bytes, would result in a signature of zero.  Instances of this class are immutable and
 * threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Signature {

    private final static int SEED = 0xAAAA;  // the value assumed by Campbell Scientific...

    private final int signature;
    private final int nullifier;


    /**
     * Creates a new instance of this class using the given bytes, which must be all the bytes in a PakBus packet BEFORE quoting and not
     * including the signature nullifier.  The given ByteBuffer is assumed to contain the bytes from position zero through the limit.
     *
     * @param _bytes The bytes to compute a signature (and nullifier) for.
     */
    public Signature( final ByteBuffer _bytes ) {

        // sanity check...
        if( (_bytes == null) || (_bytes.limit() == 0) )
            throw new IllegalArgumentException( "Packet bytes are missing" );

        // compute the signature of our given bytes...
        _bytes.position( 0 );
        int running = SEED;
        while( _bytes.hasRemaining() ) {
            running = forByte( _bytes.get(), running );
        }
        _bytes.flip();
        signature = running;

        // now compute our nullifier...
        nullifier = computeNullifier();
    }


    // computes the new value of the signature after adding the given byte...
    // this is adapted from code supplied by Campbell Scientific; modified mainly for clarity...
    private int forByte( final byte _byte, final int _current ) {

        int u = _current >>> 8;
        int l = _current & 0xFF;
        int b = _byte & 0xFF;

        int result = l << 1;
        if( result >= 0x100 )
            result++;

        result += u;
        result += b;
        result &= 0xFF;
        result |= (l << 8 );

        return result;
    }


    // computes the value of the nullifier for the signature we have...
    // this is adapted from code supplied by Campbell Scientific; modified mainly for clarity...
    private int computeNullifier() {

        byte msb = computeToZeroByte( signature );
        int newSig = forByte( msb, signature );
        byte lsb = computeToZeroByte( newSig );
        return ((msb & 0xFF) << 8) | (lsb & 0xFF);
    }


    // compute value of an additional byte that will make the least significant byte of the new signature zero...
    private byte computeToZeroByte( final int _current ) {

        int u = _current >>> 8;
        int l = _current & 0xFF;

        int x = l << 1;
        if( x >= 0x100 )
            x++;
        return (byte)(0x100 - (x + u));
    }


    public int getSignature() {
        return signature;
    }


    public int getNullifier() {
        return nullifier;
    }


    @Override
    public boolean equals( final Object _o ) {
        if( this == _o ) return true;
        if( _o == null || getClass() != _o.getClass() ) return false;
        Signature signature1 = (Signature) _o;
        return signature == signature1.signature &&
                nullifier == signature1.nullifier;
    }


    @Override
    public int hashCode() {
        return Objects.hash( signature, nullifier );
    }
}
