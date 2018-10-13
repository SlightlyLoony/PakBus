package com.dilatush.pakbus.comms;

import com.dilatush.pakbus.Address;
import com.dilatush.pakbus.HopCount;

/**
 * Implemented by classes providing a communications context.  While the Campbell Scientific documentation implies that the PakBus protocols are
 * "like TCP", they're actually connectionless and more akin UDP than TCP.  The communications context provides the information needed by the
 * PakBus communications providers to handle messages.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Context {


    /**
     * Returns the application's physical PakBus address.
     *
     * @return the application's physical PakBus address
     */
    Address applicationAddress();


    /**
     * Returns the datalogger's physical PakBus address.
     *
     * @return the datalogger's physical PakBus address
     */
    Address dataloggerAddress();


    /**
     * Returns the application's node ID.
     *
     * @return the application's node ID
     */
    int applicationNode();


    /**
     * Returns the datalogger's node ID.
     *
     * @return the datalogger's node ID
     */
    int dataloggerNode();


    /**
     * Returns the hop count between the application and the datalogger.
     *
     * @return the hop count between the application and the datalogger
     */
    HopCount hopCount();


    /**
     * Returns the transaction number to use for sending a message, or the transaction number received for a received message.
     *
     * @return the transaction number
     */
    int transactionNumber();
}
