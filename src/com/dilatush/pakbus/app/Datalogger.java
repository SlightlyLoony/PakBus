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
import com.dilatush.pakbus.shims.FieldDefinition;
import com.dilatush.pakbus.shims.TableDefinition;
import com.dilatush.pakbus.shims.TableDefinitions;
import com.dilatush.pakbus.types.*;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.ArrayDatum;
import com.dilatush.pakbus.values.CompositeDatum;
import com.dilatush.pakbus.values.Datum;
import com.dilatush.pakbus.values.SimpleDatum;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static com.dilatush.pakbus.types.DataTypes.*;

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
     * Sets one or more values in the given table and field as the given type.  If the given datum with the values to set is an array datum (but not
     * ASCIIZ or BOOL8), then this will set any number of entries in an array field.  Otherwise (the normal case) a single value is being set.
     *
     * @param _tableName the table name to set the value in
     * @param _fieldName the field name to set the value in
     * @param _values the values to set
     * @return the number of seconds to wait before trying to communicate with the datalogger, or -1 if there was no reboot and no wait is required,
     *         or null if there was an error
     */
    public Integer setValues( final String _tableName, final String _fieldName, final Datum _values ) {

        // sanity checks...
        Checks.required( _tableName, _fieldName, _values );

        // send our request and wait for an answer...
        Msg msg = new SetValuesReqMsg( 0, _tableName, _fieldName, _values, new RequestContext() );
        Transaction trans = sendRequest( msg, SetValuesRspMsg.class, 5 );

        // wait for our response, or a bad response...
        try { trans.waiter.acquire(); } catch( InterruptedException _e ) { return null; }

        // if we got no response, just leave...
        if( trans.response == null ) return null;

        // if we got a bad response code, just leave...
        SetValuesRspMsg rspMsg = (SetValuesRspMsg)trans.response;
        if( rspMsg.responseCode != ResponseCode.OK )
            return null;

        // all is well, so return our interval...
        return rspMsg.rebootInterval;
    }


    /**
     * Returns the value in the given table and field as the given type.  If the swath is one, the value is returned as the single value.  Otherwise,
     * the value is returned as an array of the given field type.
     *
     * @param _tableName the table name to get the value from
     * @param _fieldName the field name to get the value from
     * @param _fieldType the type to convert the returned value to
     * @param _swath the number of values to return (>1 for array fields)
     * @return  the datum containing the value requested, or null if there was an error
     */
    public Datum getValues( final String _tableName, final String _fieldName, final PakBusType _fieldType, final int _swath ) {

        // sanity checks...
        Checks.required( _tableName, _fieldName, _fieldType );
        if( _swath < 1 )
            throw new IllegalArgumentException( "Illegal value for swath: " + _swath );

        // send our request and wait for an answer...
        Msg msg = new GetValuesReqMsg( 0, _tableName, _fieldName, _fieldType, _swath, new RequestContext() );
        Transaction trans = sendRequest( msg, GetValuesRspMsg.class, 5 );

        // wait for our response, or a bad response...
        try { trans.waiter.acquire(); } catch( InterruptedException _e ) { return null; }

        // if we got no response, just leave...
        if( trans.response == null ) return null;

        // if we got a bad response code, just leave...
        GetValuesReqMsg reqMsg = (GetValuesReqMsg)trans.request;
        GetValuesRspMsg rspMsg = (GetValuesRspMsg)trans.response;
        if( rspMsg.responseCode != ResponseCode.OK )
            return null;

        // all is well, so make a datum of the right type...
        DataType dataType = DataTypes.fromPakBusType( reqMsg.fieldType );
        Datum result;
        if( _swath > 1 ) {
            ArrayDataType arrayDataType = new ArrayDataType( "ARRAY", null, dataType );
            result = new ArrayDatum( arrayDataType );
        }
        else {
            if( dataType instanceof SimpleDataType ) result = new SimpleDatum( dataType );
            else if( dataType instanceof ArrayDataType ) result = new ArrayDatum( dataType );
            else result = new CompositeDatum( dataType );
        }

        // then stuff the result in and leave...
        result.set( new BitBuffer( rspMsg.bytes ) );
        return result;
    }


    /**
     * Returns the value in the given table and field as the given type in the returned datum.
     *
     * @param _tableName the table name to get the value from
     * @param _fieldName the field name to get the value from
     * @param _fieldType the type to convert the returned value to
     * @return  the datum containing the value requested, or null if there was an error
     */
    public Datum getValues( final String _tableName, final String _fieldName, final PakBusType _fieldType ) {
        return getValues( _tableName, _fieldName, _fieldType, 1 );
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


    /**
     * Collects all available records from the table and fields specified in the given data query.  The collected records are returned in a list of
     * {@link Datum} instances, in the order that they were collected in.  Each of the collected records has two properties in addition to all the
     * requested fields.  The first is named "Timestamp", and it is the timestamp from the datalogger indicating when the record was collected.  The
     * second is named "RecordNumber", and it is a four byte unsigned integer with the datalogger's record number for the record.  Note that a single
     * call to this method may result in multiple request/response transactions with the datalogger, as the cumulative size of the requested records
     * may exceed what the datalogger can return in a single message.
     *
     * @param _query the query specifying the table and fields to collect data from
     * @return the list of collected records
     */
    public List<Datum> collectAll( final DataQuery _query ) {
        CollectDataReqMsg msg = CollectDataReqMsg.getAll( _query, 0, new RequestContext() );
        return collectRecords( _query, msg );
    }


    /**
     * Collects records starting with the given record number and ending with the most recent record, from the table and fields specified in the given
     * data query.  The collected records are returned in a list of {@link Datum} instances, in the order that they were collected in.  Each of the
     * collected records has two properties in addition to all the requested fields.  The first is named "Timestamp", and it is the timestamp from the
     * datalogger indicating when the record was collected.  The second is named "RecordNumber", and it is a four byte unsigned integer with the
     * datalogger's record number for the record. Note that a single call to this method may result in multiple request/response transactions with the
     * datalogger, as the cumulative size of the requested records may exceed what the datalogger can return in a single message.
     *
     * @param _query the query specifying the table and fields to collect data from
     * @param _recordNumber the first record number to collect
     * @return the list of collected records
     */
    public List<Datum> collectFromRecordNumber( final DataQuery _query, final int _recordNumber ) {
        CollectDataReqMsg msg = CollectDataReqMsg.getFromRecordNumber( _query, 0, _recordNumber, new RequestContext() );
        return collectRecords( _query, msg );
    }


    /**
     * Collects records starting with the given starting record number and ending with the last record number before the given ending record, from the
     * table and fields specified in the given data query.  The collected records are returned in a list of {@link Datum} instances, in the order that
     * they were collected in.  Each of the collected records has two properties in addition to all the requested fields.  The first is named
     * "Timestamp", and it is the timestamp from the datalogger indicating when the record was collected.  The second is named "RecordNumber", and it
     * is a four byte unsigned integer with the datalogger's record number for the record. Note that a single call to this method may result in
     * multiple request/response transactions with the datalogger, as the cumulative size of the requested records may exceed what the datalogger can
     * return in a single message.
     *
     * @param _query the query specifying the table and fields to collect data from
     * @param _startRecord the first record number to collect
     * @param _endRecord the first record number to <i>not</i> collect
     * @return the list of collected records
     */
    public List<Datum> collectRangeOfRecordNumbers( final DataQuery _query, final int _startRecord, final int _endRecord ) {
        CollectDataReqMsg msg = CollectDataReqMsg.getRangeOfRecordNumbers( _query, 0, _startRecord, _endRecord, new RequestContext() );
        return collectRecords( _query, msg );
    }


    /**
     * Collects records starting with the given starting timestamp and ending with the last timestamp before the given ending record, from the table
     * and fields specified in the given data query.  The collected records are returned in a list of {@link Datum} instances, in the order that they
     * were collected in.  Each of the collected records has two properties in addition to all the requested fields.  The first is named "Timestamp",
     * and it is the timestamp from the datalogger indicating when the record was collected.  The second is named "RecordNumber", and it is a four
     * byte unsigned integer with the datalogger's record number for the record. Note that a single call to this method may result in multiple
     * request/response transactions with the datalogger, as the cumulative size of the requested records may exceed what the datalogger can return in
     * a single message.  Note: this collection mode is not implemented on the model CR200 dataloggers.
     *
     * @param _query the query specifying the table and fields to collect data from
     * @param _startRecord the first timestamp to collect
     * @param _endRecord the first timestamp to <i>not</i> collect
     * @return the list of collected records
     */
    public List<Datum> collectRangeOfTimestamps( final DataQuery _query, final NSec _startRecord, final NSec _endRecord ) {
        CollectDataReqMsg msg = CollectDataReqMsg.getRangeOfTimestamps( _query, 0, _startRecord, _endRecord, new RequestContext() );
        return collectRecords( _query, msg );
    }


    /**
     * Collects the given number of the most recent records from the table and fields specified in the given data query.  The collected records are
     * returned in a list of {@link Datum} instances, in the order that they were collected in.  Each of the collected records has two properties in
     * addition to all the requested fields.  The first is named "Timestamp", and it is the timestamp from the datalogger indicating when the record
     * was collected.  The second is named "RecordNumber", and it is a four byte unsigned integer with the datalogger's record number for the record.
     * Note that a single call to this method may result in multiple request/response transactions with the datalogger, as the cumulative size of the
     * requested records may exceed what the datalogger can return in a single message.
     *
     * @param _query the query specifying the table and fields to collect data from
     * @param _records the number of the most recent records to collect
     * @return the list of collected records
     */
    public List<Datum> collectMostRecent( final DataQuery _query, final int _records ) {
        CollectDataReqMsg msg = CollectDataReqMsg.getMostRecent( _query, 0, _records, new RequestContext() );
        return collectRecords( _query, msg );
    }


    /**
     * Collects the data returned in response to the given query and message.
     *
     * @param _query the query specifying the table and fields to collect data from
     * @param _reqMsg the request message to initiate collection with
     * @return the list of collected records
     */
    private List<Datum> collectRecords( final DataQuery _query, final CollectDataReqMsg _reqMsg ) {

        // sanity check...
        Checks.required( _query );

        // our per-record data type...
        CompositeDataType[] recType = null;

        // some setup...
        List<Datum> result = new ArrayList<>();
        boolean done = false;
        Msg msg = _reqMsg;
        Transaction trans = null;
        CollectDataRspMsg rspMsg = null;

        // loop until we get all the records...
        while( !done ) {

            // send our request...
            trans = sendRequest( msg, CollectDataRspMsg.class, 10 );

            // wait for our response, or a bad response (in which case we return with nothing)...
            try { trans.waiter.acquire(); } catch( InterruptedException _e ) { return null; }

            // if we got no response, just leave with nothing...
            if( trans.response == null ) return null;

            // otherwise, hopefully we got a chunk of data...
            rspMsg = (CollectDataRspMsg) trans.response;

            // if we got an error from the datalogger, just leave with nothing...
            if( rspMsg.responseCode != ResponseCode.OK )
                return null;

            // if we haven't already computed our record's type, do so now...
            if( recType == null ) recType = getRecordDataType( trans );

            // loop through the response until we've decoded all the blocks in it...
            BitBuffer rspBits = new BitBuffer( rspMsg.bytes );
            while( rspBits.remaining() > 0 ) {

                // decode the block header...
                CompositeDatum header = new CompositeDatum( BLOCK_HEADER );
                header.set( rspBits );

                // if we got a fragment, error as we're not supporting these (yet)...
                // TODO: implement fragmented data collection records...
                if( header.at( FIELD_IS_OFFSET ).getAsBoolean() )
                    throw new UnsupportedOperationException( "Collecting data record fragments is not supported" );

                // decode the header...
                int recordNumber    = header.at( FIELD_FIRST_RECORD_NUMBER ).getAsInt();
                int tableNumber     = header.at( FIELD_TABLE_NUMBER ).getAsInt();
                int numberOfRecords = header.at( FIELD_RECORD_COUNT ).getAsInt();

                // decode all the records in this block
                for( int record = 0; record < numberOfRecords; record++ ) {

                    // decode a record, using the deserialization type...
                    CompositeDatum datum = new CompositeDatum( recType[0] );
                    datum.set( rspBits );

                    // now switch the type to the presentation type, and set the record number...
                    datum.changeTypeTo( recType[1] );
                    datum.at( FIELD_RECORD_NUMBER ).setTo( recordNumber );

                    // add it to our result...
                    result.add( datum );

                    // bump our record number, to the next one in the block (if there are any)...
                    recordNumber++;
                }
            }

            // if there are no more records, we're done...
            if( !rspMsg.moreRecords )
                done = true;

            // otherwise, we need to send another request...
            else {

                Log.logLn( "Collecting supplementary record..." );

                // some setup...
                CollectDataReqMsg reqMsg = (CollectDataReqMsg)trans.request;
                Datum lastRec = result.get( result.size() - 1 );
                int startRec = 1 + lastRec.at( FIELD_RECORD_NUMBER ).getAsInt();
                NSec startTime = lastRec.at( FIELD_TIMESTAMP ).getAsNSec().add( new NSec( 1, 0 ) );

                // how we handle this depends on the mode of the collection request...
                if( (reqMsg.mode == 3) || (reqMsg.mode == 4) || (reqMsg.mode == 5) )
                    msg = CollectDataReqMsg.getFromRecordNumber( _query, 0, startRec, new RequestContext() );
                else if( reqMsg.mode == 6 )
                    msg = CollectDataReqMsg.getRangeOfRecordNumbers( _query, 0, startRec, reqMsg.intP2, new RequestContext() );
                else if( reqMsg.mode == 7 )
                    msg = CollectDataReqMsg.getRangeOfTimestamps( _query, 0, startTime, reqMsg.nsecP2, new RequestContext() );
                else
                    throw new UnsupportedOperationException( "Unsupported collection mode: " + reqMsg.mode );
            }
        }
        return result;
    }


    final static private String FIELD_TABLE_NUMBER        = "TableNumber";
    final static private String FIELD_FIRST_RECORD_NUMBER = "FirstRecordNumber";
    final static private String FIELD_IS_OFFSET           = "IsOffset";
    final static private String FIELD_RECORD_COUNT        = "RecordCount";
    final static private String FIELD_TIMESTAMP           = "Timestamp";
    final static private String FIELD_RECORD_NUMBER       = "RecordNumber";

    final static private CompositeDataType BLOCK_HEADER = new CompositeDataType( "BLOCK_HEADER", null,
            new CP( FIELD_TABLE_NUMBER,        UINT2  ),
            new CP( FIELD_FIRST_RECORD_NUMBER, UINT4  ),
            new CP( FIELD_IS_OFFSET,           BIT    ),
            new CP( FIELD_RECORD_COUNT,        BITS15 ) );


    /**
     * Returns the data types for the kind of record we're collecting.  An array with two data types is returned.  The first has all the correct
     * data types for how the records are serialized; the second is identical except that it has the additional property "RecordNumber", which is
     * synthetic and not part of the
     *
     * @return the deserialization and presentation data types
     */
    private CompositeDataType[] getRecordDataType( final Transaction _trans ) {

        // retrieve the query we made from the request message...
        DataQuery query = ((CollectDataReqMsg)_trans.request).query;

        // some setup...
        TableDefinitions tds = getTableDefinitions();  // ensure that we've loaded table definitions...
        TableDefinition td = tds.getTableDef( query.tableIndex );
        boolean allFields = (0 == query.fieldsSize());
        List<CP> cps = new ArrayList<>();

        // add the timestamp field...
        cps.add( new CP( FIELD_TIMESTAMP, DataTypes.fromPakBusType( PakBusType.decode( td.timeType ) ) ) );

        // if the query has an empty field list, then we're collecting ALL the fields in that record...
        if( allFields ) {
            for( int fn = 1; fn <= td.fieldSize(); fn++ ) {
                FieldDefinition fd = td.getField( fn - 1 );
                cps.add( getField( fd ) );
            }
        }

        // otherwise we're collecting only the specified fields...
        else {
            DataQuery.FieldIterator fi = query.iterator();
            while( fi.hasNext() ) {
                FieldDefinition fd = td.getField( ((Integer)fi.next()) - 1 );
                cps.add( getField( fd ) );
            }
        }

        // we've got all the fields, so create our deserialization type...
        CompositeDataType[] result = new CompositeDataType[2];
        result[0] = new CompositeDataType( "REC_TYPE", null, cps );

        // now add the record number field and create our presentation type...
        cps.add( 0, new CP( FIELD_RECORD_NUMBER, UINT4 ) );
        result[1] = new CompositeDataType( "REC_TYPE", null, cps );

        // and we're outta here...
        return result;
    }


    private CP getField( final FieldDefinition _fieldDefinition ) {

        // get our base type...
        DataType dataType = DataTypes.fromPakBusType( PakBusType.decode( _fieldDefinition.fieldType ) );
        if( dataType == null )
            throw new IllegalStateException( "Invalid data type: " + _fieldDefinition.fieldType );

        // if it's not an array, just return a simple type...
        if( _fieldDefinition.pieceSize == 1 )
            return new CP( _fieldDefinition.name, dataType );

        // otherwise, make an array...
        return new CP( _fieldDefinition.name, new ArrayDataType( "ARRAY", null, dataType, _fieldDefinition.pieceSize ) );
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


    private static class BITS15 {
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
