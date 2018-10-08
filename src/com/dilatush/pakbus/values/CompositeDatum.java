package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.CompositeDataType;
import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.util.BitBuffer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dilatush.pakbus.util.Transformer.*;

/**
 * Instances of this class represent a composite datum, containing an arbitrary number of named properties of any type. Instances of this class are
 * mutable and NOT threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class CompositeDatum extends ADatum {

    private final Map<String,Datum> props;
    private final CompositeDataType compositeType;
    private final List<CP> order;


    public CompositeDatum( final DataType _type ) {
        super( _type );

        // sanity check...
        if( !(_type instanceof CompositeDataType) )
            throw new IllegalArgumentException( "Attempted to create a CompositeDatum from a different data type: " + _type );

        // some setup...
        compositeType = (CompositeDataType) type;
        props = new HashMap<>();
        order = compositeType.order();

        // create all our properties...
        order.forEach( item -> props.put( item.getName(), getNewDatum( item.getType() ) ) );
    }


    public Datum getDatum( final String _name ) {
        return props.get( _name );
    }


    public BitBuffer get( final String _name ) {
        Datum datum = getDatum( _name );
        if( datum == null )
            throw new IllegalArgumentException( "Property " + _name + " does not exist" );
        return datum.get();
    }


    public void set( final String _name, final BitBuffer _buffer ) {

        // call set() on the named property...
        Datum datum = getDatum( _name );
        if( datum == null )
            throw new IllegalArgumentException( "Property " + _name + " does not exist" );
        datum.set( _buffer );
    }


    /**
     * Informs this datum that all properties have had their data set.  Note that this method is invoked automatically at the end of a set()
     * invocation.  Invokes finish() on all properties, then creates and sets the binary value of this array datum.
     */
    public void finish() {

        // if we've already set our value, this call is redundant...
        if( isSet() )
            return;

        // first we call finish() on every property...
        props.values().forEach( Datum::finish );

        // then we compute the bits needed to hold the binary value...
        int bitsNeeded = 0;
        for( Datum item : props.values() ) {
            bitsNeeded += item.size();
        }

        // make the binary value for the entire array...
        buffer = new BitBuffer( bitsNeeded );
        order.forEach( cp -> {
            Datum datum = props.get( cp.getName() );
            buffer.put( datum.get() );
            datum.get().flip();
        } );
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

        // iterate through our properties in order, setting the value for each one...
        order.forEach( cp -> {
            Datum datum = props.get( cp.getName() );
            datum.set( _buffer );
            datum.finish();
        } );

        // we've set it all, so finish things up...
        finish();
    }


    public static void main( String[] _args ) {

        CompositeDatum pk = new CompositeDatum( DataTypes.fromName( "Packet" ) );
        fromInt( 10, pk.getDatum( "LinkState" ) );
        fromInt( 0xabc, pk.getDatum( "DstPhyAddr" ) );
        fromInt( 0, pk.getDatum( "ExpMoreCode" ) );
        fromInt( 1, pk.getDatum( "Priority" ) );
        fromInt( 0x123, pk.getDatum( "SrcPhyAddr" ) );
        fromInt( 0, pk.getDatum( "HiProtoCode" ) );
        fromInt( 1, pk.getDatum( "DstNodeId" ) );
        fromInt( 0, pk.getDatum( "HopCnt" ) );
        fromInt( 1, pk.getDatum( "SrcNodeId" ) );
        pk.getDatum( "Message" ).finish();
        pk.finish();
        BitBuffer bb = pk.get();

        pk = new CompositeDatum( DataTypes.fromName( "Packet" ) );
        pk.set( bb );
        bb = pk.get();

        pk.hashCode();
    }
}
