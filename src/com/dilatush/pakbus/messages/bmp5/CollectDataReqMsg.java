package com.dilatush.pakbus.messages.bmp5;

import com.dilatush.pakbus.MessageType;
import com.dilatush.pakbus.NSec;
import com.dilatush.pakbus.Protocol;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.messages.AMsg;
import com.dilatush.pakbus.shims.DataQuery;
import com.dilatush.pakbus.shims.DataQuery.TableIterator;
import com.dilatush.pakbus.types.ArrayDataType;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.CompositeDataType;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.ArrayDatum;
import com.dilatush.pakbus.values.CompositeDatum;
import com.dilatush.pakbus.values.SimpleDatum;

import static com.dilatush.pakbus.MessageType.Request;
import static com.dilatush.pakbus.Protocol.BMP5;
import static com.dilatush.pakbus.types.DataTypes.*;

/**
 * Represents a BMP5 "Collect Data Request" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class CollectDataReqMsg extends AMsg {


    final static public String FIELD_SECURITY_CODE = "SecurityCode";
    final static public String FIELD_COLLECT_MODE = "CollectMode";
    final static public String FIELD_TABLES = "Tables";
    final static public String FIELD_TABLE_NO = "TableNumber";
    final static public String FIELD_TABLE_SIGNATURE = "TableSignature";
    final static public String FIELD_P1 = "P1";
    final static public String FIELD_P2 = "P2";
    final static public String FIELD_FIELDS = "Fields";
    final static public String FIELD_FIELD_NO = "FieldNumber";
    final static public String FIELD_TYPE_0 = "TYPE0";
    final static public String FIELD_TYPE_1 = "TYPE1";

    final static private CompositeDataType TYPE0 = new CompositeDataType( FIELD_TYPE_0, null,
            new CP( FIELD_TABLE_NO, UINT2 ),
            new CP( FIELD_TABLE_SIGNATURE, UINT2 ),
            new CP( FIELD_P1, UINT4 ),
            new CP( FIELD_P2, UINT4 ),
            new CP( FIELD_FIELDS, new ArrayDataType( FIELD_FIELD_NO, null, UINT2, UINT2 ) ) );

    final static private CompositeDataType TYPE1 = new CompositeDataType( FIELD_TYPE_1, null,
            new CP( FIELD_TABLE_NO, UINT2 ),
            new CP( FIELD_TABLE_SIGNATURE, UINT2 ),
            new CP( FIELD_P1, NSEC ),
            new CP( FIELD_P2, NSEC ),
            new CP( FIELD_FIELDS, new ArrayDataType( FIELD_FIELD_NO, null, UINT2, UINT2 ) ) );

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x09;
    final static public MessageType TYPE     = Request;


    private CollectDataReqMsg( final DataQuery _query, final int _securityCode, final int _mode, final int _p1, final int _p2, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _query, _context );

        // create and initialize our datum...
        initDataType();
        props.add( new CP( FIELD_TABLES, new ArrayDataType( FIELD_TABLES, null, TYPE0 ) ) );
        setDatum();
        datum.at( FIELD_SECURITY_CODE ).setTo( _securityCode );
        datum.at( FIELD_COLLECT_MODE ).setTo( _mode );
        TableIterator ti = _query.iterator();
        ArrayDatum tables = (ArrayDatum)datum.at( FIELD_TABLES );
        while( ti.hasNext() ) {
            ti.next();
            CompositeDatum table = (CompositeDatum)tables.add();
            table.at( FIELD_TABLE_NO ).setTo( ti.getTableNumber() );
            table.at( FIELD_TABLE_SIGNATURE ).setTo( ti.getTableSignature().getSignature() );
            table.at( FIELD_P1 ).setTo( _p1 );
            table.at( FIELD_P2 ).setTo( _p2 );
            TableIterator.FieldIterator fi = ti.iterator();
            ArrayDatum fields = (ArrayDatum)table.at( FIELD_FIELDS );
            while( fi.hasNext() ) {
                SimpleDatum field = (SimpleDatum)fields.add();
                field.setTo( (Integer)fi.next() );
            }
        }
    }


    private CollectDataReqMsg( final DataQuery _query, final int _securityCode, final int _mode, final NSec _p1, final NSec _p2, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _query, _p1, _p2, _context );


        // create and initialize our datum...
        initDataType();
        props.add( new CP( FIELD_TABLES, new ArrayDataType( FIELD_TABLES, null, TYPE1 ) ) );
        setDatum();
        datum.at( FIELD_SECURITY_CODE ).setTo( _securityCode );
        datum.at( FIELD_COLLECT_MODE ).setTo( _mode );
        TableIterator ti = _query.iterator();
        ArrayDatum tables = (ArrayDatum)datum.at( FIELD_TABLES );
        while( ti.hasNext() ) {
            ti.next();
            CompositeDatum table = (CompositeDatum)tables.add();
            table.at( FIELD_TABLE_NO ).setTo( ti.getTableNumber() );
            table.at( FIELD_TABLE_SIGNATURE ).setTo( ti.getTableSignature().getSignature() );
            table.at( FIELD_P1 ).setTo( _p1 );
            table.at( FIELD_P2 ).setTo( _p2 );
            TableIterator.FieldIterator fi = ti.iterator();
            ArrayDatum fields = (ArrayDatum)table.at( FIELD_FIELDS );
            while( fi.hasNext() ) {
                SimpleDatum field = (SimpleDatum)fields.add();
                field.setTo( (Integer)fi.next() );
            }
        }
    }


    private void initDataType() {
        props.add( new CP( FIELD_SECURITY_CODE, UINT2 ) );
        props.add( new CP( FIELD_COLLECT_MODE,  BYTE  ) );
    }


    /**
     * Creates a new instance of this class to request data collection of all records, oldest to newest, from the tables and fields in the given data
     * query, with the given security code and context.
     *
     * @param _query the tables and fields to be collected
     * @param _securityCode the security code
     * @param _context the communications context
     * @return the new instance of this class
     */
    static public CollectDataReqMsg getAll( final DataQuery _query, final int _securityCode, final Context _context ) {
        return new CollectDataReqMsg( _query, _securityCode, 3, 0, 0, _context );
    }


    /**
     * Creates a new instance of this class to request data collection of all records newer than the given "from" record number, oldest to newest,
     * from the tables and fields in the given data query, with the given security code and context.  Note that the "from" record number need not
     * exist; if it does not the collection will start with the oldest existing record.
     *
     * @param _query the tables and fields to be collected
     * @param _securityCode the security code
     * @param _fromRecordNumber the first record number to collect
     * @param _context the communications context
     * @return the new instance of this class
     */
    static public CollectDataReqMsg getFromRecordNumber( final DataQuery _query, final int _securityCode,
                                                         final int _fromRecordNumber, final Context _context ) {
        return new CollectDataReqMsg( _query, _securityCode, 4, _fromRecordNumber, 0, _context );
    }


    /**
     * Creates a new instance of this class to request data collection of the given number of the most recent records, oldest to newest, from the
     * tables and fields in the given data query, with the given security code and context.
     *
     * @param _query the tables and fields to be collected
     * @param _securityCode the security code
     * @param _numberOfRecords the number of records to collect
     * @param _context the communications context
     * @return the new instance of this class
     */
    static public CollectDataReqMsg getMostRecent( final DataQuery _query, final int _securityCode, final int _numberOfRecords,
                                                   final Context _context ) {
        return new CollectDataReqMsg( _query, _securityCode, 5, _numberOfRecords, 0, _context );
    }


    /**
     * Creates a new instance of this class to request data collection of records starting with the given first record number up to (but not
     * including) the given last record number, oldest to newest, from the tables and fields in the given data query, with the given security code
     * and context.
     *
     * @param _query the tables and fields to be collected
     * @param _securityCode the security code
     * @param _firstRecordNumber the first record number to collect
     * @param _lastRecordNumber the last-plus-one record number to collect
     * @param _context the communications context
     * @return the new instance of this class
     */
    static public CollectDataReqMsg getRangeOfRecordNumbers( final DataQuery _query, final int _securityCode, final int _firstRecordNumber,
                                                             final int _lastRecordNumber, final Context _context ) {
        return new CollectDataReqMsg( _query, _securityCode, 6, _firstRecordNumber, _lastRecordNumber, _context );
    }


    /**
     * Creates a new instance of this class to request data collection of records with timestamps equal or newer than the given first timestamp and
     * older than the given last timestamp, oldest to newest, from the tables and fields in the given data query, with the given security code and
     * context.
     *
     * @param _query the tables and fields to be collected
     * @param _securityCode the security code
     * @param _firstTimestamp the oldest timestamp to collect
     * @param _lastTimestamp the newest timestamp to collect
     * @param _context the communications context
     * @return the new instance of this class
     */
    static public CollectDataReqMsg getRangeOfTimestamps( final DataQuery _query, final int _securityCode, final NSec _firstTimestamp,
                                                          final NSec _lastTimestamp, final Context _context ) {
        return new CollectDataReqMsg( _query, _securityCode, 7, _firstTimestamp, _lastTimestamp, _context );
    }


    /**
     * Creates a new instance of this class to request data collection of a record fragment from the record with the given record number, starting at
     * the given offset, with the given security code and context.
     *
     * @param _query the tables and fields to be collected
     * @param _securityCode the security code
     * @param _recordNumber the record number to collect a fragment from
     * @param _offset the offset to collect a fragment from
     * @param _context the communications context
     * @return the new instance of this class
     */
    static public CollectDataReqMsg getRecordFragment( final DataQuery _query, final int _securityCode, final int _recordNumber,
                                                       final int _offset, final Context _context ) {
        return new CollectDataReqMsg( _query, _securityCode, 8, _recordNumber, _offset, _context );
    }
}
