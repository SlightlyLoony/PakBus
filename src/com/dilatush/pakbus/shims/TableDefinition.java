package com.dilatush.pakbus.shims;

import com.dilatush.pakbus.values.NSec;
import com.dilatush.pakbus.comms.Signature;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.ArrayDatum;
import com.dilatush.pakbus.values.CompositeDatum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dilatush.pakbus.shims.TableDefinitions.*;

/**
 * Instances of this class represent one the tables (and fields) in a datalogger as retrieved by reading a "*.TDF" file from a datalogger.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class TableDefinition {

    final public int       index;   // the 1-based index for this table
    final public String    name;
    final public int       size;
    final public int       timeType;
    final public NSec      timeInto;
    final public NSec      interval;
    final public Signature signature;

    final private Map<String,FieldDefinition> byName;   // field definitions by the table name
    final private List<FieldDefinition>       byNumber; // field definitions by the table number (1-based)


    public TableDefinition( final CompositeDatum _datum, final int _index ) {

        // sanity checks...
        Checks.required( _datum );

        // some setup...
        byName = new HashMap<>();
        byNumber = new ArrayList<>();

        // populate the non-field stuff...
        index = _index;
        name =     _datum.at( FIELD_TABLE_NAME ).getAsString();
        size =     _datum.at( FIELD_TABLE_SIZE ).getAsInt();
        timeType = _datum.at( FIELD_TIME_TYPE  ).getAsInt();
        timeInto = _datum.at( FIELD_TIME_INTO  ).getAsNSec();
        interval = _datum.at( FIELD_INTERVAL   ).getAsNSec();
        signature = new Signature( _datum.getAsByteBuffer() );

        // now get the field definitions...
        ArrayDatum fields = (ArrayDatum)_datum.at( FIELD_FIELDS );
        for( int i = 0; i < fields.elements(); i++ ) {

            CompositeDatum fieldDatum = (CompositeDatum)fields.get( i );
            FieldDefinition fieldDef = new FieldDefinition( fieldDatum, byNumber.size() + 1 );
            byNumber.add( fieldDef );
            byName.put( fieldDef.name, fieldDef );
        }
    }


    /**
     * Returns the number of elements in this list.  If this list contains
     * more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this list
     */
    public int fieldSize() {
        return byNumber.size();
    }


    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public FieldDefinition getField( final int index ) {
        return byNumber.get( index );
    }
}
