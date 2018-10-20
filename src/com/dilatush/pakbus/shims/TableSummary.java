package com.dilatush.pakbus.shims;

import com.dilatush.pakbus.comms.Signature;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.CompositeDatum;

import static com.dilatush.pakbus.shims.TableSummaries.FIELD_SIGNATURE;
import static com.dilatush.pakbus.shims.TableSummaries.FIELD_TABLE_NAME;
import static com.dilatush.pakbus.shims.TableSummaries.FIELD_TABLE_SIZE;

/**
 * Instances of this class represent one the tables (and fields) in a datalogger as retrieved by reading a "*.TDF" file from a datalogger.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class TableSummary {

    final public int       index;   // the 1-based index for this table
    final public String    name;
    final public int       size;
    final public Signature signature;


    public TableSummary( final CompositeDatum _datum, final int _index ) {

        // sanity checks...
        Checks.required( _datum );

        // populate the non-field stuff...
        index     = _index;
        name      =     _datum.at( FIELD_TABLE_NAME ).getAsString();
        size      =     _datum.at( FIELD_TABLE_SIZE ).getAsInt();
        signature = new Signature( _datum.at( FIELD_SIGNATURE ).getAsInt() );
    }
}
