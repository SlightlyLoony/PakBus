package com.dilatush.pakbus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static com.dilatush.pakbus.PakBusBaseDataType.*;
import static com.dilatush.pakbus.PakBusDataType.*;

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
    private int bitLength;          // number of bits in the entire message (computed upon finalization)...


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
     * Returns a buffer containing the bytes for the given field name, containing exactly the number of bytes required to hold the field data, and
     * with the position at 0 and limit at the number of bytes.  If the field has an uneven number of bytes, the value will be LSB-justified, so that
     * any unused bits are the MSBs.  For example, a 12 bit value will be stored in two bytes as 0000xxxx xxxxxxxx.  This method is the base for all
     * other getters, all of which wrap this method with a bytes-to-something converstion.
     *
     * @param _fieldName the name of the field whose value is to be retrieved.
     * @return the buffer containing the bytes for the retrieved field.
     */
    public ByteBuffer get( final String _fieldName ) {

        // if this message is empty, invalid, or not finalized, then getting a field is disallowed...
        if( !valid || !initialized || !finalized )
            throw new IllegalStateException( "Attempted to get a field from a message that is invalid, uninitialized, or not finalized" );

        // some setup...
        MessageFieldDefinition def = definition.get( _fieldName );
        if( def == null )
            throw new IllegalArgumentException( "Field name does not exist in this message: " + _fieldName );
        FieldInfo info = fieldInfo.get( def.getName() );
        if( info == null )
            throw new IllegalStateException( "Field information for " + def.getName() + " is missing" );

        // figure out how many bytes we need and get our buffer...
        int bytesNeeded = (info.length +  7) >>> 3;
        ByteBuffer result = ByteBuffer.allocate( bytesNeeded );

        // some setup for the byte copying...
        Offset srcPos = getBitOffset( _fieldName );
        Offset dstPos = new Offset( 0, info.length & 7 );

        // if we have an even number of bytes, and the source and destination positions are on byte boundaries, just copy bytes...
        if( ((info.length & 7) == 0) && (srcPos.bitOffset == 0) && (dstPos.bitOffset == 0) ) {

            // do a simple byte copy...
            int oldPos = bytes.position();
            int oldLim = bytes.limit();
            bytes.position( srcPos.byteOffset );
            bytes.limit( srcPos.byteOffset + bytesNeeded );
            result.position( dstPos.byteOffset );
            result.put( bytes );
            result.flip();
            bytes.limit( oldLim );
            bytes.position( oldPos );
        }

        // otherwise, we do things the harder way...
        else {

            // now we copy the bytes...
            copyBits( bytes, srcPos, result, dstPos, info.length );
        }

        // if this is a little-endian field, flip the bytes...
        checkForLittleEndian( def, result, bytesNeeded );

        // and we're done...
        return result;
    }


    public String getString( final String _fieldName, final Charset _charset ) {

        // some setup and sanity checking...
        if( (_fieldName == null) || (_fieldName.length() == 0) || (_charset == null) )
            throw new IllegalArgumentException( "Required argument is missing" );
        MessageFieldDefinition def = definition.get( _fieldName );
        if( def == null )
            throw new IllegalArgumentException( "Field " + _fieldName + " is not present in this message" );
        if( !((def.getPakbusType() == ASCIIZ) || (def.getPakbusType() == ASCII)) )
            throw new IllegalArgumentException( "Field " + _fieldName + " is not a string" );

        // first we get the bytes from this field...
        ByteBuffer fieldBytes = get( _fieldName );

        // if this is a zero-terminated string, cut off the terminator...
        if( def.getPakbusType() == ASCIIZ )
            fieldBytes.limit( fieldBytes.limit() - 1 );

        // now we're all ready to go...
        fieldBytes.position( 0 );
        byte[] coded = new byte[fieldBytes.limit()];
        fieldBytes.get( coded );
        return new String( coded, _charset );
    }


    public byte[] getBytes( final String _fieldName ) {

        // some setup and sanity checking...
        if( (_fieldName == null) || (_fieldName.length() == 0) )
            throw new IllegalArgumentException( "Required argument is missing" );
        MessageFieldDefinition def = definition.get( _fieldName );
        if( def == null )
            throw new IllegalArgumentException( "Field " + _fieldName + " is not present in this message" );
        if( !((def.getPakbusType() == Bytes) || (def.getPakbusType() == BytesZ)) )
            throw new IllegalArgumentException( "Field " + _fieldName + " is not a byte array field" );

        // first we get the bytes from this field...
        ByteBuffer fieldBytes = get( _fieldName );

        // if this is a zero-terminated array, cut off the terminator...
        if( def.getPakbusType() == BytesZ )
            fieldBytes.limit( fieldBytes.limit() - 1 );

        // now we're all ready to go...
        fieldBytes.position( 0 );
        byte[] byteArray = new byte[fieldBytes.limit()];
        fieldBytes.get( byteArray );
        return byteArray;
    }


    public byte getByte( final String _fieldName ) {
        return (byte) getInteger( _fieldName, 8 );
    }


    public short getShort( final String _fieldName ) {
        return (short) getInteger( _fieldName, 16 );
    }


    public int getInt( final String _fieldName ) {
        return (int) getInteger( _fieldName, 32 );
    }


    public long getLong( final String _fieldName ) {
        return getInteger( _fieldName, 64 );
    }


    public boolean getBoolean( final String _fieldName ) {
        return getInteger( _fieldName, 64 ) != 0;
    }


    /**
     * Extracts an integer type from the field with the given name.  "SB" is the number of bits in the message field, and "RB" is the number of bits
     * requested (the _bits argument).  If SB > RB, throws an IllegalArgumentException.  SB == RB, returns exactly the value extracted.  If SB < RB,
     * and the field type is a signed integer, returns a value with the sign bit extended.  Otherwise, returns the value without sign extension.
     *
     * @param _fieldName the name of the field to be extracted
     * @param _bits the number of significant bits in the result
     * @return the extracted integer type
     */
    private long getInteger( final String _fieldName, final int _bits ) {

        // some setup and sanity checking...
        if( (_fieldName == null) || (_fieldName.length() == 0) )
            throw new IllegalArgumentException( "Required field name is missing" );
        MessageFieldDefinition def = definition.get( _fieldName );
        FieldInfo info = fieldInfo.get( _fieldName );
        if( _bits < info.length )
            throw new IllegalArgumentException( "Requested fewer bits (" + _bits + ") than the field size (" + info.length + ")" );

        // first we get the bytes from this field...
        ByteBuffer fieldBytes = get( _fieldName );

        // get our raw integer...
        long result;
        switch( fieldBytes.limit() ) {
            case 1: result = fieldBytes.get() & 0xFF; break;
            case 2: result = fieldBytes.getShort() & 0xFFFF; break;
            case 4: result = fieldBytes.getInt() & 0xFFFFFFFFL; break;
            case 8: result = fieldBytes.getLong(); break;
            default: throw new IllegalStateException( "Attempted to extract integer from field whose stored length ("
                    + fieldBytes.limit() + ") is not 1, 2, 4, or 8 bytes" );
        }

        // at this point we have our return value in unsigned format; if it's supposed to be signed we need to fix that...
        if( def.getPakbusType().getBase() == SignedInteger ) {

            // we use the left shift/right shift trick to extend the sign...
            result <<= 64 - info.length;
            result >>= 64 - info.length;
        }

        // and we're done...
        return result;
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

        // if we have a little-endian field, we need to flip the bytes...
        checkForLittleEndian( def, _buffer, bytesNeeded );

        // now we do the actual setting...
        Offset srcPos = new Offset( 0, 8 * _buffer.limit() - def.getBits() );
        copyBits( _buffer, srcPos, bytes, dstPos, def.getBits() );

        // show that we've set this field...
        FieldInfo info = fieldInfo.get( _fieldName );
        info.set = true;

        // some housekeeping...
        initialized = true;
        checkValid();
    }


    /**
     * If we're getting or setting a little-endian field, then this method flips the byte order.  Otherwise, it does nothing.
     *
     * @param _def the field definition for the field we're working on
     * @param _dst the buffer containing the destination bytes
     * @param _bytes the number of bytes
     */
    private void checkForLittleEndian( final MessageFieldDefinition _def, final ByteBuffer _dst, final int _bytes ) {

        // if this field is little-endian, we have work to do...
        if( _def.getPakbusType().getOrder() == ByteOrder.LITTLE_ENDIAN ) {

            // make sure we have an even number of bytes...
            if( (_def.getBits() & 7) != 0 )
                throw new IllegalStateException( "Field with a length that is an uneven number of bytes was marked Little-Endian" );

            // ok, now flip the bytes...
            int fs = 0;
            int fe = _bytes - 1;
            while( fs < fe ) {
                byte b = _dst.get( fs );
                _dst.put( fs, _dst.get( fe ) );
                _dst.put( fe, b );
                fs++;
                fe--;
            }
        }
    }


    /**
     * Copies the given number of bits from the given source buffer at the given position to the give destination buffer at the given position.
     *
     * @param _src the source buffer
     * @param _srcPos the source position
     * @param _dst the destination buffer
     * @param _dstPos the destination position
     * @param _bits the number of bits to copy
     */
    private void copyBits( final ByteBuffer _src, Offset _srcPos, final ByteBuffer _dst, final Offset _dstPos, final int _bits ) {

        int remainingBits = _bits;
        Offset srcPos = _srcPos;
        Offset dstPos = _dstPos;


        while( remainingBits > 0 ) {

            // figure out how many bits we can fit in the current byte...
            int fitBits = Math.min( remainingBits, Math.min( 8 - srcPos.bitOffset, 8 - dstPos.bitOffset ) );

            // if we can't fit at least one bit, then we've got a problem...
            if( fitBits < 1 )
                throw new IllegalStateException( "Can't set at least one bit - computed " + fitBits );

            // set the bits...
            int setMask = Integer.rotateLeft( BITS_MASK[fitBits], 8 - (dstPos.bitOffset + fitBits) );
            int srcBits = Integer.rotateLeft( _src.get( srcPos.byteOffset ) & 0xFF, srcPos.bitOffset - dstPos.bitOffset );
            _dst.put( dstPos.byteOffset, (byte)( (_dst.get( dstPos.byteOffset ) & ~setMask) | (srcBits & setMask) ) );

            // update our source and destination positions, and bit count...
            remainingBits -= fitBits;
            srcPos = srcPos.add( fitBits );
            dstPos = dstPos.add( fitBits );
        }
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
     * Sets the value of the field with the given name to the given byte value.  The field's type must be a byte array type.
     *
     * @param _fieldName the field name whose value is to be set
     * @param _bytes the bytes containing the value to set
     */
    public void set( final String _fieldName, final byte[] _bytes ) {

        // sanity checks...
        if( (_fieldName == null) || (_fieldName.length() == 0) || (_bytes == null) )
            throw new IllegalArgumentException( "Required argument is missing" );
        MessageFieldDefinition def = definition.get( _fieldName );
        if( def == null )
            throw new IllegalArgumentException( "Field name does not exist in this message: " + _fieldName );

        // we have two possibly correct field data types here: Bytes and BytesZ.  The first must be the last field in a message; the second
        // may appear anywhere...
        PakBusDataType type = def.getPakbusType();
        boolean isLast = (def == definition.get( definition.size() - 1 ));
        if( (type != Bytes) && (type != BytesZ) )
            throw new IllegalArgumentException( "Attempting to store bytes to a field whose type is "
                    + type + " instead of the required Bytes or BytesZ" );
        if( (type == Bytes) && !isLast )
            throw new IllegalArgumentException( "Attempting to store bytes to a Bytes field that is not the last field of the message" );

        // all is ok, so make a buffer to hold our bytes and then set them...
        ByteBuffer buffer = ByteBuffer.allocate( _bytes.length + 1 );  // we're leaving room for the possible terminator...
        buffer.put( _bytes );
        if( type == BytesZ )
            buffer.put( (byte) 0 );
        buffer.flip();

        // now handle it through the base setter...
        set( _fieldName, buffer );
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
        if( def.getPakbusType() != ASCIIZ )
            throw new IllegalArgumentException( "Attempting to store variable length string to a field whose type is "
                    + def.getPakbusType() + " instead of the required ASCIIZ" );

        // first get the encoded bytes...
        byte[] bytes = _string.getBytes( _charset );

        // then put them in a buffer and set the value...
        ByteBuffer buffer = ByteBuffer.allocate( bytes.length + 1 );  // we're adding room for the terminating zero...
        buffer.put( bytes );
        buffer.put( (byte) 0 );  // the terminator...
        buffer.flip();

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
     */
    public void finalizeMessage() {

        // first we set the MsgType field, and if it hasn't been set already, the TranNbr field...
        set( "MsgType", type.getCode() );   // we set this one by default...
        FieldInfo info = fieldInfo.get( "TranNbr" );
        if( (info == null) || (!info.set) )
            set( "TranNbr", 0 );

        // if the message is still not valid, then we have a problem...
        if( !valid )
            throw new IllegalStateException( "Attempted to get bytes from an invalid message" );

        // now we find the last byte of our message...
        bitLength = 0;
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

        // mark this as finalized...
        finalized = true;
    }


    /**
     * Returns the encoded bytes for this message.  This method will result in an error for empty, involid, or non-finalized messages.
     *
     * @return the buffer containing this message's encoding
     */
    public ByteBuffer encoding() {

        if( !valid || !initialized || !finalized )
            throw new IllegalStateException( "Attempted to get encoding for message that is empty, invalid, or not finalized" );

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

        Message msg;

        msg = new Message( MessageType.DeliveryFailure );
        msg.set( "ErrCode", 0x55 );
        msg.set( "HiProtoCode", 0xf );
        msg.set( "DstPBAddr", 0x123 );
        msg.set( "HopCnt", 3 );
        msg.set( "SrcPBAddr", 0x456 );
        msg.set( "MsgData", new byte[] {9,8,7,6,5,4,3,2,1,0} );
        msg.finalizeMessage();
        byte mt = msg.getByte( "MsgType" );
        int addr = msg.getInt( "DstPBAddr" );
        byte[] ba = msg.getBytes( "MsgData" );

        msg = new Message( MessageType.GetSettingsReq );
        msg.set( "NameList", "os.version", Charset.forName( "US-ASCII" ) );
        msg.finalizeMessage();
        String osv = msg.getString( "NameList", Charset.forName( "US-ASCII" ) );

        msg = new Message( MessageType.HelloTrnsRsp );
        msg.set( "IsRouter", false );
        msg.set( "HopMetric", 4 );
        msg.set( "VerifyIntv", 100 );
        msg.finalizeMessage();
        boolean ir = msg.getBoolean( "IsRouter" );

        msg.hashCode();
    }
}
