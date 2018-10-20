package com.dilatush.pakbus.app;

import com.dilatush.pakbus.HopCount;
import com.dilatush.pakbus.Log;
import com.dilatush.pakbus.NSec;
import com.dilatush.pakbus.Node;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.comms.SimpleContext;
import com.dilatush.pakbus.messages.Msg;
import com.dilatush.pakbus.messages.bmp5.ClockReqMsg;
import com.dilatush.pakbus.messages.bmp5.ClockRspMsg;
import com.dilatush.pakbus.shims.TableDefinitions;
import com.dilatush.pakbus.util.Checks;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Instances of this class represent a datalogger on a PakBus network.  Instances of this class are mutable and stateful, and not threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Datalogger {

    final public Application application;
    final public String      name;
    final public Node        address;
    final public HopCount    hopCount;
    final public Context     context;    // simple context with no transaction number, for decoding received messages...

    final private Map<Integer,Transaction> transactions;

    private int nextTrialTransactionNumber;
    private TableDefinitions tableDefinitions;


    /**
     * Creates a new instance of this class with the given application, name, and address.
     *
     * @param _application the application this datalogger is associated with
     * @param _name the user-friendly name of this datalogger
     * @param _address the PakBus address and node ID of this datalogger
     */
    public Datalogger( final Application _application, final String _name, final Node _address ) {
        application = _application;
        name = _name;
        address = _address;
        hopCount = HopCount.ZERO;
        transactions = new HashMap<>();
        nextTrialTransactionNumber = 1;
        context = new SimpleContext( application, this, 0 );
    }


    /**
     * Handle the given message, which was received by the application.
     *
     * @param _msg the message received by the application
     */
    public synchronized void handle( final Msg _msg ) {

        // see if we have an active transaction matching the message we just got...
        Transaction active = transactions.get( _msg.transactionNumber() );
        if( active != null ) {

            // if we got the right type of message, stuff it away; null otherwise...
            active.response = active.expectedClass.isInstance( _msg ) ? _msg : null;

            // now finish up and release the thread waiting on this...
            active.waiter.release();
            transactions.remove( _msg.transactionNumber() );
        }

        // if there's no active transaction with this number, then it's a mystery and we'll ignore it...
        else {
            Log.logLn( "Received unexpected message; ignoring");
        }
    }


    /**
     * Examines all outstanding (unsatisfied) requests; if any of them have timed out the request is resent.
     */
    public synchronized void tickle() {

        Instant now = Instant.now();

        transactions.forEach( (number, transaction) -> {

            // if this transaction has timed out, resend it and reset the timeout...
            if( transaction.timeout.compareTo( now ) < 0 ) {

                send( transaction.request );
                transaction.timeout = now.plus( Duration.ofSeconds( transaction.timeoutSeconds ) );
            }
        } );
    }


    /**
     * Returns the current time in the datalogger, blocking until the request is sent and the response received.  Returns null if the request failed.
     *
     * @return the current time in the datalogger
     */
    public Instant getTime() {

        // send the request...
        Msg msg = new ClockReqMsg( NSec.ZERO, new RequestContext() );
        Transaction trans = sendRequest( msg, ClockRspMsg.class, 5 );

        // wait for our response, or a bad response...
        try { trans.waiter.acquire(); } catch( InterruptedException _e ) { return null; }

        // if we got no response, just leave...
        if( trans.response == null ) return null;

        // otherwise, return with our time...
        ClockRspMsg rspMsg = (ClockRspMsg) trans.response;
        return rspMsg.oldTime.asInstant();
    }


    /**
     * Sends the given request message, assigning the transaction number, then registers the given expected response class and maximum wait time.
     * Returns a transaction that contains a semaphore that the caller can wait on; a permit will be issued only when a response message of the
     * correct type is received.  If a response is not received within the max wait second period, the request will be resent.  If a response message
     * of the wrong type is received, the transaction is terminated and there is no response message in the transaction.
     *
     * @param _msg the request message to send
     * @param _expectedResponseClass the class of the expected response message
     * @param _maxWaitSeconds the maximum number of seconds to wait for a response
     * @return the transaction record
     */
    private synchronized Transaction sendRequest( final Msg _msg, final Class _expectedResponseClass, final int _maxWaitSeconds ) {

        // get a new transaction and number to use...
        Transaction transaction = getTransaction( _msg, _expectedResponseClass, _maxWaitSeconds );

        // send the request...
        send( _msg );

        return transaction;
    }


    private synchronized Transaction getTransaction( final Msg _msg, final Class _expectedResponseClass, final int _maxWaitSeconds ) {

        // create our transaction, stuff it away, and return it...
        Transaction result = new Transaction();
        result.waiter = new Semaphore( 0 );
        result.response = null;
        result.expectedClass = _expectedResponseClass;
        result.timeout = Instant.now().plus( Duration.ofSeconds( _maxWaitSeconds ) );
        result.timeoutSeconds = _maxWaitSeconds;
        result.request = _msg;
        transactions.put( _msg.context().transactionNumber(), result );
        return result;
    }


    private void send( final Msg _msg ) {

        Checks.required( _msg );

        application.send( _msg );
    }


    private class RequestContext implements Context {

        private int transactionNumber;

        private RequestContext() {

            // find an unused transaction number...
            Integer numb = null;
            while( numb == null ) {
                if( !transactions.containsKey( nextTrialTransactionNumber ) )
                    numb = nextTrialTransactionNumber;
                nextTrialTransactionNumber = 0xFF & (nextTrialTransactionNumber + 1);
            }

            // set the transaction number...
            transactionNumber = numb;
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
            return Datalogger.this;
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


    private static class Transaction {
        private Semaphore waiter;
        private Msg       response;
        private Instant   timeout;
        private Class     expectedClass;
        private int       timeoutSeconds;
        private Msg       request;
    }
}
