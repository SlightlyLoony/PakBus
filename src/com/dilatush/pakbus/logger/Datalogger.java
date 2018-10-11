package com.dilatush.pakbus.logger;

/**
 * Instances of this class hold information a particular datalogger.  Instances of this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Datalogger {

    final public String     name;            // human-readable name for the datalogger (e.g., "Cowabunga")
    final public String     type;            // human-readable type for the datalogger (e.g., "WeatherHawk")
    final public int        physicalAddress; // PakBus physical address for this datalogger
    final public TableDef[] tables;          // table definitions for all tables in the datalogger (table number is 1 + index)


    public Datalogger( final String _name, final String _type, final int _physicalAddress, final TableDef[] _tables ) {

        name            = _name;
        type            = _type;
        physicalAddress = _physicalAddress;
        tables          = _tables;
    }
}
