package com.dilatush.pakbus.objects;

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
public class TableDefinitions {

    final static /* package */ String FIELD_TYPE0          = "TYPE0";
    final static /* package */ String FIELD_TYPE1          = "TYPE1";
    final static /* package */ String FIELD_TYPE2          = "TYPE2";
    final static /* package */ String FIELD_TYPE3          = "TYPE3";
    final static /* package */ String FIELD_TYPE4          = "TYPE4";
    final static /* package */ String FIELD_TYPE5          = "TYPE5";
    final static /* package */ String FIELD_TDF_VERSION    = "TdfVersion";
    final static /* package */ String FIELD_TABLE_NAME     = "TableName";
    final static /* package */ String FIELD_TABLE_SIZE     = "TableSize";
    final static /* package */ String FIELD_TIME_TYPE      = "TimeType";
    final static /* package */ String FIELD_TIME_INTO      = "TimeInto";
    final static /* package */ String FIELD_INTERVAL       = "Interval";
    final static /* package */ String FIELD_READ_ONLY      = "ReadOnly";
    final static /* package */ String FIELD_FIELD_TYPE     = "FieldType";
    final static /* package */ String FIELD_FIELD_NAME     = "FieldName";
    final static /* package */ String FIELD_RESERVED       = "Reserved";
    final static /* package */ String FIELD_PROCESSING     = "Processing";
    final static /* package */ String FIELD_UNITS          = "Units";
    final static /* package */ String FIELD_DESCRIPTION    = "Description";
    final static /* package */ String FIELD_BEGIN_INDEX    = "BeginIndex";
    final static /* package */ String FIELD_PIECE_SIZE     = "PieceSize";
    final static /* package */ String FIELD_DIMENSIONS     = "Dimensions";
    final static /* package */ String FIELD_FIELDS         = "Fields";
    final static /* package */ String FIELD_TABLES         = "Tables";

    final static private ArrayDataType TYPE5 = new ArrayDataType( FIELD_TYPE5, null, UINT4, UINT4 );
    final static private CompositeDataType TYPE4 = new CompositeDataType( FIELD_TYPE4, null,
            new CP( FIELD_READ_ONLY,   BIT    ),
            new CP( FIELD_FIELD_TYPE,  BITS7  ),
            new CP( FIELD_FIELD_NAME,  ASCIIZ ),
            new CP( FIELD_RESERVED,    BYTE   ),
            new CP( FIELD_PROCESSING,  ASCIIZ ),
            new CP( FIELD_UNITS,       ASCIIZ ),
            new CP( FIELD_DESCRIPTION, ASCIIZ ),
            new CP( FIELD_BEGIN_INDEX, UINT4  ),
            new CP( FIELD_PIECE_SIZE,  UINT4  ),
            new CP( FIELD_DIMENSIONS,  TYPE5  ) );
    final static private ArrayDataType TYPE3 = new ArrayDataType( FIELD_TYPE3, null, TYPE4, BYTE );
    final static private CompositeDataType TYPE2 = new CompositeDataType( FIELD_TYPE2, null,
            new CP( FIELD_TABLE_NAME, ASCIIZ ),
            new CP( FIELD_TABLE_SIZE, UINT4  ),
            new CP( FIELD_TIME_TYPE,  BYTE   ),
            new CP( FIELD_TIME_INTO,  NSEC   ),
            new CP( FIELD_INTERVAL,   NSEC   ),
            new CP( FIELD_FIELDS,     TYPE3  ) );
    final static private ArrayDataType TYPE1 = new ArrayDataType( FIELD_TYPE1, null, TYPE2 );
    final static private CompositeDataType TYPE0 = new CompositeDataType( FIELD_TYPE0, null,
            new CP( FIELD_TDF_VERSION, BYTE  ),
            new CP( FIELD_TABLES,      TYPE1 ) );

    final private Map<String,TableDefinition> byName;   // table definitions by the table name
    final private List<TableDefinition>       byNumber; // table definitions by the table number (1-based)


    /**
     * Creates a new instance of this class from a datalogger ".TDF" file contents contained in the given byte buffer.
     *
     * @param _buffer the contents of a datalogger ".TDF" file
     */
    public TableDefinitions( final ByteBuffer _buffer ) {

        // sanity checks...
        Checks.required( _buffer );
        _buffer.position( 0 );
        if( (_buffer.get( 0 ) & 0xFF) != 1 )
            throw new IllegalArgumentException( "Incorrect table definitions file version" );

        // some setup...
        byName = new HashMap<>();
        byNumber = new ArrayList<>();

        // parse the input buffer...
        CompositeDatum datum = new CompositeDatum( TYPE0 );
        datum.set( new BitBuffer( _buffer ) );

        // loop through all the table definitions to create table definition records...
        ArrayDatum tables = (ArrayDatum)datum.at( FIELD_TABLES );
        for( int i = 0; i < tables.elements(); i++ ) {

            CompositeDatum tableDatum = (CompositeDatum)tables.get( i );
            TableDefinition td = new TableDefinition( tableDatum, byNumber.size() + 1 );
            byNumber.add( td );
            byName.put( td.name, td );
        }
    }


    /**
     * Returns the table definition for the table with the given name, or null if there is no table by that name.
     *
     * @param _name the name of the table to return a definition for
     * @return the table definition
     */
    public TableDefinition getTableDef( final String _name ) {
        Checks.required( _name );
        return byName.get( _name );
    }


    /**
     * Returns the table definition at the given one-based index.
     *
     * @param _index the table definition index
     * @return the table definition at the given index
     */
    public TableDefinition getTableDef( final int _index ) {
        if( (_index < 1) || (_index > byNumber.size() ) )
            throw new IllegalArgumentException( "Index is out of range: " + _index + " (must be in the range [1.." + byNumber.size() + "])" );
        return byNumber.get( _index - 1 );
    }
}
