package com.dilatush.pakbus.comms;

import com.dilatush.pakbus.app.Application;
import com.dilatush.pakbus.app.Datalogger;

/**
 * Implemented by classes providing a communications context.  While the Campbell Scientific documentation implies that the PakBus protocols are
 * "like TCP", they're actually connectionless and more akin UDP than TCP.  The communications context provides the information needed by the
 * PakBus communications providers to handle messages.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Context {


    /**
     * Returns the application associated with the use of this interface.
     *
     * @return the application
     */
    Application application();


    /**
     * Returns the datalogger associated with the use of this interface.
     *
     * @return the datalogger
     */
    Datalogger datalogger();


    /**
     * Return the transaction number for a request message.
     *
     * @return the transaction number for a request message
     */
    int transactionNumber();
}
