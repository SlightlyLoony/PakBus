package com.dilatush.pakbus.example;

/**
 * Enumerates precipitation types as reported by a WeatherHawk weather station.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum PrecipitationType {

    Invalid, None, Rain, Snow;

    public static PrecipitationType fromValue( final float _value ) {
        if( _value ==  0 ) return None;
        if( _value == 60 ) return Rain;
        if( _value == 70 ) return Snow;
        return Invalid;
    }
}
