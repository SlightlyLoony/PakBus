package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.ArrayDataType;
import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.util.BitBuffer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Instances of this class represent an array datum, containing an array of datum elements of identical type.  There are three different kinds of
 * arrays, differing only in how their length is determined.  Fixed-length arrays have a fixed number of elements.  Zero-terminated arrays have a
 * variable number of elements, and are terminated by a zero value in the terminating type.  Data-bound arrays are variable in length, and the number
 * elements is determined by the length of the data they're decoded from.  Instances of this class are mutable and NOT threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ArrayDatum extends ADatum {

    private final List<Datum> array;
    private final ArrayDataType arrayType;
    private final Datum terminator;

    public ArrayDatum( final DataType _type ) {
        super( _type );

        // sanity check...
        if( !(_type instanceof ArrayDataType) )
            throw new IllegalArgumentException( "Attempted to create an ArrayDatum from a different data type: " + _type );

        arrayType = (ArrayDataType) type;

        // if we have a fixed-length array, we'll pre-populate it with datums of the right type...
        if( arrayType.isFixedLength() ) {
            array = new ArrayList<>( arrayType.getLength() );
            for( int i = 0; i < arrayType.getLength(); i++ ) {
                Datum datum = Datum.from( arrayType.getItemType() );
                array.add( datum );
            }
            terminator = null;
            return;
        }

        // otherwise, we have a variable-length array, so we just make our list for now...
        array = new ArrayList<>();

        // set our terminator, if we have one...
        if( arrayType.getTerminatorType() != null ) {
            terminator = new SimpleDatum( arrayType.getTerminatorType() );
            terminator.setTo( 0 );
        }
        else
            terminator = null;
    }


    /**
     * Adds the given datum as a new element on the end of this array.
     *
     * @param _element the datum to add
     */
    public void add( final Datum _element ) {

        // sanity check...
        if( _element == null )
            throw new IllegalArgumentException( "Required datum argument missing" );
        if( arrayType.isFixedLength() )
            throw new IllegalStateException( "Attempted to add element to fixed-length array datum" );
        if( _element.type() != arrayType.getItemType() )
            throw new IllegalArgumentException( "Attempted to add a " + _element.type() + " datum to a " + arrayType.getItemType() + " array" );

        // all is ok, so do it...
        array.add( _element );
    }


    /**
     * Adds a new datum of the correct type as the last element of this array.  The new datum is returned.
     *
     * @return the new datum added to the end of this array
     */
    public Datum add() {
        Datum result = Datum.from( arrayType.getItemType() );
        array.add( result );
        return result;
    }


    /**
     * Return the datum at the given index within this array.
     *
     * @param _index the index of the datum to retrieve
     * @return the datum at the given index within this array
     */
    public Datum get( final int _index ) {
        return array.get( _index );
    }


    /**
     * Returns the number of elements in this array.
     *
     * @return the number of elements in this array
     */
    public int elements() {
        return array.size();
    }


    /**
     * Informs this datum that all elements have been added to the array, and that all data has been set.  Invokes finish() on all array elements,
     * then creates and sets the binary value of this array datum.
     */
    public void finish() {

        // if we've already set our value, this call is redundant...
        if( isSet() )
            return;

        // first we call finish() on every array element...
        array.forEach( Datum::finish );

        // then we compute the bits needed to hold the binary value...
        int bitsNeeded = (arrayType.getTerminatorType() == null) ? 0 : arrayType.getTerminatorType().bits() ;
        for( Datum item : array ) {
            bitsNeeded += item.size();
        }

        // make the binary value for the entire array...
        buffer = new BitBuffer( bitsNeeded );
        array.forEach( item -> {buffer.put( item.get() ); item.get().flip();} );
        if( terminator != null ) {
            buffer.put( terminator.get() );
            terminator.get().flip();
        }
        buffer.flip();

        size = bitsNeeded;
    }


    /**
     * Sets this datum's value from the bits in the given buffer.  Upon invocation, the given buffer's position must be at the first bit of this
     * datum's value, and the limit must be at the bit following the last bit that is available.  This method will read as many bits from the buffer
     * as are required to set this datum's value, leaving the position at the first bit following the bits read.  The given buffer's limit is not
     * changed by this method.
     *
     * @param _buffer the buffer containing the bits from which this datum's value will be read
     */
    @Override
    public void set( final BitBuffer _buffer ) {

        // sanity check...
        if( buffer != null )
            throw new IllegalStateException( "Attempting to set a value that has already been set" );
        if( _buffer == null )
            throw new IllegalArgumentException( "Required buffer argument is missing" );

        // we handle each type of array differently...
        // fixed-length arrays...
        if( arrayType.getLength() > 0 ) {

            // we initialized the elements in the constructor, so now we just have to read in their data...
            for( int i = 0; i < arrayType.getLength(); i++ ) {

                Datum element = array.get( i );
                element.set( _buffer );
                element.finish();
            }
        }

        // zero-terminated arrays...
        else if( arrayType.getTerminatorType() != null ) {

            // loop until we run into a terminator...
            while( true ) {

                // first we see if we can read the zero terminator...
                int oldPosition = _buffer.position();
                Datum term = new SimpleDatum( arrayType.getTerminatorType() );
                term.set( _buffer );
                if( term.getAsInt() == 0 )
                    break;
                _buffer.position( oldPosition );  // rewind back to the point where we tried to read the terminator...

                // not terminated yet, so add a new element...
                Datum element = Datum.from( arrayType.getItemType() );
                element.set( _buffer );
                element.finish();
                add( element );
            }
        }

        // data-bound arrays...
        else {

            // loop until we have no more data...
            while( _buffer.remaining() > 0 ) {

                // add a new element...
                Datum element = Datum.from( arrayType.getItemType() );
                element.set( _buffer );
                element.finish();
                add( element );
            }
        }

        // automatically finish this thing...
        finish();
    }


    /**
     * Sets the value of this datum to the given string.  This setter works on a datum of any character or string type (i.e., ASCII or arrays of
     * ASCII). Invoking this method on a datum of any other type will throw an exception.  If this datum is a fixed-length type, then the given string
     * must exactly match the length, or an exception will be thrown.  Note that the string will be encoded as UTF-8, which is compatible with
     * US-ASCII but allows Unicode characters.
     *
     * @param _value the string value to set this datum to
     */
    @Override
    public void setTo( final String _value ) {

        // sanity checks...
        if( isSet() )
            throw new IllegalStateException( "Attempted to set a datum that's already set" );
        if( _value == null )
            throw new IllegalArgumentException( "String argument is missing" );
        boolean isString = (arrayType.getItemType() == DataTypes.ASCII );
        if( !isString )
            throw new IllegalStateException( "Attempted to set a non-string array datum to a string" );

        // some setup...
        byte[] strBytes = _value.getBytes( StandardCharsets.UTF_8 );

        // sanity check...
        if( arrayType.isFixedLength() && (elements() != strBytes.length) )
            throw new IllegalArgumentException( "Attempted to write " + strBytes.length + " byte string to "
                    + elements() + " character long fixed ASCII array" );

        // get the bits we need to set the array...
        ByteBuffer bb = ByteBuffer.allocate( strBytes.length + (arrayType.isZeroTerminated() ? 1 : 0) );
        bb.put( strBytes );
        bb.flip();
        bb.limit( bb.capacity() );
        BitBuffer bits = new BitBuffer( bb );

        // then just set our array and we're done...
        set( bits );
    }


    /**
     * Returns the value of this datum as a string value.  This getter works on a datum of any character or string type (i.e., ASCII or arrays of
     * ASCII). Invoking this method on a datum of any other type will throw an exception.  Note that this datum will be decoded as UTF-8, which is
     * compatible with US-ASCII but allows Unicode characters.
     *
     * @return the value of this datum as a string
     */
    @Override
    public String getAsString() {

        // sanity checks...
        if( !isSet() )
            throw new IllegalStateException( "Attempted to get string from unset datum" );

        // if we don't have an array of ASCII characters, we've got a problem...
        boolean isString = (arrayType.getItemType() == DataTypes.ASCII );
        if( !isString )
            throw new IllegalStateException( "Attempted to get string from a datum that is not a string" );

        // we're good, so handle them...
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < elements(); i++ ) {
            Datum id = get( i );
            char c = (char)id.getAsShort();
            if( c != 0)
                sb.append( c );
        }
        return sb.toString();
    }


    /**
     * Sets the value of this datum to the given boolean.  This setter works on a simple datum of any boolean or integer type.  Invoking this method
     * on any array datum, composite datum, or simple datum that is not a boolean or integer type will throw an exception.  For integer types, a value
     * of false will be set as a zero, and a value of true will be set as a one.
     *
     * @param _value the boolean value to set this datum to.
     */
    @Override
    public void setTo( final boolean _value ) {
        throw new UnsupportedOperationException( "Attempted to set boolean value on array datum" );
    }


    /**
     * Returns the value of this datum as a boolean value.  This getter works on a simple datum of any boolean or integer type.  Invoking this method
     * on any array datum, composite datum, or simple datum that is not a boolean or integer type will throw an exception.  For integer types, a value
     * of zero will be returned as false, and any other value will be returned as true.
     *
     * @return the value of this datum as a boolean
     */
    @Override
    public boolean getAsBoolean() {
        throw new UnsupportedOperationException( "Attempted to get boolean value from array datum" );
    }


    /**
     * Sets the value of this datum to the given double.  This setter works on any simple datum of any integer or float type.  Invoking this method on
     * a datum of any other type will throw an exception.  Depending on this datum's type, the resulting value may be out of range (which will throw
     * an exception) or may lose precision (because the PakBus type is less precise than a double).  When setting integer values with this method, the
     * double value will first be rounded to the nearest integer.
     *
     * @param _value the double value to set this datum to
     */
    @Override
    public void setTo( final double _value ) {
        throw new UnsupportedOperationException( "Attempted to set double value on array datum" );
    }


    /**
     * Returns the value of this datum as a double.  This getter works on any simple datum of any integer or float type.  Invoking this method on a
     * datum of any other type will throw an exception.  Depending on this datum's type, the returned value may be out of range (which will throw an
     * exception).
     */
    @Override
    public double getAsDouble() {
        throw new UnsupportedOperationException( "Attempted to get double value from array datum" );
    }
}
