package com.dilatush.pakbus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class contains static utility methods that convert the several PakBus floating numeric types to or from Java doubles.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class PakBusFloat {


    /**
     * Converts the given double value to the given PakBus floating type, returning the result in a buffer of the correct length.  If the given type
     * is not one of the PakBus floating types, throws an IllegalArgumentException.
     *
     * @param _n the double value to convert
     * @param _type the PakBus floating data type to convert to
     * @return the buffer with the converted result
     */
    public static ByteBuffer toPakBusFloat( final double _n, final PakBusDataType _type ) {

        // sanity check...
        if( _type == null )
            throw new IllegalArgumentException( "Required argument missing" );

        // different strokes for different folks (or floating types)...
        switch( _type ) {

            case FP2: return toFP2( _n );

            case FP3: return toFP3( _n );

            case FP4: return toFP4( _n );

            case IEEE4:
            case IEEE4Lsf:
            case IEEE4L:
                ByteBuffer result = ByteBuffer.allocate( 4 );
                result.order( _type.getOrder() );
                result.putFloat( (float) _n );
                result.flip();
                return result;

            case IEEE8:
            case IEEE8Lsf:
            case IEEE8L:
                result = ByteBuffer.allocate( 8 );
                result.order( _type.getOrder() );
                result.putDouble( _n );
                result.flip();
                return result;

            default: throw new IllegalArgumentException( "The type argument (" + _type + ") is not a floating type" );
        }
    }


    public static double fromPakBusFloat( final ByteBuffer _buffer, final PakBusDataType _type ) {

        // sanity check...
        if( (_buffer == null) || (_type == null) )
            throw new IllegalArgumentException( "Required argument missing" );

        // different strokes for different folks (or floating types)...
        switch( _type ) {

            case FP2: return fromFP2( _buffer );

            case FP3: return fromFP3( _buffer );

            case FP4: return fromFP4( _buffer );

            case IEEE4:
            case IEEE4Lsf:
            case IEEE4L:
                if( _buffer.limit() != 4 )
                    throw new IllegalArgumentException( "Buffer is not the correct length: " + _buffer.limit() + " instead of 4" );
                _buffer.order( _type.getOrder() );
                return (double)(_buffer.getFloat( 0 ));

            case IEEE8:
            case IEEE8Lsf:
            case IEEE8L:
                if( _buffer.limit() != 8 )
                    throw new IllegalArgumentException( "Buffer is not the correct length: " + _buffer.limit() + " instead of 8" );
                _buffer.order( _type.getOrder() );
                return _buffer.getDouble( 0 );

            default: throw new IllegalArgumentException( "The type argument (" + _type + ") is not a floating type" );
        }
    }


    /*
     * FP2 can represent magnitudes between 0 and 8,189, though some documentation I found says that Campbell Scientific defines the largest magnitude
     * as 7,999.  The smallest non-zero magnitude is 0.001.  Special values represent NaN and +/- infinity.  This method converts doubles with NaN or
     * infinity directly to their FP2 counterparts.  In addition, it converts magnitudes over 8,189.5 to +/- infinity as well.  All other double
     * values are converted to the nearest value that FP2 can represent.
     *
     */
    private static final double MAX_FP2_MAGNITUDE = 8189;
    private static final int FP2_NAN = 0b0_1001_1111_1111_1110;
    private static final int FP2_INF = 0b0_0001_1111_1111_1111;
    private static final int FP2_MAN = 0b0_0001_1111_1111_1111;
    private static final int FP2_SGN = 0b0_1000_0000_0000_0000;
    private static final int FP2_EXP = 0b0_0110_0000_0000_0000;


    private static double fromFP2( final ByteBuffer _buffer ) {

        // sanity check...
        if( _buffer.limit() != 2 )
            throw new IllegalArgumentException( "Buffer length is invalid: " + _buffer.limit() + " instead of 2" );

        // first we get the bits...
        _buffer.order( ByteOrder.BIG_ENDIAN );
        int bits = _buffer.getShort( 0 ) & 0xFFFF;

        // handle our special cases...
        switch( bits ) {
            case FP2_NAN:           return Double.NaN;
            case FP2_INF:           return Double.POSITIVE_INFINITY;
            case FP2_INF | FP2_SGN: return Double.NEGATIVE_INFINITY;
        }

        // chop it up...
        boolean neg = (bits & FP2_SGN) != 0;
        int exp = (bits & FP2_EXP) >>> 13;
        int mantissa = (bits & FP2_MAN);

        // now make a double out of it...
        return (neg ? -1.0d : 1.0d ) * mantissa * Math.pow( 10.0d, -exp );
    }


    private static ByteBuffer toFP2( final double _n ) {

        // we're going to deal with the magnitude independently of the sign...
        double an = Math.abs( _n );

        // get the bits for our FP2 value...
        int bits;

        // handle the special cases...
        if( Double.isNaN( _n ))
            bits = FP2_NAN;
        else if( Double.isInfinite( _n ) )
            bits = FP2_INF;
        else if( an >= (MAX_FP2_MAGNITUDE) )
            bits = FP2_INF;

        // or handle the normal cases...
        else if( an >= 818.9 )
            bits = fpOp( an, 0, 13, 1, 0x1FFF );
        else if( an >= 81.89 )
            bits = fpOp( an, 1, 13, 10, 0x1FFF );
        else if( an >= 8.189 )
            bits = fpOp( an, 2, 13, 100, 0x1FFF );
        else
            bits = fpOp( an, 3, 13, 1000, 0x1FFF );

        // set the sign bit if we're negative...
        if( _n < 0 ) bits |= FP2_SGN;

        // leave with our two bytes...
        ByteBuffer result = ByteBuffer.allocate( 2 );
        result.order( ByteOrder.BIG_ENDIAN );
        result.putShort( (short) bits );
        result.flip();
        return result;
    }


    /*
     * FP2 can represent magnitudes between 0 and 1,048,573.  The smallest non-zero magnitude is 0.0000001.  Special values represent NaN and +/-
     * infinity.  This method converts doubles with NaN or infinity directly to their FP3 counterparts.  In addition, it converts magnitudes over
     * 1,048,573.5 to +/- infinity as well.  All other double values are converted to the nearest value that FP3 can represent.
     *
     */
    private static final double MAX_FP3_MAGNITUDE = 1_048_573;
    private static final int FP3_NAN = 0b0_1000_1111_1111_1111_1111_1110;
    private static final int FP3_INF = 0b0_0000_1111_1111_1111_1111_1111;
    private static final int FP3_MAN = 0b0_0000_1111_1111_1111_1111_1111;
    private static final int FP3_SGN = 0b0_1000_0000_0000_0000_0000_0000;
    private static final int FP3_EXP = 0b0_0111_0000_0000_0000_0000_0000;


    private static double fromFP3( final ByteBuffer _buffer ) {

        // sanity check...
        if( _buffer.limit() != 3 )
            throw new IllegalArgumentException( "Buffer length is invalid: " + _buffer.limit() + " instead of 3" );

        // first we get the bits...
        _buffer.order( ByteOrder.BIG_ENDIAN );
        int bits = ((_buffer.get( 0 ) & 0xFF) << 16) | (_buffer.getShort(1 ) & 0xFFFF);

        // handle our special cases...
        switch( bits ) {
            case FP3_NAN:           return Double.NaN;
            case FP3_INF:           return Double.POSITIVE_INFINITY;
            case FP3_INF | FP3_SGN: return Double.NEGATIVE_INFINITY;
        }

        // chop it up...
        boolean neg = (bits & FP3_SGN) != 0;
        int exp = (bits & FP3_EXP) >>> 20;
        int mantissa = (bits & FP3_MAN);

        // now make a double out of it...
        return (neg ? -1.0d : 1.0d ) * mantissa * Math.pow( 10.0d, -exp );
    }


    private static ByteBuffer toFP3( final double _n ) {

        // we're going to deal with the magnitude independently of the sign...
        double an = Math.abs( _n );

        // get the bits for our FP3 value...
        int bits;

        // handle the special cases...
        if( Double.isNaN( _n ))
            bits = FP3_NAN;
        else if( Double.isInfinite( _n ) )
            bits = FP3_INF;
        else if( an >= MAX_FP3_MAGNITUDE )
            bits = FP3_INF;

            // or handle the normal cases...
        else if( an >= 104857.3 )
            bits = fpOp( an, 0, 20, 1, 0xFFFFF );
        else if( an >= 10485.73 )
            bits = fpOp( an, 1, 20, 10, 0xFFFFF );
        else if( an >= 1048.573 )
            bits = fpOp( an, 2, 20, 100, 0xFFFFF );
        else if( an >= 104.8573)
            bits = fpOp( an, 3, 20, 1000, 0xFFFFF );
        else if( an >= 10.48573)
            bits = fpOp( an, 4, 20, 10000, 0xFFFFF );
        else if( an >= 1.048573)
            bits = fpOp( an, 5, 20, 100000, 0xFFFFF );
        else if( an >= .1048573)
            bits = fpOp( an, 6, 20, 1000000, 0xFFFFF );
        else
            bits = fpOp( an, 7, 20, 10000000, 0xFFFFF );

        // set the sign bit if we're negative...
        if( _n < 0 ) bits |= FP3_SGN;

        // leave with our three bytes...
        ByteBuffer result = ByteBuffer.allocate( 3 );
        result.order( ByteOrder.BIG_ENDIAN );
        result.put( (byte)(bits >>> 16) );
        result.put( (byte)(bits >>> 8) );
        result.put( (byte)(bits) );
        result.flip();
        return result;
    }


    private static int fpOp( final double _an, final int _exp, final int _shift, final double _mult, final int _max ) {
        int m = Math.min( _max, (int)(0.5 + _an * _mult));
        return (_exp << _shift) | m;
    }


    /*
     * FP4 is an "almost IEEE" binary floating point representation.  FP4 has one more bit of mantissa than IEEE, and one less of exponent.  I was
     * unable to find any documentation for a NaN representation in FP4, so we throw an error in that case.  FP4's exponent has a range of [-63..63];
     * doubles with exponents > 63 are reported as infinity, and with exponents < -63 as zero.
     */
    private static final int FP4_INF = 0b0_0111_1111_1111_1111_1111_1111_1111_1111;
    private static final int FP4_SGN = 0b0_1000_0000_0000_0000_0000_0000_0000_0000;
    private static final int FP4_ESG = 0b0_0100_0000_0000_0000_0000_0000_0000_0000;
    private static final int FP4_EXP = 0b0_0011_1111_0000_0000_0000_0000_0000_0000;
    private static final int FP4_MAN = 0b0_0000_0000_1111_1111_1111_1111_1111_1111;


    private static double fromFP4( final ByteBuffer _buffer ) {

        // sanity check...
        if( _buffer.limit() != 4 )
            throw new IllegalArgumentException( "Buffer length is invalid: " + _buffer.limit() + " instead of 4" );

        // first we get the bits...
        _buffer.order( ByteOrder.BIG_ENDIAN );
        int bits = _buffer.getInt( 0 );

        // handle our special cases (note that there is no NaN for FP4)...
        switch( bits ) {
            case 0:                 return 0d;
            case FP4_SGN:           return -0d;
            case FP4_INF:           return Double.POSITIVE_INFINITY;
            case FP4_INF | FP4_SGN: return Double.NEGATIVE_INFINITY;
        }

        // chop it up...
        boolean neg = (bits & FP4_SGN) != 0;
        boolean expNeg = (bits & FP4_ESG) == 0;  // exponent's sign is 1 for POSITIVE...
        int exp = (bits & FP4_EXP) >>> 24;
        int mantissa = (bits & FP4_MAN);

        // now make a double out of it...
        long dExp = (long)((expNeg ? -exp : exp) + 1023) << 52;
        long dMan = (long)mantissa << 28;
        long dBits = dExp | dMan | (neg ? 0x8000_0000_0000_0000L : 0);
        return Double.longBitsToDouble( dBits );
    }


    private static ByteBuffer toFP4( final double _n ) {

        // sanity checks and setup...
        if( Double.isNaN( _n ) )
            throw new IllegalArgumentException( "Cannot convert a NaN to FP4" );
        int bits;

        // a bit of analysis...
        long rawBits = Double.doubleToRawLongBits( _n );
        int mantissa = (int)((rawBits & 0x0F_FFFF_FFFF_FFFFL) >>> 28);  // our double's 52 bit mantissa, shifted to make a 24 bit FP4 mantissa...
        int exp = (int)((rawBits & 0x7FF0_0000_0000_0000L) >>> 52) - 1023;  // our de-offsetted IEEE exponent...


        // simple cases...
        // double is infinite...
        if( Double.isInfinite( _n ) )
            bits = FP4_INF;

        // double is zero or denormal...
        else if( (_n == 0) || ((rawBits & 0x7FF0_0000_0000_0000L) == 0) )
            bits = 0;

        // double is too large to represent as an FP4 number...
        else if( exp > 63 )
            bits = FP4_INF;

        // double is too small to represent as an FP4 number...
        else if( exp < -63 )
            bits = 0;

        // now the slightly more difficult case of the normal numbers...
        // note the exponent's sign bit is set if it's POSITIVE (weird!)...
        else
            bits = ((exp >= 0) ? (1 << 30) : 0) | (Math.abs( exp ) << 24 ) | mantissa;

        // set the sign bit if we're negative...
        if( _n < 0 ) bits |= FP4_SGN;

        // leave with our four bytes...
        ByteBuffer result = ByteBuffer.allocate( 4 );
        result.order( ByteOrder.BIG_ENDIAN );
        result.putInt( bits );
        result.flip();
        return result;
    }
}
