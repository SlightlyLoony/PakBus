package com.dilatush.pakbus.comms;

import java.time.Duration;

/**
 * Implemented by classes that provide a serial data transceiver.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface SerialTransceiver {

    /**
     * Adds the given byte to the ordered queue of bytes to be transmitted from this serial transceiver, returning true if the byte was successfully
     * added.  A false return indicates that the transmission queue is full and the byte could not be added.
     *
     * @param _byte the byte to be added to the transmission queue
     * @return true if the byte was successfully added
     */
    boolean tx( final byte _byte );


    /**
     * Returns the next byte received from this serial transceiver, blocking until it becomes available.
     *
     * @return the next byte received
     * @throws InterruptedException if interrupted while blocked
     */
    byte rx() throws InterruptedException;


    /**
     * Returns how much time has elapsed since the last byte was received.
     *
     * @return how much time has elapsed since the last byte was received
     */
    Duration sinceRx();


    /**
     * Returns how much time has elapsed since the last byte was transmitted.
     *
     * @return how much time has elapsed since the last byte was transmitted
     */
    Duration sinceTx();
}
