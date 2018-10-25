package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.ExpectMore;
import com.dilatush.pakbus.types.LinkState;
import com.dilatush.pakbus.types.Priority;
import com.dilatush.pakbus.util.Checks;

/**
 * Instances of this class define a set of options for PakBus packets.  Instances are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class PacketOptions {

    final public LinkState state;
    final public ExpectMore expectMore;
    final public Priority priority;


    /**
     * Creates a new instance of this class with the given packet options.
     *
     * @param _state the link state for the packet
     * @param _expectMore the expect more code for the packet
     * @param _priority the priority for the packet
     */
    public PacketOptions( final LinkState _state, final ExpectMore _expectMore, final Priority _priority ) {

        // sanity check...
        Checks.required( _state, _expectMore, _priority );

        state = _state;
        expectMore = _expectMore;
        priority = _priority;
    }


    /**
     * Creates a new instance of this class with a "Ready" link state and the given expect more code and priority.
     *
     * @param _expectMore the expect more code for the packet
     * @param _priority the priority for the packet
     */
    public PacketOptions( final ExpectMore _expectMore, final Priority _priority ) {
        this( LinkState.Ready, _expectMore, _priority );
    }


    /**
     * Creates a new instance of this class with an "ExpectMore" expect more code and the given link state and priority.
     *
     * @param _state the link state for the packet
     * @param _priority the priority for the packet
     */
    public PacketOptions( final LinkState _state, final Priority _priority ) {
        this( _state, ExpectMore.ExpectMore, _priority );
    }


    /**
     * Creates a new instance of this class with a "Normal" priority and the given link state and expect more code.
     *
     * @param _state the link state for the packet
     * @param _expectMore the expect more code for the packet
     */
    public PacketOptions( final LinkState _state, final ExpectMore _expectMore ) {
        this( _state, _expectMore, Priority.Normal );
    }


    /**
     * Creates a new instance of this class with an "ExpectMore" expect more code, "Normal" priority, and the given link state.
     *
     * @param _state the link state for the packet
     */
    public PacketOptions( final LinkState _state ) {
        this( _state, ExpectMore.ExpectMore, Priority.Normal );
    }


    /**
     * Creates a new instance of this class with a "Ready" link state, "Normal" priority, and the given expect more code.
     *
     * @param _expectMore the expect more code for the packet
     */
    public PacketOptions( final ExpectMore _expectMore ) {
        this( LinkState.Ready, _expectMore, Priority.Normal );
    }


    /**
     * Creates a new instance of this class with a "Ready" link state, "ExpectMore" expect more code, and the given priority.
     *
     * @param _priority the priority for the packet
     */
    public PacketOptions( final Priority _priority ) {
        this( LinkState.Ready, ExpectMore.ExpectMore, _priority );
    }


    /**
     * Creates a new instance of this class with a "Ready" link state, "ExpectMore" expect more code, and "Normal" priority.
     */
    public PacketOptions() {
        this( LinkState.Ready, ExpectMore.ExpectMore, Priority.Normal );
    }
}
