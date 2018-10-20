package com.dilatush.pakbus;

import java.util.Objects;

/**
 * Instances of this class represent a 12 bit PakBus physical address (either source or destination).  Instances of this class are immutable and
 * threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Address {

    public static final Address BROADCAST = new Address( 0x0FFF );

    private static final int MASK = 0x00000FFF;

    private final int address;


    /**
     * Creates a new instance of this class from the given numeric address.
     *
     * @param _address the numeric address to create an instance of this class from
     */
    public Address( final int _address ) {

        // sanity check...
        if( (~MASK & _address) != 0 )
            throw new IllegalArgumentException( "Address has more than 12 bits: " + _address );

        address = _address;
    }


    /**
     * Returns the numeric address.
     *
     * @return the numeric address
     */
    public int getAddress() {
        return address;
    }


    /**
     * Returns true if this address is a conventional application address.
     *
     * @return true if this address is a conventional application address
     */
    public boolean isApp() {
        return !isDatalogger() && !isBroadcast();
    }


    /**
     * Returns true if this address is a conventional datalogger address.
     *
     * @return true if this address is a conventional datalogger address
     */
    public boolean isDatalogger() {
        return address < 4000;
    }


    /**
     * Returns true if this address is the broadcast address.
     *
     * @return true if this address is the broadcast address
     */
    public boolean isBroadcast() {
        return address == 0x0FFF;
    }


    public String toString() {
        return Integer.toString( address );
    }


    @Override
    public boolean equals( final Object _o ) {
        if( this == _o ) return true;
        if( _o == null || getClass() != _o.getClass() ) return false;
        Address address1 = (Address) _o;
        return address == address1.address;
    }


    @Override
    public int hashCode() {
        return Objects.hash( address );
    }
}
