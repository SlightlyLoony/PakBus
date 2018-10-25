package com.dilatush.pakbus.example;

import com.dilatush.pakbus.shims.TableDefinitions;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This is a (very) simple test program for the WeatherHawk class.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Test {


    // TEST CODE
    static public void main( String[] _args ) throws InterruptedException {

        Logger LOGGER = Logger.getLogger( Test.class.getSimpleName() );
        LOGGER.info( "WeatherHawk test program started..." );

        WeatherHawk wh = new WeatherHawk( "/dev/cu.usbserial-AC01R521", 1027 );

        TableDefinitions loggerTableDefinitions = wh.getTableDefinitions();

        SiteValues siteValues = wh.getSiteValues();

        Instant loggerTime = wh.getTime();

        Map<String,String> loggerSettings = wh.getAllSettings();

        RealtimeRecord record = wh.collectRealtimeData();

        List<HourlyRecord> hourlies = wh.collectHourlyData( 24 );

        List<DailyRecord> dailies = wh.collectDailyData( 5 );

        // just a place to breakpoint on!
        wh.hashCode();

    }
}
