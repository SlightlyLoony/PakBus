package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.ArrayDataType;
import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.util.BitBuffer;

import java.util.ArrayList;
import java.util.List;

import static com.dilatush.pakbus.util.Transformer.fromInt;
import static com.dilatush.pakbus.util.Transformer.toInt;

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
                Datum datum = getNewDatum( arrayType.getItemType() );
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
            fromInt( 0, terminator );
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

        super.finish();
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
                if( toInt( term ) == 0 )
                    break;
                _buffer.position( oldPosition );  // rewind back to the point where we tried to read the terminator...

                // not terminated yet, so add a new element...
                Datum element = getNewDatum( arrayType.getItemType() );
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
                Datum element = getNewDatum( arrayType.getItemType() );
                element.set( _buffer );
                element.finish();
                add( element );
            }
        }

        // automatically finish this thing...
        finish();
    }
}
