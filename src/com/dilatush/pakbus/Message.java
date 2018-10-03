package com.dilatush.pakbus;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static com.dilatush.pakbus.PakBusBaseDataType.*;

/**
 * Instances of this class represent a PakCtrl or BMP5 message.  There are no individual classes for each of the many message types, which have
 * little in common with each other and so do not form a natural hierarchy.  Instead, instances of this class have a message definition that contains
 * type definitions for every field in the message, along with mappings between Java types, message types, and the location of the data within the
 * message.  Accessors and mutators are provided for all the Java types used.  Note also that there are two special message types, not defined by
 * Campbell Scientific: INVALID and EMPTY.  Message instances are invalid if a given byte buffer could not be decoded, or if all the required fields
 * were not set by a mutator.  Message instances are empty if a byte buffer to be decoded was empty, or if a message was created but no fields were
 * set by a mutator.  Instances of this class are mutable and not threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Message {

    private static final int MAX_MESSAGE_SIZE = 1000;
    private static final int BITS_MASK[] = { 0x00, 0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF };

    private final MessageType type;
    private final MessageDefinition definition;
    private final ByteBuffer bytes;
    private final Map<String,FieldInfo> fieldInfo;

    private boolean finalized;      // true if getBytes() has been called and the byte length has been set...
    private boolean initialized;    // true if a byte buffer has been decoded or at least one field has been set...
    private boolean valid;          // true if a byte buffer has been successfully decoded, or if all non-optional fields have been set...


    public Message( final MessageType _type ) {

        type = _type;
        definition = MessageDefinitions.INSTANCE.getDefinition( _type );
        if( definition == null )
            throw new IllegalArgumentException( "Message type " + _type + " is not defined" );
        bytes = ByteBuffer.allocate( MAX_MESSAGE_SIZE );
        initialized = false;
        valid = false;
        finalized = false;
        fieldInfo = new HashMap<>( 100 );
    }


    /**
     * Sets the given field name to the value in the given buffer.  The behavior of this method depends on the field type.  If the field type is a
     * fixed length type, then the buffer must contain (between position 0 and the limit) the exact number of bytes required to hold the value.  Thus,
     * for a 12 bit fixed length field, there must be two byte; for a 32 bit field there must be four bytes, etc.  The value must be LSB-justified,
     * so that any unused bits are the MSBs.  For example, a 12 bit value should be stored in two bytes as 0000xxxx xxxxxxxx.  If the field type is
     * a variable-length type (all of which are byte-aligned), then the buffer must contain all of the bytes of the value (between position 0 and the
     * limit) and no more.  This method is the base for all other setters, all of which wrap this method with a something-to-buffer conversion.
     *
     * @param _fieldName the field name whose value is to be set
     * @param _buffer the buffer containing the bytes to be set
     */
    public void set( final String _fieldName,  final ByteBuffer _buffer ) {

        // if this message has been finalized, then setting a field is disallowed...
        if( finalized )
            throw new IllegalStateException( "Attempt made to set a field on a finalized message" );

        // some setup...
        MessageFieldDefinition def = definition.get( _fieldName );
        if( def == null )
            throw new IllegalArgumentException( "Field name does not exist in this message: " + _fieldName );

        // get the offset to this field's MSB...
        Offset dstPos = getBitOffset( _fieldName );
        Offset dstEnd = dstPos.add( def.getBits() );

        // if this is a variable length field, we handle it specially...
        if( def.getBits() == 0 ) {

            // all the variable-length types are byte-aligned, so if we're not at an even byte position we've got a problem...
            if( dstPos.bitOffset != 0 )
                throw new IllegalStateException( "Attempted to set a variable length field when field start is not byte-aligned: " + dstPos );

            // stuff our bytes away...
            bytes.position( dstPos.byteOffset );
            _buffer.position( 0 );
            bytes.put( _buffer );

            // update our length information, now that we know it, and show that we're set...
            FieldInfo info = fieldInfo.get( _fieldName );
            info.length = 8 * _buffer.limit();
            info.set = true;

            // some housekeeping...
            initialized = true;
            checkValid();

            // and we're done...
            return;
        }

        // if we get here, we've got a fixed length field...
        // make sure we've got the correct number of bytes...
        int bytesNeeded = (def.getBits() + 7) >>> 3;
        if( bytesNeeded != _buffer.limit() )
            throw new IllegalArgumentException( "Given buffer has wrong number of bytes - expected " + bytesNeeded + ", got " + _buffer.limit() );

        // now we do the actual setting...
        Offset srcPos = new Offset( 0, 8 * _buffer.limit() - def.getBits() );
        int bitsRemaining = def.getBits();
        while( bitsRemaining > 0 ) {

            // figure out how many bits we can fit in the current byte...
            int fitBits = Math.min( bitsRemaining, Math.min( 8 - srcPos.bitOffset, 8 - dstPos.bitOffset ) );

            // if we can't fit at least one bit, then we've got a problem...
            if( fitBits < 1 )
                throw new IllegalStateException( "Can't set at least one bit - computed " + fitBits );

            // set the bits...
            int setMask = Integer.rotateLeft( BITS_MASK[fitBits], 8 - (dstPos.bitOffset + fitBits) );
            int srcBits = Integer.rotateLeft( _buffer.get( srcPos.byteOffset ), srcPos.bitOffset - dstPos.bitOffset );
            bytes.put( dstPos.byteOffset, (byte)( (bytes.get( dstPos.byteOffset ) & ~setMask) | (srcBits & setMask) ) );

            // update our source and destination positions, and bit count...
            bitsRemaining -= fitBits;
            srcPos = srcPos.add( fitBits );
            dstPos = dstPos.add( fitBits );
        }

        // show that we've set this field...
        FieldInfo info = fieldInfo.get( _fieldName );
        info.set = true;

        // some housekeeping...
        initialized = true;
        checkValid();
    }


    public void set( final String _fieldName, final byte _value ) {
        set( _fieldName, 8, _value );
    }


    public void set( final String _fieldName, final short _value ) {
        set( _fieldName, 16, _value );
    }


    public void set( final String _fieldName, final int _value ) {
        set( _fieldName, 32, _value );
    }


    public void set( final String _fieldName, final long _value ) {
        set( _fieldName, 64, _value );
    }


    public void set( final String _fieldName, final boolean _state ) {
        set( _fieldName, _state ? 1L : 0L );
    }


    private void set( final String _fieldName, final int _bits, final long _value ) {

        // some setup and sanity checking...
        MessageFieldDefinition def = definition.get( _fieldName );
        if( def == null )
            throw new IllegalArgumentException( "Field name does not exist in this message: " + _fieldName );

        // make sure we're setting a fixed-length integer field and that we've got enough bits...
        PakBusBaseDataType base = def.getPakbusType().getBase();
        if( !((base == SignedInteger) || (base == UnsignedInteger) || (base == Boolean) ))
            throw new IllegalArgumentException( "Attempting to set non-integer type (" + def.getPakbusType().getBase() + ") to an integer" );
        if( def.getBits() == 0 )
            throw new IllegalArgumentException( "Attempted to set variable-length field with a " + _bits + " bit integer" );
        if( def.getBits() > _bits )
            throw new IllegalArgumentException( "Attempted to set a " + def.getBits() + " bit value with a " + _bits + " bit integer" );

        // figure out how many bytes we need...
        int bytesNeeded = (def.getBits() + 7) >>> 3;

        // put them in a buffer...
        ByteBuffer bytes = ByteBuffer.allocate( bytesNeeded );
        for( int i = 0; i < bytesNeeded; i++ ) {
            bytes.put( bytesNeeded - (i + 1), (byte)(_value >>> (i * 8)) );
        }

        // now handle it through the base setter...
        set( _fieldName, bytes );
    }


    /**
     * Sets the value of the field with the given name to the given string value, encoded with the given character set, as a zero-terminated
     * variable length string.
     *
     * @param _fieldName the field name whose value is to be set
     * @param _string the string to store as a zero terminated, variable length string
     * @param _charset the Charset to use when encoding (generally either "US-ASCII" or "UTF-8")
     */
    public void set( final String _fieldName, final String _string, final Charset _charset ) {

        // sanity checks...
        if( (_fieldName == null) || (_fieldName.length() == 0) || (_string == null) || (_charset == null) )
            throw new IllegalArgumentException( "Required argument is missing" );
        MessageFieldDefinition def = definition.get( _fieldName );
        if( def == null )
            throw new IllegalArgumentException( "Field name does not exist in this message: " + _fieldName );
        if( def.getPakbusType() != PakBusDataType.ASCIIZ )
            throw new IllegalArgumentException( "Attempting to store variable length string to a field whose type is "
                    + def.getPakbusType() + " instead of the required ASCIIZ" );

        // first get the encoded bytes...
        byte[] bytes = _string.getBytes( _charset );

        // then put them in a buffer and set the value...
        ByteBuffer buffer = ByteBuffer.allocate( bytes.length + 1 );  // we're adding room for the terminating zero...
        buffer.put( bytes );
        buffer.put( (byte) 0 );  // the terminator...

        // now handle it through the base setter...
        set( _fieldName, buffer );
    }


    /**
     * Return the type of this message. If this message has been initialized (by decoding a byte buffer, or by using the setters) then the type
     * returned will be the type this instance was constructed with.  However, if this message has not been initialized, a type of EMPTY will be
     * returned, and it is initialized but not valid then a type of INVALID will be returned.
     *
     * @return the type of this message.
     */
    public MessageType type() {

        // return a value that depends on our current state...
        if( !initialized ) return MessageType.EMPTY;
        if( !valid ) return MessageType.INVALID;
        return type;
    }


    /**
     * This method should only be called after all fields have been set.  Invoking this method computes the final length of the encoded message and
     * marks the message as finalized.  No further mutations (field settings) are allowed after this.  Returns a buffer containing the bytes of the
     * encoded message, with the position set to zero and the limit and capacity set to the number of bytes in the encoded message.
     *
     * @return the buffer containing the message's bytes
     */
    public ByteBuffer getBytes() {

        // first we set the MsgType field, and if it hasn't been set already, the TranNbr field...
        set( "MsgType", type.getCode() );   // we set this one by default...
        FieldInfo info = fieldInfo.get( "TranNbr" );
        if( (info == null) || (!info.set) )
            set( "TranNbr", 0 );

        // if the message is still not valid, then we have a problem...
        if( !valid )
            throw new IllegalStateException( "Attempted to get bytes from an invalid message" );

        // now we find the last byte of our message...
        int bitLength = 0;
        for( int i = 0; i < definition.size(); i++ ) {

            // get the definition and info...
            MessageFieldDefinition def = definition.get( i );
            info = fieldInfo.get( def.getName() );

            // if it's set, accumulate the length...
            if( (info != null) && (info.set) )
                bitLength += info.length;
        }

        // make sure we ended on an even byte boundary...
        if( (bitLength & 7) != 0 )
            throw new IllegalStateException( "Attempted to get bytes from a message that has an uneven length" );

        // calculate the bytes needed, and return with a sweet little buffer full of message bytes...
        int bytesNeeded = bitLength >>> 3;
        ByteBuffer result = ByteBuffer.allocate( bytesNeeded );
        bytes.position( 0 );
        bytes.limit( bytesNeeded );
        result.put( bytes );
        result.flip();
        return result;
    }


    /**
     * Checks to see if all required fields are set, and if so, sets the valid flag...
     */
    private void checkValid() {

        for( int i = 0; i < definition.size(); i++ ) {

            // get the next field (in order)...
            MessageFieldDefinition def = definition.get( i );

            // if it's not required, then continue...
            if( def.isOptional() )
                continue;

            // if it IS required, but not set, then we're done...
            FieldInfo info = fieldInfo.get( def.getName() );
            if( (info == null) || (!info.set && !def.isOptional()) )
                return;
        }

        // if we get here, then all required fields have been set...
        valid = true;
    }


    /**
     * Returns the offset to the MSB of the field with the given name.  If the offset is not computable because a field laid out before
     * the requested field is of variable length and has not been set, then an IllegalStateException is thrown.  If the given field name does not
     * exist in this message, an IllegalArgumentException is thrown.
     *
     * @param _fieldName  the name of the field for which to compute an offset
     * @return the offset to the MSB of the field with the given name
     */
    private Offset getBitOffset( final String _fieldName ) {

        if( _fieldName == null )
            throw new IllegalArgumentException( "Field name argument is missing" );

        // first we check to see if we've already computed the offset...
        FieldInfo info = fieldInfo.get( _fieldName );
        if( info != null )
            return info.offset;


        // looks like we're going to have to do it the hard way...
        Offset result = new Offset();
        for( int i = 0; i < definition.size(); i++ ) {

            // some prep...
            MessageFieldDefinition def = definition.get( i );
            info = fieldInfo.get( def.getName() );

            // if we don't already have it, save the field info, since we've gone to the trouble of computing it...
            if( info == null ) {
                info = new FieldInfo( false, def.getBits(), result );
                fieldInfo.put( def.getName(), info );
            }

            // if we've found this field, then we're done...
            if( _fieldName.equals( def.getName() ) )
                return result;

            // if this is a fixed length field, add its offset and carry on...
            if( def.getBits() > 0 ) {
                result = result.add( def.getBits() );
                continue;
            }

            // this is a variable length field, so let's see if it's been set...
            if( info.length <= 0 )
                // we can't know the offset, 'cause we've got an unset variable length field...
                throw new IllegalStateException( "Tried to get offset of field after a variable length field that has not been set" );

            // we know the actual length, so add its offset and carry on...
            result = result.add( info.length );
        }

        // we can only get here if we never found the field name argument, so barf...
        throw new IllegalArgumentException( "Field name does not exist in this message type: " + _fieldName );
    }


    public boolean isInitialized() {
        return initialized;
    }


    public boolean isValid() {
        return valid;
    }


    private static class Offset {
        int byteOffset;
        int bitOffset;

        Offset() {
            byteOffset = 0;
            bitOffset = 0;
        }


        public Offset( final int _byteOffset, final int _bitOffset ) {
            byteOffset = _byteOffset;
            bitOffset = _bitOffset;
        }


        Offset add( final int _bitLength ) {
            int newBits = bitOffset + _bitLength;
            int byteOff = byteOffset + (newBits >>> 3);
            int bitOff = newBits & 0x7;
            return new Offset( byteOff, bitOff );
        }


        public String toString() {
            return "byte " + byteOffset + ", bit " + bitOffset;
        }
    }


    private static class FieldInfo {
        boolean set;   // true if the field has been set...
        int length;    // actual bit length of field...
        Offset offset;    // actual offset to MSB...


        public FieldInfo( final boolean _set, final int _length, final Offset _offset ) {
            set = _set;
            length = _length;
            offset = _offset;
        }
    }


    public static void main( String[] _args ) {

        // create a test message...
        Message msg = new Message( MessageType.GetSettingsReq );
        msg.set( "NameList", "os.version", Charset.forName( "US-ASCII" ) );
        ByteBuffer bytes = msg.getBytes();

        msg = new Message( MessageType.HelloTrnsRsp );
        msg.set( "IsRouter", false );
        msg.set( "HopMetric", 4 );
        msg.set( "VerifyIntv", 100 );
        bytes = msg.getBytes();

        msg.hashCode();
    }
}
