package com.dilatush.pakbus.example;

import com.dilatush.pakbus.values.Datum;

/**
 * Instances of this class contain a single record of "realtime" (actually, collected at 10 second intervals) weather data from the WeatherHawk
 * weather station.  Instance of this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DailyRecord {

    final public PrecipitationType precipitationType;
    final public float solarIrradiance;                  // in watts/square meter...
    final public float barometricPressure;               // in kilopascals...
    final public float snow;                             // in mm since January 1 (reset then)...
    final public float snowRainEquivalent;               // in mm since January 1 (reset then)...
    final public float rain;                             // in mm since January 1 (reset then)...
    final public float windDirection;                    // origination direction in degrees...
    final public float windSpeedAvg;                     // in meters/second averaged over 10 seconds...
    final public float windSpeedMax;                     // in meters/second, peak over 10 seconds...
    final public float relativeHumidity;                 // in percent...
    final public float temperature;                      // in degrees Celsius...
    final public float evapotranspiration;               // computed, in mm/day...


    /**
     * Creates a new instance of this class from the given datum representing the value collected from the "Public" table of the WeatherHawk.
     *
     * @param _datum the datum containing weather data from the "Public" record data.
     */
    /*package*/ DailyRecord( final Datum _datum ) {

        // fetch all our data from the datum and stuff it away...
        precipitationType  = PrecipitationType.fromValue( (float) _datum.at( "Percip_Type" ).getAsDouble() );
        solarIrradiance    = (float) _datum.at( "Solar"           ).getAsDouble();
        barometricPressure = (float) _datum.at( "Barometer_KPa"   ).getAsDouble();
        snow               = (float) _datum.at( "Snow_Acc_Yearly" ).getAsDouble();
        snowRainEquivalent = (float) _datum.at( "SnowYearly_mm"   ).getAsDouble();
        rain               = (float) _datum.at( "RainYearly_mm"   ).getAsDouble();
        windDirection      = (float) _datum.at( "WindDirect_deg"  ).getAsDouble();
        windSpeedAvg       = (float) _datum.at( "WindSpeed_ms"    ).getAsDouble();
        windSpeedMax       = (float) _datum.at( "WindGust_ms"     ).getAsDouble();
        relativeHumidity   = (float) _datum.at( "RH"              ).getAsDouble();
        temperature        = (float) _datum.at( "AirTemp_C"       ).getAsDouble();
        evapotranspiration = (float) _datum.at( "DailyETo_mm"     ).getAsDouble();
    }
}
