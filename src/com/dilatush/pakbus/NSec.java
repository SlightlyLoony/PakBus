package com.dilatush.pakbus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Instances of this class represent a moment in time, defined as the elapsed time since midnight on January 1, 1990.  The Campbell Scientific
 * documentation does not mention time zones, so we have assumed that it is GMT.  Instances of this class are threadsafe and immutable.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class NSec {

    final static private Instant EPOCH = Instant.parse( "1990-01-01T00:00:00.000Z" );

    final private int seconds;
    final private int nanoseconds;


    /**
     * Creates a new instance of this class with the given time since midnight January 1, 1990 GMT.
     *
     * @param _seconds the number of seconds since midnight January 1, 1990 GMT
     * @param _nanoseconds the number of nanoseconds within the second since midnight January 1, 1990 GMT
     */
    public NSec( final int _seconds, final int _nanoseconds ) {
        seconds = _seconds;
        nanoseconds = _nanoseconds;
    }


    public Instant asInstant() {
        return EPOCH.plus( Duration.ofSeconds( seconds, nanoseconds ) );
    }


    static public NSec fromInstant( final Instant _instant ) {
        Duration delta = Duration.between( EPOCH, _instant );
        return new NSec( (int) delta.getSeconds(), delta.getNano() );
    }


    public static void main( String[] args ) {

        NSec n = new NSec( 2000000000, 123456789 );
        Instant i = n.asInstant();
        NSec o = NSec.fromInstant( i );

        NSec now = NSec.fromInstant( Instant.now( Clock.systemUTC() ) );

        int x = 0;
    }
}
