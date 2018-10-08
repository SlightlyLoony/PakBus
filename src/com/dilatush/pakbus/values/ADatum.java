package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.ArrayDataType;
import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.types.SimpleDataType;
import com.dilatush.pakbus.util.BitBuffer;

/**
 * Abstract base class for all classes that implement Datum.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class ADatum implements Datum {

    final protected DataType type;   // the type of this datum...

    protected BitBuffer buffer;
    protected int size;  // the actual size of this datum, in bits, or zero if it is not yet known...


    public ADatum( final DataType _type ) {

        if( _type == null )
            throw new IllegalArgumentException( "Required type argument is missing" );

        type = _type;
        buffer = null;
        size = _type.bits();
    }


    public void finish() {
        if( !isSet() )
            throw new IllegalStateException( "Attempted to finish a datum whose value has not been set" );
    }


    @Override
    public boolean isSet() {
        return buffer != null;
    }


    @Override
    public BitBuffer get() {
        return buffer;
    }


    @Override
    public DataType type() {
        return type;
    }


    @Override
    public int size() {
        return size;
    }


    /**
     * Returns a newly constructed datum appropriate for the given data type.
     *
     * @param _type the type of datum to construct
     * @return the newly constructed datum
     */
    public Datum getNewDatum( final DataType _type ) {

        if( _type == null )
            throw new IllegalArgumentException( "Required type is missing" );

        if( _type instanceof SimpleDataType )
            return new SimpleDatum( _type );
        else if( _type instanceof ArrayDataType )
            return new ArrayDatum( _type );
        return new CompositeDatum( _type );
    }
}
