package com.dilatush.pakbus.comms;

import com.dilatush.pakbus.Address;
import com.dilatush.pakbus.HopCount;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class SimpleContext implements Context {


    final private Address  appAddr;
    final private Address  logAddr;
    final private int      appNode;
    final private int      logNode;
    final private HopCount hopCount;
    final private int      trnNbr;


    public SimpleContext( final Address _appAddr, final Address _logAddr, final int _appNode, final int _logNode,
                          final HopCount _hopCount, final int _trnNbr ) {

        appAddr = _appAddr;
        logAddr = _logAddr;
        appNode = _appNode;
        logNode = _logNode;
        hopCount = _hopCount;
        trnNbr = _trnNbr;
    }


    /**
     * Returns the application's physical PakBus address.
     *
     * @return the application's physical PakBus address
     */
    @Override
    public Address applicationAddress() {
        return appAddr;
    }


    /**
     * Returns the datalogger's physical PakBus address.
     *
     * @return the datalogger's physical PakBus address
     */
    @Override
    public Address dataloggerAddress() {
        return logAddr;
    }


    /**
     * Returns the application's node ID.
     *
     * @return the application's node ID
     */
    @Override
    public int applicationNode() {
        return appNode;
    }


    /**
     * Returns the datalogger's node ID.
     *
     * @return the datalogger's node ID
     */
    @Override
    public int dataloggerNode() {
        return logNode;
    }


    /**
     * Returns the hop count between the application and the datalogger.
     *
     * @return the hop count between the application and the datalogger
     */
    @Override
    public HopCount hopCount() {
        return hopCount;
    }


    /**
     * Returns the transaction number to use for sending a message, or the transaction number received for a received message.
     *
     * @return the transaction number
     */
    @Override
    public int transactionNumber() {
        return trnNbr;
    }
}
