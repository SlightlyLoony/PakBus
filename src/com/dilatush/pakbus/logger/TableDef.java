package com.dilatush.pakbus.logger;

import com.dilatush.pakbus.NSec;
import com.dilatush.pakbus.types.PakBusType;

/**
 * Instances of this class describe a particular table in a Campbell Scientific datalogger.  Instances of this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class TableDef {

    final public String     name;        // name of the table in the datalogger
    final public int        size;        // number of records allocated for this table in the datalogger
    final public PakBusType timeType;    // the type (Sec, USec, or NSec) for the timestamps in this table
    final public NSec       timeInto;    // the time into the interval that records are to be stored (zero for event-driven tables)
    final public NSec       interval;    // the interval at which records are stored in the table (zero for event-driven tables)
    final public FieldDef[] fields;      // list of fields in the table (field number is 1 + index)


    public TableDef( final String _name, final int _size, final PakBusType _timeType, final NSec _timeInto, final NSec _interval,
                     final FieldDef[] _fields ) {

        name     = _name;
        size     = _size;
        timeType = _timeType;
        timeInto = _timeInto;
        interval = _interval;
        fields   = _fields;
    }
}
