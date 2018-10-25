package com.dilatush.pakbus.values;

import java.util.Objects;

/**
 * Instances of this class represent a PakBus address and node ID combination.  Instances of this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Node {

    final public Address address;
    final public int nodeID;


    /**
     * Create a new instance of this class with the given address and node id.
     *
     * @param _address the address
     * @param _nodeID the node ID
     */
    public Node( final Address _address, final int _nodeID ) {
        address = _address;
        nodeID = _nodeID;
    }


    @Override
    public String toString() {
        return address.toString() + ":" + nodeID;
    }


    @Override
    public boolean equals( final Object _o ) {
        if( this == _o ) return true;
        if( !(_o instanceof Node) ) return false;
        Node node = (Node) _o;
        return nodeID == node.nodeID &&
                Objects.equals( address, node.address );
    }


    @Override
    public int hashCode() {
        return Objects.hash( address, nodeID );
    }
}
