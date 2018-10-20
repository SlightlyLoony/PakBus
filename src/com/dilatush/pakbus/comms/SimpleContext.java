package com.dilatush.pakbus.comms;

import com.dilatush.pakbus.app.Application;
import com.dilatush.pakbus.app.Datalogger;

/**
 * Instances of this class represent a simple context.  Instances of this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class SimpleContext implements Context {

    final private Application application;
    final private Datalogger datalogger;
    final private int transactionNumber;


    /**
     * Creates a new instance of this class with the given application and datalogger.
     *
     * @param _application the application
     * @param _datalogger the datalogger
     * @param _transactionNumber the transaction number
     */
    public SimpleContext( final Application _application, final Datalogger _datalogger, final int _transactionNumber ) {
        application = _application;
        datalogger = _datalogger;
        transactionNumber = _transactionNumber;
    }


    /**
     * Returns the application associated with the use of this interface.
     *
     * @return the application
     */
    @Override
    public Application application() {
        return application;
    }


    /**
     * Returns the datalogger associated with the use of this interface.
     *
     * @return the datalogger
     */
    @Override
    public Datalogger datalogger() {
        return datalogger;
    }


    /**
     * Return the transaction number for a request message.
     *
     * @return the transaction number for a request message
     */
    @Override
    public int transactionNumber() {
        return transactionNumber;
    }
}
