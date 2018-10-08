package com.dilatush.pakbus.types;

import java.lang.String;
import java.util.*;

import static com.dilatush.pakbus.types.GeneralDataType.Boolean;
import static com.dilatush.pakbus.types.GeneralDataType.Float;
import static com.dilatush.pakbus.types.GeneralDataType.*;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * Singleton class that provides access to all PakBus data types by name or by Campbell Scientific code.  The singleton instance of this class is
 * mutable, but is threadsafe through synchronization.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DataTypes {

    final private static DataTypes ourInstance = new DataTypes();

    final private Map<String,DataType> byName;
    final private Map<Integer,DataType> byCode;


    public static DataTypes getInstance() {
        return ourInstance;
    }


    private DataTypes() {
        byName = Collections.synchronizedMap( new HashMap<>( 250 ) );
        byCode = Collections.synchronizedMap( new HashMap<>( 100 ) );
        init();
    }


    private void init() {

        // first we add our basic simple types...
        add( new SimpleDataType( "Byte",        1,  8, UnsignedInteger, null   ) );
        add( new SimpleDataType( "UInt2",       2, 16, UnsignedInteger,    BIG_ENDIAN    ) );
        add( new SimpleDataType( "UInt2Lsf",   21, 16, UnsignedInteger,    LITTLE_ENDIAN ) );
        add( new SimpleDataType( "UShort",     21, 16, UnsignedInteger,    LITTLE_ENDIAN ) );
        add( new SimpleDataType( "UInt4",       3, 32, UnsignedInteger,    BIG_ENDIAN    ) );
        add( new SimpleDataType( "Int1",        4,  8, SignedInteger, null     ) );
        add( new SimpleDataType( "Int2",        5, 16, SignedInteger,      BIG_ENDIAN    ) );
        add( new SimpleDataType( "Int2Lsf",    19, 16, SignedInteger,      LITTLE_ENDIAN ) );
        add( new SimpleDataType( "Short",      19, 16, SignedInteger,      LITTLE_ENDIAN ) );
        add( new SimpleDataType( "Int4",        6, 32, SignedInteger,      BIG_ENDIAN    ) );
        add( new SimpleDataType( "Int4Lsf",     6, 32, SignedInteger,      LITTLE_ENDIAN ) );
        add( new SimpleDataType( "Long",        6, 32, SignedInteger,      LITTLE_ENDIAN ) );
        add( new SimpleDataType( "FP2",         7, 16, Float,              BIG_ENDIAN    ) );
        add( new SimpleDataType( "FP3",        15, 24, Float,              BIG_ENDIAN    ) );
        add( new SimpleDataType( "FP4",         8, 32, Float,              BIG_ENDIAN    ) );
        add( new SimpleDataType( "IEEE4",       9, 32, Float,              BIG_ENDIAN    ) );
        add( new SimpleDataType( "IEEE4Lsf",   24, 32, Float,              LITTLE_ENDIAN ) );
        add( new SimpleDataType( "IEEE4L",     24, 32, Float,              LITTLE_ENDIAN ) );
        add( new SimpleDataType( "IEEE8",      18, 64, Float,              BIG_ENDIAN    ) );
        add( new SimpleDataType( "IEEE8Lsf",   25, 64, Float,              LITTLE_ENDIAN ) );
        add( new SimpleDataType( "IEEE8L",     25, 64, Float,              LITTLE_ENDIAN ) );
        add( new SimpleDataType( "Bool",       10,  8, Boolean,            BIG_ENDIAN    ) );
        add( new SimpleDataType( "Bool2",      27, 16, Boolean,            BIG_ENDIAN    ) );
        add( new SimpleDataType( "Bool4",      28, 32, Boolean,            BIG_ENDIAN    ) );
        add( new SimpleDataType( "Sec",        12, 32, SignedInteger,      BIG_ENDIAN    ) );
        add( new SimpleDataType( "USec",       13, 48, UnsignedInteger,    BIG_ENDIAN    ) );
        add( new SimpleDataType( "Bit",         0,  1, UnsignedInteger, null   ) );
        add( new SimpleDataType( "ASCII",      11,  8, UnsignedInteger, null   ) );

        // now remember some that we're gonna use...
        DataType BIT     = byName.get( "Bit"     );
        DataType ASCII   = byName.get( "ASCII"   );
        DataType BYTE    = byName.get( "Byte"    );
        DataType INT4    = byName.get( "Int4"    );
        DataType INT4LSF = byName.get( "Int4Lsf" );

        // then our basic array types...
        add( new ArrayDataType( "Bits2",  0, BIT,  2 ) );
        add( new ArrayDataType( "Bits4",  0, BIT,  4 ) );
        add( new ArrayDataType( "Bits12", 0, BIT, 12 ) );
        add( new ArrayDataType( "Bool8", 17, BIT,  8 ) );
        add( new ArrayDataType( "ASCIIZ",16, ASCII,   ASCII ) );
        add( new ArrayDataType( "Bytes",  0, BYTE           ) );
        add( new ArrayDataType( "BytesZ", 0, BYTE,     BYTE ) );

        // now remember some that we're gonna use...
        DataType BITS2   = byName.get( "Bits2"   );
        DataType BITS4   = byName.get( "Bits4"   );
        DataType BITS12  = byName.get( "Bits12"  );
        DataType BYTES   = byName.get( "Bytes"   );

        // then our basic composite types...
        add( new CompositeDataType( "NSec", 14,
                new CP( "Seconds",     INT4    ),
                new CP( "Nanoseconds", INT4    ) ) );
        add( new CompositeDataType( "NSecLsf", 26,
                new CP( "Seconds",     INT4LSF ),
                new CP( "Nanoseconds", INT4LSF ) ) );

        // a fundamental composite type: the PakBus packet...
        add( new CompositeDataType( "Packet", 0,
                new CP( "LinkState",   BITS4  ),
                new CP( "DstPhyAddr",  BITS12 ),
                new CP( "ExpMoreCode", BITS2  ),
                new CP( "Priority",    BITS2  ),
                new CP( "SrcPhyAddr",  BITS12 ),
                new CP( "HiProtoCode", BITS4  ),
                new CP( "DstNodeId",   BITS12 ),
                new CP( "HopCnt",      BITS4  ),
                new CP( "SrcNodeId",   BITS12 ),
                new CP( "Message",     BYTES  ) ) );
    }


    public void add( final DataType _type ) {
        if( byName.containsKey( _type.name() ) )
            throw new IllegalArgumentException( "Duplicate DataType name: " + _type.name() );
        byName.put( _type.name(), _type );
        if( _type.code() != 0 )
            byCode.put( _type.code(), _type );
    }


    static public DataType fromName( final String _name ) {
        return ourInstance.byName.get( _name );
    }


    static public DataType fromCode( final int _code ) {
        return ourInstance.byCode.get( _code );
    }
}
