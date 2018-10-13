package com.dilatush.pakbus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Instances of this class represent a moment in time, defined as the elapsed time since midnight on January 1, 1990.  The Campbell Scientific
 * documentation does not mention time zones, so we have assumed that it is GMT.  Instances of this class are threadsafe and immutable.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class NSec {

    final static public NSec ZERO = new NSec( 0, 0 );

    final static private Instant EPOCH = Instant.parse( "1990-01-01T00:00:00.000Z" );

    final public int seconds;
    final public int nanoseconds;


    /**
     * Creates a new instance of this class with the given time since midnight January 1, 1990 GMT.  Note that if the given nanoseconds value is not
     * between 0 and 999,999,999 then the seconds and nanoseconds are adjusted to make that so.
     *
     * @param _seconds the number of seconds since midnight January 1, 1990 GMT
     * @param _nanoseconds the number of nanoseconds within the second since midnight January 1, 1990 GMT
     */
    public NSec( final int _seconds, final int _nanoseconds ) {

        int ss = _seconds;
        int ns = _nanoseconds;
        while( ns < 0 ) {
            ss--;
            ns += 1_000_000_000;
        }
        while( ns >= 1_000_000_000 ) {
            ss++;
            ns -= 1_000_000_000;
        }
        seconds = ss;
        nanoseconds = ns;
    }


    /**
     * Creates a new instance of this class that is equivalent to the given instant.  Note that if the given instant is out of the range of times
     * that can be represented in an instance of this class, this throws an {@link IllegalArgumentException}.
     *
     * @param _instant the instant to make an equivalent instance from
     */
    public NSec( final Instant _instant ) {

        // sanity check...
        if( _instant == null )
            throw new IllegalArgumentException( "Required instant is missing" );

        // first we get the duration between the Campbell Scientific epoch and the given instant
        Duration delta = Duration.between( EPOCH, _instant );

        // range check...
        long secs = delta.getSeconds();
        if( ((int)secs) != secs )
            throw new IllegalArgumentException( "Instant is out of range for conversion to NSec" );

        seconds = (int) secs;
        nanoseconds = delta.getNano();
    }


    /**
     * Creates a new instance of this class that is equivalent to the given duration.  Note that if the given duration is out of range of times that
     * can be represented as an instance of this class, this throws an {@link IllegalArgumentException}.
     *
     * @param _duration the duration to make an equivalent instance from
     */
    public NSec( final Duration _duration ) {

        // sanity check...
        if( _duration == null )
            throw new IllegalArgumentException( "Required duration is missing" );

        // range check...
        long secs = _duration.getSeconds();
        if( ((int)secs) != secs )
            throw new IllegalArgumentException( "Duration is out of range for conversion to NSec" );

        seconds = (int) secs;
        nanoseconds = _duration.getNano();
    }


    /**
     * Returns a new instance of this class that is the sum of this instance and the given instance.
     *
     * @param _operand the instance to add to this instance
     * @return the new instance with the sum of this instance and the given instance
     */
    public NSec add( final NSec _operand ) {
        return new NSec( seconds + _operand.seconds, nanoseconds + _operand.nanoseconds );
    }


    /**
     * Returns a new instance of this class that is the result of this instance minus the given instance.
     *
     * @param _operand the instance to subtract from this instance
     * @return the new instance with the result of this instance minus the given instance
     */
    public NSec sub( final NSec _operand ) {
        return new NSec( seconds - _operand.seconds, nanoseconds - _operand.nanoseconds );
    }


    /**
     * Returns an {@link Instant} with a value equivalent to that of this instance.
     *
     * @return the equivalent instant
     */
    public Instant asInstant() {
        return EPOCH.plus( Duration.ofSeconds( seconds, nanoseconds ) );
    }


    /**
     * Returns a {@link Duration} instance equivalent to the value of this instance.  This is useful when an NSec instance is being used to represent
     * the difference between two moments, rather than an actual moment.
     *
     * @return a duration equivalent to the value of this instance
     */
    public Duration asDuration() {
        return Duration.ofSeconds( seconds, nanoseconds );
    }


    @Override
    public boolean equals( final Object _o ) {
        if( this == _o ) return true;
        if( _o == null || getClass() != _o.getClass() ) return false;
        NSec nSec = (NSec) _o;
        return seconds == nSec.seconds &&
                nanoseconds == nSec.nanoseconds;
    }


    @Override
    public int hashCode() {
        return Objects.hash( seconds, nanoseconds );
    }


    /**
     * Returns a new instance of this class that represents the current date and time.
     *
     * @return a new instance of this class that represents the current date and time
     */
    static public NSec now() {
        return new NSec( Instant.now( Clock.systemUTC() ) );
    }
}
