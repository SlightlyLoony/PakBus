package com.dilatush.pakbus.comms;

import java.nio.ByteBuffer;

/**
 * Instances of this class contain the bytes of a valid PakBus packet sequence, along with the signature that verified them.  The first byte is the
 * first byte of the PakBus header; the last byte is the last byte of the signature nullifier.  None of the bytes are quoted.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class RawPacket {

    final public ByteBuffer packetBytes;
    final public Signature packetSignature;


    /**
     * Creates a new instance of this class with the given bytes and signature.  This constructor should be used when the bytes were received and the
     * signature verified.
     *
     * @param _packetBytes the bytes of the packet <i>including</i> the signature nullifier, with the limit at the byte after the last packet byte
     * @param _packetSignature the signature of the packet
     */
    public RawPacket( final ByteBuffer _packetBytes, final Signature _packetSignature ) {

        // sanity checks...
        if( (_packetBytes == null) || (_packetSignature == null) )
            throw new IllegalArgumentException( "Missing required argument" );

        packetBytes = _packetBytes.asReadOnlyBuffer();
        packetBytes.position( 0 );
        packetSignature = _packetSignature;
    }


    /**
     * Constructs a new instance of this class from the given bytes.  This constructor should be used when the bytes are being transmitted and the
     * signature nullifier needs to be added.
     *
     * @param _packetBytes the bytes of the packet <i>excluding</i> the signature nullifier, with the limit at the byte after the last packet byte
     */
    public RawPacket( final ByteBuffer _packetBytes ) {

        // sanity checks...
        if( (_packetBytes == null) )
            throw new IllegalArgumentException( "Missing required argument" );

        // we need to generate a signature nullifier; if we don't have enough capacity we need to copy the buffer to a bigger one...
        ByteBuffer buffer = _packetBytes;
        if( buffer.capacity() < (buffer.limit() + 2) ) {
            buffer = ByteBuffer.allocate( _packetBytes.limit() + 2 );
            _packetBytes.position( 0 );
            buffer.put( _packetBytes );
            buffer.limit( _packetBytes.limit() );
        }

        // generate the signature nullifier...
        buffer.position( 0 );
        packetSignature = new Signature( buffer );
        buffer.position( buffer.limit() );
        buffer.limit( _packetBytes.limit() + 2 );
        buffer.putShort( (short) packetSignature.getNullifier() );
        buffer.position( 0 );
        packetBytes = buffer.asReadOnlyBuffer();
    }
}
