package com.dilatush.pakbus.example;

import com.dilatush.pakbus.values.Datum;

import java.time.Instant;

/**
 * Instances of this class contain a single record of hourly weather data from the WeatherHawk weather station.  Instance of this class are immutable
 * and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class HourlyRecord {

    final public float   evapotranspiration;             // computed, in mm/day...
    final public float   relativeHumidityAvg;            // in percent...
    final public float   snow;                           // in mm since January 1 (reset then)...
    final public float   snowRainEquivalent;             // in mm since January 1 (reset then)...
    final public float   rain;                           // in mm since January 1 (reset then)...
    final public float   barometricPressure;             // in kilopascals...
    final public float   temperatureMin;                 // in degrees Celsius...
    final public float   temperatureAvg;                 // in degrees Celsius...
    final public float   temperatureMax;                 // in degrees Celsius...
    final public float   solarIrradianceAvg;             // in watts/square meter...
    final public float   windSpeedAvg;                   // in meters/second averaged over 10 seconds...
    final public float   windDirectionAvg;               // origination direction in degrees...
    final public float   windSpeedMax;                   // in meters/second, peak over 10 seconds...
    final public Instant windSpeedMaxTime;               // the time at which the maximum wind speed in this hour occurred...
    final public Instant timestamp;                      // the logger time that this record was created...


    /**
     * Creates a new instance of this class from the given datum representing the value collected from the "data1" table of the WeatherHawk.
     *
     * @param _datum the datum containing weather data from the "Public" record data.
     */
    /*package*/ HourlyRecord( final Datum _datum ) {

        // fetch all our data from the datum and stuff it away...
        solarIrradianceAvg  = (float) _datum.at( "Solar_Avg"                    ).getAsDouble();
        barometricPressure  = (float) _datum.at( "Barometer_KPa"                ).getAsDouble();
        snow                = (float) _datum.at( "Snow_Acc_Yearly"              ).getAsDouble();
        snowRainEquivalent  = (float) _datum.at( "SnowYearly_mm"                ).getAsDouble();
        rain                = (float) _datum.at( "RainYearly_mm"                ).getAsDouble();
        windDirectionAvg    = (float) _datum.at( "WindSpeed_ms_WVc", 1 ).getAsDouble();
        windSpeedAvg        = (float) _datum.at( "WindSpeed_ms_WVc", 0 ).getAsDouble();
        windSpeedMax        = (float) _datum.at( "WindGust_ms_Max"              ).getAsDouble();
        windSpeedMaxTime    =         _datum.at( "WindGust_ms_TMx"              ).getAsNSec().asInstant();
        relativeHumidityAvg = (float) _datum.at( "RH_Avg"                       ).getAsDouble();
        temperatureMin      = (float) _datum.at( "AirTemp_C_Min"                ).getAsDouble();
        temperatureAvg      = (float) _datum.at( "AirTemp_C_Avg"                ).getAsDouble();
        temperatureMax      = (float) _datum.at( "AirTemp_C_Max"                ).getAsDouble();
        evapotranspiration  = (float) _datum.at( "ETo"                          ).getAsDouble();
        timestamp           =         _datum.at( "Timestamp"                    ).getAsNSec().asInstant();
    }
}
