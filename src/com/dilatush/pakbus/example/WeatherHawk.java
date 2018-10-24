package com.dilatush.pakbus.example;

import com.dilatush.pakbus.Address;
import com.dilatush.pakbus.Node;
import com.dilatush.pakbus.app.Application;
import com.dilatush.pakbus.app.Datalogger;
import com.dilatush.pakbus.comms.PacketTransceiver;
import com.dilatush.pakbus.comms.SerialPacketTransceiver;
import com.dilatush.pakbus.comms.SerialTransceiver;
import com.dilatush.pakbus.shims.DataQuery;
import com.dilatush.pakbus.shims.TableDefinition;
import com.dilatush.pakbus.shims.TableDefinitions;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.types.PakBusType;
import com.dilatush.pakbus.values.Datum;
import com.dilatush.pakbus.values.SimpleDatum;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.dilatush.pakbus.comms.PortSerialTransceiver.*;
import static com.dilatush.pakbus.example.SiteValues.isCloseEnough;
import static java.lang.Thread.sleep;

/**
 * Instances of this class implement an interface to a WeatherHawk weather station that communicates by spread spectrum radio.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class WeatherHawk {

    final static public int APP_ADDRESS = 4010;   // the PakBus address of our application...
    final static public int APP_NODE_ID = 1;      // the PakBus Node ID of our application...

    final private Application app;
    final private Datalogger logger;


    /**
     * Creates a new instance of this class using a WeatherHawk connected through a radio on the serial port with the given name, at the given
     * physical address (which is the same as the node ID).
     *
     * @param _serialPortName the name of the serial port to communicate to the WeatherHawk through
     * @param _weatherhawkAddress the physical address (and node ID) of the WeatherHawk on the PakBus
     */
    public WeatherHawk( final String _serialPortName, final int _weatherhawkAddress ) {

        // some setup...
        SerialTransceiver st = getSerialTransceiver( _serialPortName, 9600, 8, STOP_BITS_ONE, PARITY_NONE, FLOW_CTRL_NONE );
        PacketTransceiver pt = new SerialPacketTransceiver( st );
        Node appAddr = new Node( new Address( APP_ADDRESS ), APP_NODE_ID );

        // get our app...
        app = new Application( pt, appAddr );

        // now get our WeatherHawk datalogger...
        Node loggerAddr = new Node( new Address( _weatherhawkAddress ), _weatherhawkAddress );
        logger = new Datalogger( app, "WeatherHawk 621", loggerAddr );
        app.register( logger );
    }


    /**
     * Collect and return realtime weather data from the WeatherHawk.
     *
     * @return the realtime weather data collected, or null if there was a problem
     */
    public RealtimeRecord collectRealtimeData() {
        TableDefinitions tds = logger.getTableDefinitions();
        TableDefinition publicTable = tds.getTableDef( "Public" );
        DataQuery query = new DataQuery( publicTable.index, publicTable.signature );
        List<Datum> record = logger.collectMostRecent( query, 1 );
        if( record == null )
            return null;
        return new RealtimeRecord( record.get( 0 ) );
    }


    /**
     * Collect and return the given number of the most recent hourly weather data records from the WeatherHawk.
     *
     * @return the hourly weather data collected, or null if there was a problem
     */
    public List<HourlyRecord> collectHourlyData( final int _records ) {

        // get the data in the form of Datum instances...
        TableDefinitions tds = logger.getTableDefinitions();
        TableDefinition data1Table = tds.getTableDef( "data1" );
        DataQuery query = new DataQuery( data1Table.index, data1Table.signature );
        List<Datum> record = logger.collectMostRecent( query, _records );
        if( record == null )
            return null;

        // now turn it into hourly records...
        List<HourlyRecord> result = new ArrayList<>();
        record.forEach( (item) -> result.add( new HourlyRecord( item ) ) );
        return result;
    }


    /**
     * Read and return the WeatherHawk's site values.
     *
     * @return the WeatherHawk's site values
     */
    public SiteValues getSiteValues() {
        TableDefinitions tds = logger.getTableDefinitions();
        TableDefinition publicTable = tds.getTableDef( "Public" );
        DataQuery query = new DataQuery( publicTable.index, publicTable.signature );
        List<Datum> record = logger.collectMostRecent( query, 1 );
        if( record == null )
            return null;
        return new SiteValues( record.get( 0 ) );
    }


    /**
     * Sets the WeatherHawk's site values to the given values.  Note that this command can take up to a minute or so, depending on how many attempts
     * are required to get all the parameters set correctly.  Additional note: the altitude setting and barometric offset settings independently
     * adjust the barometric pressure readings collected.  Generally you would use one and leave the other at zero.
     *
     * @param _altitude the WeatherHawk station's altitude in meters
     * @param _latitude the WeatherHawk station's latitude in degrees
     * @param _longitude the WeatherHawk station's longitude in degrees
     * @param _barometerOffset the WeatherHawk station's barometric offset in kilopascals
     */
    public void setSiteValues( final double _altitude, final double _latitude, final double _longitude, final double _barometerOffset ) {

        // loop in here until we succeed...
        boolean allGood = false;
        do {
            // get the current site values, so we can tell what needs changing...
            SiteValues current = getSiteValues();

            // see if anything needs changing...
            allGood = isCloseEnough( current.altitude, (float) _altitude ) &&
                      isCloseEnough( current.latitude, (float) _latitude ) &&
                      isCloseEnough( current.longitude, (float) _longitude ) &&
                      isCloseEnough( current.barometerOffset, (float) _barometerOffset );

            if( !allGood ) {

                // see what's wrong and fix it...
                if( !isCloseEnough( current.altitude, (float) _altitude ) )
                    setPublicFloatValue( "Altitude_m", (float) _altitude );
                if( !isCloseEnough( current.latitude, (float) _latitude ) )
                    setPublicFloatValue( "Latitude", (float) _latitude );
                if( !isCloseEnough( current.longitude, (float) _longitude ) )
                    setPublicFloatValue( "Longitude", (float) _longitude );
                if( !isCloseEnough( current.barometerOffset, (float) _barometerOffset ) )
                    setPublicFloatValue( "BPoffset_KPa", (float) _barometerOffset );

                // tell the thing to save the values...
                setPublicFloatValue( "SaveSite", 1 );

                // wait ten seconds for the "SaveSite" setting to be effective...
                try {
                    sleep( 10000 );
                }
                catch( InterruptedException _e ) {
                    // naught to do here...
                }
            }
        }
        while( !allGood );
    }


    /**
     * Set the given field name in the "Public" table to the given value.
     *
     * @param _fieldName the field name in the "Public" table to set
     * @param _value the value to set the field to
     */
    private void setPublicFloatValue( final String _fieldName, final float _value ) {
        Datum field = new SimpleDatum( DataTypes.IEEE4 );
        field.setTo( _value );
        logger.setValues( "Public", _fieldName, field );
    }


    /**
     * Calibrate the datalogger's clock against this computer's clock, adjusting the datalogger's clock to match within one second.
     */
    public void calibrateClock() {
        logger.calibrateClock();
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
        return logger.setValues( _tableName, _fieldName, _values );
    }


    /**
     * Returns the value in the given table and field as the given type.  If the swath is one, the value is returned as the single value.  Otherwise,
     * the value is returned as an array of the given field type.
     *
     * @param _tableName the table name to get the value from
     * @param _fieldName the field name to get the value from
     * @param _fieldType the type to convert the returned value to
     * @param _swath the number of values to return (>1 for array fields)
     * @return the datum containing the value requested, or null if there was an error
     */
    public Datum getValues( final String _tableName, final String _fieldName, final PakBusType _fieldType, final int _swath ) {
        return logger.getValues( _tableName, _fieldName, _fieldType, _swath );
    }


    /**
     * Returns the value in the given table and field as the given type in the returned datum.
     *
     * @param _tableName the table name to get the value from
     * @param _fieldName the field name to get the value from
     * @param _fieldType the type to convert the returned value to
     * @return the datum containing the value requested, or null if there was an error
     */
    public Datum getValues( final String _tableName, final String _fieldName, final PakBusType _fieldType ) {
        return logger.getValues( _tableName, _fieldName, _fieldType );
    }


    /**
     * Returns all settings in the datalogger as a map of setting names to their values.
     *
     * @return a map of setting names to their values
     */
    public Map<String, String> getAllSettings() {
        return logger.getAllSettings();
    }


    /**
     * Returns the values of the given setting names as a map of the setting names to their values.
     *
     * @param _settingNames the list of setting names to retrieve
     * @return a map of setting names to their values
     */
    public Map<String, String> getSettings( final List<String> _settingNames ) {
        return logger.getSettings( _settingNames );
    }


    /**
     * Returns the current time in the datalogger, blocking until the request is sent and the response received.  Returns null if the request failed.
     *
     * @return the current time in the datalogger
     */
    public Instant getTime() {
        return logger.getTime();
    }


    /**
     * Returns the table definitions from this datalogger, reading them from the datalogger if necessary.
     *
     * @return the table definitions from this datalogger
     */
    public TableDefinitions getTableDefinitions() {
        return logger.getTableDefinitions();
    }


    static public void main( String[] _args ) throws InterruptedException {

        WeatherHawk wh = new WeatherHawk( "/dev/cu.usbserial-AC01R521", 1027 );

        TableDefinitions loggerTableDefinitions = wh.getTableDefinitions();

        // bo 16.4
        // alt 1481.938
        wh.setSiteValues( 1481.938, 41.58389560, -111.84003310, 0 );
        sleep( 10000 );
        SiteValues siteValues = wh.getSiteValues();

        Instant loggerTime = wh.getTime();

        Map<String,String> loggerSettings = wh.getAllSettings();

        RealtimeRecord record = wh.collectRealtimeData();

        List<HourlyRecord> hourlies = wh.collectHourlyData( 24 );

        wh.hashCode();


        // try setting and returning a value...
//        logger.getTableDefinitions();
//        Datum saver = new SimpleDatum( DataTypes.IEEE4 );
//        saver.setTo( 1 );
//        logger.setValues( "Public", "SaveSite", saver );
//        Datum answer;
//        answer = logger.getValues( "Status", "OSversion", PakBusType.ASCIIZ );
//        Datum altitude = new SimpleDatum( DataTypes.IEEE4 );
//        altitude.setTo( 1473 );
//        logger.setValues( "SiteVal", "Altitude_m", altitude );
//        answer = logger.getValues( "SiteVal", "Altitude_m", PakBusType.IEEE4 );
//
//
//        // calibrate the clock...
//        logger.calibrateClock();
//
//        // get the time...
//        Instant time = logger.getTime();
//        Duration error = Duration.between( Instant.now(), time );
//
//        // get the settings...
//        Map<String,String> settings = logger.getAllSettings();
//        List<String> settingNames = new ArrayList<>();
//
//        // collect most recent record from data2...
//        TableDefinitions defs = logger.getTableDefinitions();
//        TableDefinition pub = defs.getTableDef( "Public" );
//        DataQuery query = new DataQuery( pub.index, pub.signature );
//        TableDefinition data1 = defs.getTableDef( "data1" );
//        DataQuery query2 = new DataQuery( data1.index, data1.signature );
//
//        NSec startTS = new NSec( Instant.now().minus( 50, ChronoUnit.HOURS ) );
//        NSec endTS = new NSec( Instant.now().minus( 49, ChronoUnit.HOURS ) );
//        List<Datum> records2 = logger.collectRangeOfTimestamps( query2, startTS, endTS );
//
//        while( true ) {
//            List<Datum> records = logger.collectMostRecent( query, 1 );
//            if( records == null ) break;
//
//            Datum datum = records.get( 0 );
//
//            pp( "AirTemp_C", datum );
//            pp( "RH", datum );
//            pp( "WindSpeed_ms", datum );
//            pp( "WindDirect_deg", datum );
//            pp( "Barometer_KPa", datum );
//            pp( "Solar", datum );
//            pp( "BatVolt_V", datum );
//            pp( "RainYearly_mm", datum );
//            pp( "Snow_Acc_Yearly", datum );
//            pp( "SnowYearly_mm", datum );
//            Log.logLn( "--------------------------" );
//
//            sleep(9000 );
//        }
//
//        st.hashCode();
//    }
//
//
//    private static void pp( final String _name, final Datum _datum ) {
//
//        Datum value = _datum.at( _name );
//        Log.logLn( _name + ": " + value.getAsDouble() );
    }
}
