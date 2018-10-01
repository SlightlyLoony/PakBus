package com.dilatush.pakbus;

import java.util.Objects;

/**
 * Represents the hop count field of a PakBus packet.  Note that this is a four bit field, so it can only have values [0..15].  The purpose of this
 * field is not explained fully in the Campbell Scientific documentation, but it appears to represent PakBus router hops.  In a directly connected
 * device, this value will always be zero (THAT is documented).  Instances of this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class HopCount {

    private final int hops;


    public HopCount( final int _hops ) {

        // sanity check...
        if( (_hops < 0) || (_hops > 15) )
            throw new IllegalArgumentException( "Invalid number of hops: " + _hops );

        hops = _hops;
    }


    public int getHops() {
        return hops;
    }


    @Override
    public boolean equals( final Object _o ) {
        if( this == _o ) return true;
        if( _o == null || getClass() != _o.getClass() ) return false;
        HopCount hopCount = (HopCount) _o;
        return hops == hopCount.hops;
    }


    @Override
    public int hashCode() {
        return Objects.hash( hops );
    }
}
