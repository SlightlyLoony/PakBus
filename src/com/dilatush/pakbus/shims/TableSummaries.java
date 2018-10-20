package com.dilatush.pakbus.shims;

import com.dilatush.pakbus.NSec;
import com.dilatush.pakbus.comms.Signature;
import com.dilatush.pakbus.types.ArrayDataType;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.CompositeDataType;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.ArrayDatum;
import com.dilatush.pakbus.values.CompositeDatum;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dilatush.pakbus.types.DataTypes.*;

/**
 * Instances of this class represent the tables (and fields) in a datalogger as retrieved by reading the ".TDF" virtual file from a datalogger.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class TableSummaries {

    final static /* package */ String FIELD_TYPE0             = "TYPE0";
    final static /* package */ String FIELD_TYPE1             = "TYPE1";
    final static /* package */ String FIELD_TYPE2             = "TYPE2";
    final static /* package */ String FIELD_TDF_VERSION       = "TdfVersion";
    final static /* package */ String FIELD_TABLE_NAME        = "TableName";
    final static /* package */ String FIELD_TABLE_SIZE        = "TableSize";
    final static /* package */ String FIELD_SIGNATURE         = "TableSignature";
    final static /* package */ String FIELD_TABLES            = "Tables";

    final static /* package */ String FIELD_OS_VERSION        = "OsVersion";
    final static /* package */ String FIELD_OS_SIGNATURE      = "OsSignature";
    final static /* package */ String FIELD_SERIAL_NO         = "SerialNo";
    final static /* package */ String FIELD_POWER_UP_PROG     = "PowerUpProgram";
    final static /* package */ String FIELD_COMPILE_STATE     = "CompileState";
    final static /* package */ String FIELD_PROGRAM_NAME      = "ProgramName";
    final static /* package */ String FIELD_PROGRAM_SIGNATURE = "ProgramSignature";
    final static /* package */ String FIELD_COMPILE_TIME      = "CompileTime";
    final static /* package */ String FIELD_COMPILE_RESULT    = "CompileResult";
    final static /* package */ String FIELD_MODEL_NO          = "ModelNo";

    final static private CompositeDataType TYPE2 = new CompositeDataType( FIELD_TYPE2, null,
            new CP( FIELD_TABLE_NAME,        ASCIIZ ),
            new CP( FIELD_TABLE_SIZE,        UINT4  ),
            new CP( FIELD_SIGNATURE,         UINT2  ) );
    final static private ArrayDataType TYPE1 = new ArrayDataType( FIELD_TYPE1, null, TYPE2 );
    final static private CompositeDataType TYPE0 = new CompositeDataType( FIELD_TYPE0, null,
            new CP( FIELD_TDF_VERSION,       BYTE   ),
            new CP( FIELD_OS_VERSION,        ASCIIZ ),
            new CP( FIELD_OS_SIGNATURE,      UINT2  ),
            new CP( FIELD_SERIAL_NO,         ASCIIZ ),
            new CP( FIELD_POWER_UP_PROG,     ASCIIZ ),
            new CP( FIELD_COMPILE_STATE,     BYTE   ),
            new CP( FIELD_PROGRAM_NAME,      ASCIIZ ),
            new CP( FIELD_PROGRAM_SIGNATURE, UINT2  ),
            new CP( FIELD_COMPILE_TIME,      NSEC   ),
            new CP( FIELD_COMPILE_RESULT,    ASCIIZ ),
            new CP( FIELD_MODEL_NO,          ASCIIZ ),
            new CP( FIELD_TABLES,            TYPE1  ) );

    final public String        osVersion;
    final public Signature     osSignature;
    final public String        serialNumber;
    final public String        powerUpProgram;
    final public ProgramStatus programStatus;
    final public String        programName;
    final public Signature     programSignature;
    final public NSec          compileTime;
    final public String        compileResult;
    final public String        modelNumber;

    final private Map<String,TableSummary> byName;   // table summaries by the table name
    final private List<TableSummary>       byNumber; // table summaries by the table number (1-based)


    /**
     * Creates a new instance of this class from a datalogger ".TDF" file contents contained in the given byte buffer.
     *
     * @param _buffer the contents of a datalogger ".TDF" file
     */
    public TableSummaries( final ByteBuffer _buffer ) {

        // sanity checks...
        Checks.required( _buffer );
        _buffer.position( 0 );
        if( (_buffer.get( 0 ) & 0xFF) != 0x11 )
            throw new IllegalArgumentException( "Incorrect table summaries file version" );

        // some setup...
        byName = new HashMap<>();
        byNumber = new ArrayList<>();

        // parse the input buffer...
        CompositeDatum datum = new CompositeDatum( TYPE0 );
        datum.set( new BitBuffer( _buffer ) );

        // set the datalogger status items...
        osVersion        = datum.at( FIELD_OS_VERSION ).getAsString();
        osSignature      = new Signature( datum.at( FIELD_OS_SIGNATURE ).getAsInt() );
        serialNumber     = datum.at( FIELD_SERIAL_NO ).getAsString();
        powerUpProgram   = datum.at( FIELD_POWER_UP_PROG ).getAsString();
        programStatus    = ProgramStatus.decode( datum.at( FIELD_COMPILE_STATE ).getAsInt() );
        programName      = datum.at( FIELD_PROGRAM_NAME ).getAsString();
        programSignature = new Signature( datum.at( FIELD_PROGRAM_SIGNATURE ).getAsInt() );
        compileTime      = datum.at( FIELD_COMPILE_TIME ).getAsNSec();
        compileResult    = datum.at( FIELD_COMPILE_RESULT ).getAsString();
        modelNumber      = datum.at( FIELD_MODEL_NO ).getAsString();

        // loop through all the table summaries to create table table summary records...
        ArrayDatum tables = (ArrayDatum)datum.at( FIELD_TABLES );
        for( int i = 0; i < tables.elements(); i++ ) {

            CompositeDatum tableDatum = (CompositeDatum)tables.get( i );
            TableSummary ts = new TableSummary( tableDatum, byNumber.size() + 1 );
            byNumber.add( ts );
            byName.put( ts.name, ts );
        }
    }


    /**
     * Returns the table summary for the table with the given name, or null if there is no table by that name.
     *
     * @param _name the name of the table to return a summary for
     * @return the table summary
     */
    public TableSummary getTableSummary( final String _name ) {
        Checks.required( _name );
        return byName.get( _name );
    }


    /**
     * Returns the table summary at the given one-based index.
     *
     * @param _index the table summary index
     * @return the table summary at the given index
     */
    public TableSummary getTableSummary( final int _index ) {
        if( (_index < 1) || (_index > byNumber.size() ) )
            throw new IllegalArgumentException( "Index is out of range: " + _index + " (must be in the range [1.." + byNumber.size() + "])" );
        return byNumber.get( _index - 1 );
    }
}
