package com.dilatush.pakbus.example;

import com.dilatush.pakbus.values.Datum;

import java.time.Instant;

/**
 * Instances of this class contain a single record of daily weather data from the WeatherHawk weather station.  Instance of this class are immutable
 * and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DailyRecord {

    final public float   evapotranspiration;             // computed, in mm/day...
    final public float   snow;                           // in mm since January 1 (reset then)...
    final public float   snowRainEquivalent;             // in mm since January 1 (reset then)...
    final public float   rain;                           // in mm since January 1 (reset then)...
    final public float   temperatureMin;                 // in degrees Celsius...
    final public float   temperatureMax;                 // in degrees Celsius...
    final public float   windSpeedMax;                   // in meters/second, peak for the day...
    final public float   windGustMax;                    // in meters/second, peak for the day...
    final public Instant windGustMaxTime;                // the time at which the maximum gust speed in this day occurred...
    final public Instant timestamp;                      // the logger time that this record was created...


    /**
     * Creates a new instance of this class from the given datum representing the value collected from the "data1" table of the WeatherHawk.
     *
     * @param _datum the datum containing weather data from the "Public" record data.
     */
    /*package*/ DailyRecord( final Datum _datum ) {

        // fetch all our data from the datum and stuff it away...
        snow                = (float) _datum.at( "Snow_Acc_Yearly"              ).getAsDouble();
        snowRainEquivalent  = (float) _datum.at( "SnowYearly_mm"                ).getAsDouble();
        rain                = (float) _datum.at( "RainYearly_mm"                ).getAsDouble();
        windSpeedMax        = (float) _datum.at( "WindSpeed_ms_Max"             ).getAsDouble();
        windGustMax         = (float) _datum.at( "WindGust_ms_Max"              ).getAsDouble();
        windGustMaxTime     =         _datum.at( "WindGust_ms_TMx"              ).getAsNSec().asInstant();
        temperatureMin      = (float) _datum.at( "AirTemp_C_Min"                ).getAsDouble();
        temperatureMax      = (float) _datum.at( "AirTemp_C_Max"                ).getAsDouble();
        evapotranspiration  = (float) _datum.at( "DailyETo_mm"                  ).getAsDouble();
        timestamp           =         _datum.at( "Timestamp"                    ).getAsNSec().asInstant();
    }
}
