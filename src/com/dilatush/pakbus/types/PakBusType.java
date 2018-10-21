package com.dilatush.pakbus.types;

import com.dilatush.pakbus.PakBusBaseDataType;

import java.lang.String;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import static com.dilatush.pakbus.PakBusBaseDataType.*;
import static com.dilatush.pakbus.PakBusBaseDataType.Boolean;
import static com.dilatush.pakbus.PakBusBaseDataType.Float;
import static com.dilatush.pakbus.PakBusBaseDataType.String;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * Represents all possible data types used in PakCtrl or BMP5 messages.  Most of these are documented in Campbell Scientific documentation, but
 * some are inferred from actual usage.  For example, data types less than 8 bits long are not defined by Campbell Scientific, but are commonly
 * used in the messages.  These inferred types are marked as undocumented for clarity.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum PakBusType {

    Byte       (  8, "Byte",           1,  null,          UnsignedInteger ),
    UInt2      ( 16, "UInt2",          2,  BIG_ENDIAN,    UnsignedInteger ),
    UInt2Lsf   ( 16, "UInt2Lsf",      21,  LITTLE_ENDIAN, UnsignedInteger ),
    UShort     ( 16, "UShort",        21,  LITTLE_ENDIAN, UnsignedInteger ),
    UInt4      ( 32, "UInt4",          3,  BIG_ENDIAN,    SignedInteger   ),
    Int1       (  8, "Int1",           4,  null,          SignedInteger   ),
    Int2       ( 16, "Int2",           5,  BIG_ENDIAN,    SignedInteger   ),
    Int2Lsf    ( 16, "Int2Lsf",       19,  LITTLE_ENDIAN, SignedInteger   ),
    Short      ( 16, "Short",         19,  LITTLE_ENDIAN, SignedInteger   ),
    Int4       ( 32, "Int4",           6,  BIG_ENDIAN,    SignedInteger   ),
    Int4Lsf    ( 32, "Int4Lsf",       20,  LITTLE_ENDIAN, SignedInteger   ),
    Long       ( 32, "Long",          20,  LITTLE_ENDIAN, SignedInteger   ),
    FP2        ( 16, "FP2",            7,  BIG_ENDIAN,    Float           ),
    FP3        ( 24, "FP3",           15,  BIG_ENDIAN,    Float           ),
    FP4        ( 32, "FP4",            8,  BIG_ENDIAN,    Float           ),
    IEEE4      ( 32, "IEEE4",          9,  BIG_ENDIAN,    Float           ),
    IEEE4Lsf   ( 32, "IEEE4Lsf",      24,  LITTLE_ENDIAN, Float           ),
    IEEE4L     ( 32, "IEEE4L",        24,  LITTLE_ENDIAN, Float           ),
    IEEE8      ( 64, "IEEE8",         18,  BIG_ENDIAN,    Float           ),
    IEEE8Lsf   ( 64, "IEEE8Lsf",      25,  LITTLE_ENDIAN, Float           ),
    IEEE8L     ( 64, "IEEE8L",        25,  LITTLE_ENDIAN, Float           ),
    Bool8      (  8, "Bool8",         17,  BIG_ENDIAN,    BitArray        ),
    Bool       (  8, "Bool",          10,  BIG_ENDIAN,    Boolean         ),
    Bool2      ( 16, "Bool2",         27,  BIG_ENDIAN,    Boolean         ),
    Bool4      ( 32, "Bool4",         28,  BIG_ENDIAN,    Boolean         ),
    Sec        ( 32, "Sec",           12,  BIG_ENDIAN,    SignedInteger   ),
    USec       ( 48, "USec",          13,  BIG_ENDIAN,    UnsignedInteger ),
    NSec       ( 64, "NSec",          14,  BIG_ENDIAN,    SignedInteger   ),
    NSecLsf    ( 64, "NSecLsf",       26,  LITTLE_ENDIAN, SignedInteger   ),
    ASCII      (  8, "ASCII",         11,  null,          String          ),
    ASCIIZ     (  0, "ASCIIZ",        16,  null,          String          );


    private static Map<Integer,PakBusType> byCode;

    private int                bits;        // length of the data type in bits, or 0 if it is a variable length field...
    private String             name;        // the name of the type...
    private int                code;        // the Campbell Scientific code used to indicate this type...
    private ByteOrder          order;       // Big-Endian or Little-Endian...
    private PakBusBaseDataType base;        // base data type...


    // constructor for a simple data type...
    PakBusType( final int _bits, final String _name, final int _code, final ByteOrder _order, final PakBusBaseDataType _base ) {
        bits = _bits;
        name = _name;
        code = _code;
        order = _order;
        base = _base;
        init( );
    }


    private void init() {
        if( byCode == null )
            byCode = new HashMap<>();
        byCode.put( code, this );
    }


    public static PakBusType decode( final int _code ) {
        return byCode.get( _code );
    }


    public int getBits() {
        return bits;
    }


    public String getName() {
        return name;
    }


    public int getCode() {
        return code;
    }


    public ByteOrder getOrder() {
        return order;
    }


    public PakBusBaseDataType getBase() {
        return base;
    }
}
