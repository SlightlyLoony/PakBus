package com.dilatush.pakbus.app;

import com.dilatush.pakbus.HopCount;
import com.dilatush.pakbus.Log;
import com.dilatush.pakbus.NSec;
import com.dilatush.pakbus.Node;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.comms.SimpleContext;
import com.dilatush.pakbus.messages.Msg;
import com.dilatush.pakbus.messages.bmp5.*;
import com.dilatush.pakbus.messages.pakctrl.GetStringSettingsReqMsg;
import com.dilatush.pakbus.messages.pakctrl.GetStringSettingsRspMsg;
import com.dilatush.pakbus.shims.DataQuery;
import com.dilatush.pakbus.shims.DataQuery.FieldIterator;
import com.dilatush.pakbus.shims.FieldDefinition;
import com.dilatush.pakbus.shims.TableDefinition;
import com.dilatush.pakbus.shims.TableDefinitions;
import com.dilatush.pakbus.types.*;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.ArrayDatum;
import com.dilatush.pakbus.values.Datum;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        return correctTime( Duration.ZERO );
    }


    /**
     * Corrects the datalogger's clock by the given duration, returning the original clock time.
     *
     * @param _correction the amount to correct the clock by (may be negative)
     * @return the datalogger's clock before correction...
     */
    public Instant correctTime( final Duration _correction ) {

        // sanity check...
        Checks.required( _correction );

        // send the request...
        Msg msg = new ClockReqMsg( 0, new NSec( _correction ), new RequestContext() );
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
     * Calibrate the datalogger's clock against this computer's clock, adjusting the datalogger's clock to match within one second.
     */
    public void calibrateClock() {

        // get our round-trip time...
        // empirically the first two or three round trips are variable; the rest stable, so we take the fifth one...
        Duration roundTrip = null;
        Instant start = null;
        Instant end = null;
        Instant clock = null;
        for( int i = 0; i < 5; i++ ) {

            start = Instant.now();
            clock = getTime();
            end = Instant.now();
            roundTrip = Duration.between( start, end );
        }

        // calculate our correction...
        Duration center = Duration.ofMillis( roundTrip.toMillis() >>> 1 );
        Instant target = start.plus( center );
        Duration correction = Duration.between( clock, target );

        // if the computed correction is greater than a second, issue it...
        if( Math.abs( correction.toMillis() ) > 1000 ) {
            Log.logLn( "Correcting datalogger clock by " + correction );
            correctTime( correction );
        }
    }


    /**
     * Returns all settings in the datalogger as a map of setting names to their values.
     *
     * @return a map of setting names to their values
     */
    public Map<String,String> getAllSettings() {
        return getSettings( new ArrayList<>() );
    }


    /**
     * Returns the values of the given setting names as a map of the setting names to their values.
     *
     * @param _settingNames the list of setting names to retrieve
     * @return a map of setting names to their values
     */
    public Map<String,String> getSettings( final List<String> _settingNames ) {

        // sanity check...
        Checks.required( _settingNames );

        // form our request string...
        StringBuilder sb = new StringBuilder();
        _settingNames.forEach( name -> {
            if( sb.length() != 0 )
                sb.append( ';' );
            sb.append( name );
        } );

        // send the request...
        Msg msg = new GetStringSettingsReqMsg( sb.toString(), new RequestContext() );
        Transaction trans = sendRequest( msg, GetStringSettingsRspMsg.class, 5 );

        // wait for our response, or a bad response...
        try { trans.waiter.acquire(); } catch( InterruptedException _e ) { return null; }

        // if we got no response, just leave...
        if( trans.response == null ) return null;

        // otherwise, return with our settings...
        GetStringSettingsRspMsg rspMsg = (GetStringSettingsRspMsg) trans.response;
        return rspMsg.settings;
    }


    /**
     * Read the datalogger file with the given name and return its bytes.
     *
     * @param _fileName the name of the file to read
     * @return the bytes of the file read, or null if there was a problem
     */
    public ByteBuffer readFile( final String _fileName ) {

        // sanity check...
        Checks.required( _fileName );

        // some setup...
        boolean done = false;
        int swath = 400;
        ByteBuffer result = ByteBuffer.allocate( 1000 );
        int offset = 0;

        // loop until we get it all...
        while( !done ) {

            Msg msg = new FileReceiveReqMsg( 0, _fileName, 0, offset, swath, new RequestContext() );
            Transaction trans = sendRequest( msg, FileReceiveRspMsg.class, 10 );

            // wait for our response, or a bad response (in which case we return with nothing)...
            try { trans.waiter.acquire(); } catch( InterruptedException _e ) { return null; }

            // if we got no response, just leave with nothing...
            if( trans.response == null ) return null;

            // otherwise, hopefully we got a chunk of data...
            FileReceiveRspMsg rspMsg = (FileReceiveRspMsg) trans.response;

            // if we got an error, just leave with nothing...
            if( (rspMsg.responseCode != ResponseCode.OK) || (rspMsg.fileData == null) )
                return null;

            // if we don't have enough room in our buffer, enlarge it...
            if( result.remaining() < rspMsg.fileData.limit() ) {
                ByteBuffer bb = ByteBuffer.allocate( result.capacity() + 1000 );
                result.flip();
                bb.put( result );
                result = bb;
            }

            // concatenate the data we just received...
            offset += rspMsg.fileData.limit();
            result.put( rspMsg.fileData );
            if( rspMsg.fileData.limit() < swath ) {
                done = true;
            }
        }
        result.flip();
        return result;
    }


    /**
     * Reads the table definitions from the datalogger, caches them, and returns them.
     *
     * @return the table definitions from this datalogger
     */
    public TableDefinitions readTableDefinitions() {

        // first we read the file...
        ByteBuffer tdfBytes = readFile( ".TDF" );
        if( tdfBytes == null )
            return null;

        // then we interpret them...
        tableDefinitions = new TableDefinitions( tdfBytes );
        return tableDefinitions;
    }


    /**
     * Returns the table definitions from this datalogger, reading them from the datalogger if necessary.
     *
     * @return the table definitions from this datalogger
     */
    public TableDefinitions getTableDefinitions() {
        return (tableDefinitions == null) ? readTableDefinitions() : tableDefinitions;
    }


    public Datum collectMostRecent( final DataQuery _query, final int _records ) {

        // sanity check...
        Checks.required( _query );

        // some setup...
        TableDefinitions tds = getTableDefinitions();
        ByteBuffer buffer = ByteBuffer.allocate( 1000 );
        boolean done = false;
        Msg msg = CollectDataReqMsg.getMostRecent( _query, 0, _records, new RequestContext() );
        Transaction trans = null;
        CollectDataRspMsg rspMsg = null;

        // loop until we get all the bytes...
        while( !done ) {

            // get our message...
            trans = sendRequest( msg, CollectDataRspMsg.class, 10 );

            // wait for our response, or a bad response (in which case we return with nothing)...
            try { trans.waiter.acquire(); } catch( InterruptedException _e ) { return null; }

            // if we got no response, just leave with nothing...
            if( trans.response == null ) return null;

            // otherwise, hopefully we got a chunk of data...
            rspMsg = (CollectDataRspMsg) trans.response;

            // if we got an error, just leave with nothing...
            if( rspMsg.responseCode != ResponseCode.OK )
                return null;

            // add the data in the message to our buffer, expanding it as required...
            if( rspMsg.bytes.limit() > buffer.remaining() ) {
                ByteBuffer newBuffer = ByteBuffer.allocate( buffer.capacity() + 1000 );
                buffer.flip();
                newBuffer.put( buffer );
                buffer = newBuffer;
            }
            buffer.put( rspMsg.bytes );

            // if we have no fragment, we're done...
            if( !rspMsg.isFragment )
                done = true;

            // if it is a fragment, we'll just error out...
            else
                throw new UnsupportedOperationException( "Collecting data record fragments is not supported (yet)" );

        }

        // we've got our bytes; now we need to interpret them...
        buffer.flip();
        DataQuery query = ((CollectDataReqMsg)trans.request).query;
        TableDefinition td = tds.getTableDef( rspMsg.tableNumber );

        // make up our per-record data type...
        boolean allFields = (0 == query.fieldsSize());
        List<CP> cps = new ArrayList<>();
        cps.add( new CP( "Timestamp", DataTypes.fromPakBusType( PakBusType.decode( td.timeType ) ) ) );
        if( allFields ) {
            for( int fn = 1; fn <= td.fieldSize(); fn++ ) {
                FieldDefinition fd = td.getField( fn - 1 );
                cps.add( new CP( fd.name, DataTypes.fromPakBusType( PakBusType.decode( fd.fieldType ) ) ) );
            }
        }
        else {
            FieldIterator fi = query.iterator();
            while( fi.hasNext() ) {
                FieldDefinition fd = td.getField( ((Integer)fi.next()) - 1 );
                cps.add( new CP( fd.name, DataTypes.fromPakBusType( PakBusType.decode( fd.fieldType ) ) ) );
            }
        }
        CompositeDataType recType = new CompositeDataType( "REC_TYPE", null, cps );
        ArrayDataType arrayType = new ArrayDataType( "Records", null, recType );

        Datum datum = new ArrayDatum( arrayType );
        datum.set( new BitBuffer( buffer ) );

        return datum;
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
