package com.dilatush.pakbus.comms;

/**
 * Implemented by classes that provide a raw packet data transceiver.
 *
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface PacketTransceiver {

    /**
     * Adds the given packet to the ordered queue of packets to be transmitted from this packet transceiver, returning true if the packet was
     * successfully added.  A false return indicates that the transmission queue is full and the packet could not be added.
     *
     * @param _packet the packet to be added to the transmission queue
     * @return true if the packet was successfully added
     */
    boolean tx( final RawPacket _packet );


    /**
     * Returns the next packet received from this packet transceiver, blocking for up to one second until it becomes available.
     *
     * @return the next packet received, or null if none were received
     * @throws InterruptedException if interrupted while blocked
     */
    RawPacket rx() throws InterruptedException;
}
