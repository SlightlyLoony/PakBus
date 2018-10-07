package com.dilatush.pakbus;

import java.nio.ByteOrder;

import static com.dilatush.pakbus.PakBusBaseDataType.*;
import static java.nio.ByteOrder.*;

/**
 * Represents all possible data types used in PakCtrl or BMP5 messages.  Most of these are documented in Campbell Scientific documentation, but
 * some are inferred from actual usage.  For example, data types less than 8 bits long are not defined by Campbell Scientific, but are commonly
 * used in the messages.  These inferred types are marked as undocumented for clarity.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum PakBusDataType {

    Byte       (  8, "Byte",           1,  true, null,          UnsignedInteger ),
    UInt2      ( 16, "UInt2",          2,  true, BIG_ENDIAN,    UnsignedInteger ),
    UInt2Lsf   ( 16, "UInt2Lsf",      21,  true, LITTLE_ENDIAN, UnsignedInteger ),
    UShort     ( 16, "UShort",        21,  true, LITTLE_ENDIAN, UnsignedInteger ),
    UInt4      ( 32, "UInt4",          3,  true, BIG_ENDIAN,    SignedInteger   ),
    Int1       (  8, "Int1",           4,  true, null,          SignedInteger   ),
    Int2       ( 16, "Int2",           5,  true, BIG_ENDIAN,    SignedInteger   ),
    Int2Lsf    ( 16, "Int2Lsf",       19,  true, LITTLE_ENDIAN, SignedInteger   ),
    Short      ( 16, "Short",         19,  true, LITTLE_ENDIAN, SignedInteger   ),
    Int4       ( 32, "Int4",           6,  true, BIG_ENDIAN,    SignedInteger   ),
    Int4Lsf    ( 32, "Int4Lsf",       20,  true, LITTLE_ENDIAN, SignedInteger   ),
    Long       ( 32, "Long",          20,  true, LITTLE_ENDIAN, SignedInteger   ),
    FP2        ( 16, "FP2",            7,  true, BIG_ENDIAN,    Float           ),
    FP3        ( 24, "FP3",           15,  true, BIG_ENDIAN,    Float           ),
    FP4        ( 32, "FP4",            8,  true, BIG_ENDIAN,    Float           ),
    IEEE4      ( 32, "IEEE4",          9,  true, BIG_ENDIAN,    Float           ),
    IEEE4Lsf   ( 32, "IEEE4Lsf",      24,  true, LITTLE_ENDIAN, Float           ),
    IEEE4L     ( 32, "IEEE4L",        24,  true, LITTLE_ENDIAN, Float           ),
    IEEE8      ( 64, "IEEE8",         18,  true, BIG_ENDIAN,    Float           ),
    IEEE8Lsf   ( 64, "IEEE8Lsf",      25,  true, LITTLE_ENDIAN, Float           ),
    IEEE8L     ( 64, "IEEE8L",        25,  true, LITTLE_ENDIAN, Float           ),
    Bool8      (  8, "Bool8",         17,  true, BIG_ENDIAN,    BitArray        ),
    Bool       (  8, "Bool",          10,  true, BIG_ENDIAN,    Boolean         ),
    Bool2      ( 16, "Bool2",         27,  true, BIG_ENDIAN,    Boolean         ),
    Bool4      ( 32, "Bool4",         28,  true, BIG_ENDIAN,    Boolean         ),
    Sec        ( 32, "Sec",           12,  true, BIG_ENDIAN,    SignedInteger   ),
    USec       ( 48, "USec",          13,  true, BIG_ENDIAN,    UnsignedInteger ),
    NSec       ( 64, "NSec",          14,  true, BIG_ENDIAN,    SignedInteger   ),
    NSecSec    ( 32, "NSecSec",     1013, false, BIG_ENDIAN,    SignedInteger   ),
    NSecNs     ( 32, "NSecNS",      1014, false, BIG_ENDIAN,    SignedInteger   ),
    NSecLsf    ( 64, "NSecLsf",       26,  true, LITTLE_ENDIAN, SignedInteger   ),
    NSecSecLsf ( 32, "NSecSecLsf",  1015, false, LITTLE_ENDIAN, SignedInteger   ),
    NSecNsLsf  ( 32, "NSecNSLsf",   1016, false, LITTLE_ENDIAN, SignedInteger   ),
    ASCII      (  0, "ASCII",         11,  true, null,          String          ),
    ASCIIZ     (  0, "ASCIIZ",        16,  true, null,          String          ),
    Bytes      (  0, "Bytes",       1000, false, null,          ByteArray       ),
    BytesZ     (  0, "BytesZ",      1001, false, null,          ByteArray       ),
    Bits1      (  1, "Bits1",       1002, false, null,          UnsignedInteger ),
    Bits2      (  2, "Bits2",       1003, false, null,          UnsignedInteger ),
    Bits3      (  3, "Bits3",       1004, false, null,          UnsignedInteger ),
    Bits4      (  4, "Bits4",       1005, false, null,          UnsignedInteger ),
    Bits5      (  5, "Bits5",       1006, false, null,          UnsignedInteger ),
    Bits6      (  6, "Bits6",       1007, false, null,          UnsignedInteger ),
    Bits7      (  7, "Bits7",       1008, false, null,          UnsignedInteger ),
    Bits9      (  9, "Bits9",       1009, false, null,          UnsignedInteger ),
    Bits10     ( 10, "Bits10",      1010, false, null,          UnsignedInteger ),
    Bits11     ( 11, "Bits11",      1011, false, null,          UnsignedInteger ),
    Bits12     ( 12, "Bits12",      1012, false, null,          UnsignedInteger ),
    ;

    private int                bits;        // length of the data type in bits, or 0 if it is a variable length field...
    private String             name;        // the name of the type...
    private int                code;        // the Campbell Scientific code used to indicate this type (-1 if unknown)...
    private boolean            documented;  // true if documented in Campbell Scientific documentation...
    private ByteOrder          order;       // Big-Endian or Little-Endian...
    private PakBusBaseDataType base;        // base data type...


    // constructor for a simple data type...
    PakBusDataType( final int _bits, final String _name, final int _code, final boolean _documented, final ByteOrder _order, final PakBusBaseDataType _base ) {
        bits = _bits;
        name = _name;
        code = _code;
        documented = _documented;
        order = _order;
        base = _base;
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


    public boolean isDocumented() {
        return documented;
    }


    public ByteOrder getOrder() {
        return order;
    }


    public PakBusBaseDataType getBase() {
        return base;
    }
}
