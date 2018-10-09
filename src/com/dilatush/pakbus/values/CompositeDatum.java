package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.*;
import com.dilatush.pakbus.util.BitBuffer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        order.forEach( item -> props.put( item.getName(), Datum.from( item.getType() ) ) );
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

        // first we finish all our children that are set or required...
        order.forEach( item -> {
            Datum datum = props.get( item.getName() );
            if( !item.isOptional() || datum.isSet() )
                datum.finish();
        } );

        // then we compute the bits needed to hold the binary value...
        int bitsNeeded = 0;
        for( Datum item : props.values() ) {
            if( item.isSet() )
                bitsNeeded += item.size();
        }

        // finally, make the binary value for the entire object...
        buffer = new BitBuffer( bitsNeeded );
        order.forEach( cp -> {
            Datum datum = props.get( cp.getName() );
            if( datum.isSet() )
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

        // if there are required properties laid out AFTER an optional property, we need to compute the length of them...
        int bitsAfterOptional = 0;
        boolean foundOptional = false;
        for( CP cp : order ) {
            Datum datum = props.get( cp.getName() );
            if( cp.isOptional() )
                foundOptional = true;
            else if( foundOptional ) {
                if( datum.type().bits() == 0 )
                    throw new IllegalStateException( "Attempted to set value of composite with variable-length property after an optioanl property" );
                bitsAfterOptional += datum.type().bits();
            }
        }
        final int bao = bitsAfterOptional;

        // iterate through our properties in order, setting the value for each one that's set...
        order.forEach( cp -> {

            Datum datum = props.get( cp.getName() );

            // if we're setting an optional property, we need to constrain the bits it may use...
            if( cp.isOptional() ) {
                int oldLimit = _buffer.limit();
                _buffer.limit( oldLimit - bao );
                if( _buffer.remaining() > 0 )
                    datum.set( _buffer );
                _buffer.limit( oldLimit );
            }

            // otherwise, it's simpler...
            else
                datum.set( _buffer );

            datum.finish();
        } );

        // we've set it all, so finish things up...
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
        throw new UnsupportedOperationException( "Attempted to set composite datum to a string value" );
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
        throw new UnsupportedOperationException( "Attempted to get composite datum as a string value" );
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
        throw new UnsupportedOperationException( "Attempted to set boolean value on composite datum" );
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
        throw new UnsupportedOperationException( "Attempted to get boolean value from composite datum" );
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
        throw new UnsupportedOperationException( "Attempted to set double value on composite datum" );
    }


    /**
     * Returns the value of this datum as a double.  This getter works on any simple datum of any integer or float type.  Invoking this method on a
     * datum of any other type will throw an exception.  Depending on this datum's type, the returned value may be out of range (which will throw an
     * exception).
     */
    @Override
    public double getAsDouble() {
        throw new UnsupportedOperationException( "Attempted to get double value from composite datum" );
    }


    public static void main( String[] _args ) {

        CompositeDatum pk = new CompositeDatum( DataTypes.fromName( "Packet" ) );
        pk.at( "LinkState"   ).setTo( 10    );
        pk.at( "DstPhyAddr"  ).setTo( 0xabc );
        pk.at( "ExpMoreCode" ).setTo( 0     );
        pk.at( "Priority"    ).setTo( 1     );
        pk.at( "SrcPhyAddr"  ).setTo( 0x123 );
        pk.at( "HiProtoCode" ).setTo( 0     );
        pk.at( "DstNodeId"   ).setTo( 1     );
        pk.at( "HopCnt"      ).setTo( 0     );
        pk.at( "SrcNodeId"   ).setTo( 1     );
        pk.at( "Message"     ).finish();
        pk.finish();
        BitBuffer bb = pk.get();

        int srcAddr    = pk.at( "SrcPhyAddr" ).getAsInt();
        int dstAddr    = pk.at( "DstPhyAddr" ).getAsInt();
        byte linkState = pk.at( "LinkState"  ).getAsByte();

        pk = new CompositeDatum( DataTypes.fromName( "Packet" ) );
        pk.set( bb );
        bb = pk.get();

        ArrayDatum ad = new ArrayDatum( DataTypes.fromName( "ASCIIZ" ) );
        ad.setTo( "This is a hamburger test of the mustard persuasion." );
        ad.finish();
        String adr = ad.getAsString();

        SimpleDatum fp2 = new SimpleDatum( DataTypes.fromPakBusType( PakBusType.FP2 ) );
        fp2.setTo( 12.345 );
        double fp2r = fp2.getAsDouble();

        pk.hashCode();
    }
}
