package com.dilatush.pakbus.example;

import com.dilatush.pakbus.values.Datum;

/**
 * Instances of this class represent the contents of the single-record "SiteVal" table on a WeatherHawk.  Instances of this class are immutable and
 * threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class SiteValues {

    final public float altitude;         // altitude (above sea level) of the WeatherHawk in meters...
    final public float latitude;         // latitude of the WeatherHawk in degrees...
    final public float longitude;        // longitude of the WeatherHaWK in degrees...
    final public float barometerOffset;  // pressure offset (because of altitude) in kilopascals...
    final public float data1Interval;    // interval between new records in table "data1", in minutes...


    /**
     * Creates a new instance of this class from the given datum representing the value collected from the "SiteVal" table of the WeatherHawk.
     *
     * @param _datum the datum containing weather station site data from the "SiteVal" record data.
     */
    /*package*/ SiteValues( final Datum _datum ) {

        // fetch all our data from the datum and stuff it away...
        altitude        = (float) _datum.at( "Altitude_m"   ).getAsDouble();
        latitude        = (float) _datum.at( "Latitude"     ).getAsDouble();
        longitude       = (float) _datum.at( "Longitude"    ).getAsDouble();
        barometerOffset = (float) _datum.at( "BPoffset_KPa" ).getAsDouble();
        data1Interval   = (float) _datum.at( "Int_timer"    ).getAsDouble();
    }


    /**
     * Returns true if the two given numbers are within 0.000001 of each other.
     *
     * @param _a one number
     * @param _b the other number
     * @return true if the two given numbers are within 0.000001 of each other
     */
    static public boolean isCloseEnough( final float _a, final float _b ) {
        return Math.abs( _a - _b ) < 0.000001;
    }
}
