package com.dilatush.pakbus.types;

import java.lang.String;
import java.util.*;

import static com.dilatush.pakbus.types.GeneralDataType.*;
import static java.nio.ByteOrder.*;

/**
 * Singleton class that provides access to all basic PakBus data types by name or by Campbell Scientific code.  The singleton instance of this class
 * is immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DataTypes {

    final public static DataType BYTE     = new SimpleDataType( "Byte",     PakBusType.Byte,      8, UnsignedInteger, null          );
    final public static DataType UINT2    = new SimpleDataType( "UInt2",    PakBusType.UInt2,    16, UnsignedInteger, BIG_ENDIAN    );
    final public static DataType UINT2LSF = new SimpleDataType( "UInt2Lsf", PakBusType.UInt2Lsf, 16, UnsignedInteger, LITTLE_ENDIAN );
    final public static DataType USHORT   = new SimpleDataType( "UShort",   PakBusType.UShort,   16, UnsignedInteger, LITTLE_ENDIAN );
    final public static DataType UINT4    = new SimpleDataType( "UInt4",    PakBusType.UInt4,    32, UnsignedInteger, BIG_ENDIAN    );
    final public static DataType INT1     = new SimpleDataType( "Int1",     PakBusType.Int1,      8, SignedInteger,   null          );
    final public static DataType INT2     = new SimpleDataType( "Int2",     PakBusType.Int2,     16, SignedInteger,   BIG_ENDIAN    );
    final public static DataType INT2LSF  = new SimpleDataType( "Int2Lsf",  PakBusType.Int2Lsf,  16, SignedInteger,   LITTLE_ENDIAN );
    final public static DataType SHORT    = new SimpleDataType( "Short",    PakBusType.Short,    16, SignedInteger,   LITTLE_ENDIAN );
    final public static DataType INT4     = new SimpleDataType( "Int4",     PakBusType.Int4,     32, SignedInteger,   BIG_ENDIAN    );
    final public static DataType INT4LSF  = new SimpleDataType( "Int4Lsf",  PakBusType.Int4Lsf,  32, SignedInteger,   LITTLE_ENDIAN );
    final public static DataType LONG     = new SimpleDataType( "Long",     PakBusType.Long,     32, SignedInteger,   LITTLE_ENDIAN );
    final public static DataType FP2      = new SimpleDataType( "FP2",      PakBusType.FP2,      16, Float,           BIG_ENDIAN    );
    final public static DataType FP3      = new SimpleDataType( "FP3",      PakBusType.FP3,      24, Float,           BIG_ENDIAN    );
    final public static DataType FP4      = new SimpleDataType( "FP4",      PakBusType.FP4,      32, Float,           BIG_ENDIAN    );
    final public static DataType IEEE4    = new SimpleDataType( "IEEE4",    PakBusType.IEEE4,    32, Float,           BIG_ENDIAN    );
    final public static DataType IEEE4LSF = new SimpleDataType( "IEEE4Lsf", PakBusType.IEEE4Lsf, 32, Float,           LITTLE_ENDIAN );
    final public static DataType IEEE4L   = new SimpleDataType( "IEEE4L",   PakBusType.IEEE4L,   32, Float,           LITTLE_ENDIAN );
    final public static DataType IEEE8    = new SimpleDataType( "IEEE8",    PakBusType.IEEE8,    64, Float,           BIG_ENDIAN    );
    final public static DataType IEEE8LSF = new SimpleDataType( "IEEE8Lsf", PakBusType.IEEE8Lsf, 64, Float,           LITTLE_ENDIAN );
    final public static DataType IEEE8L   = new SimpleDataType( "IEEE8L",   PakBusType.IEEE8L,   64, Float,           LITTLE_ENDIAN );
    final public static DataType BOOL     = new SimpleDataType( "Bool",     PakBusType.Bool,      8, Boolean,         BIG_ENDIAN    );
    final public static DataType BOOL2    = new SimpleDataType( "Bool2",    PakBusType.Bool2,    16, Boolean,         BIG_ENDIAN    );
    final public static DataType BOOL4    = new SimpleDataType( "Bool4",    PakBusType.Bool4,    32, Boolean,         BIG_ENDIAN    );
    final public static DataType SEC      = new SimpleDataType( "Sec",      PakBusType.Sec,      32, SignedInteger,   BIG_ENDIAN    );
    final public static DataType USEC     = new SimpleDataType( "USec",     PakBusType.USec,     48, UnsignedInteger, BIG_ENDIAN    );
    final public static DataType BIT      = new SimpleDataType( "Bit",      null,                 1, UnsignedInteger, null          );
    final public static DataType ASCII    = new SimpleDataType( "ASCII",    PakBusType.ASCII,     8, UnsignedInteger, null          );

    final public static DataType BITS2    = new ArrayDataType(  "Bits2",    null,              BIT,   2     );
    final public static DataType BITS4    = new ArrayDataType(  "Bits4",    null,              BIT,   4     );
    final public static DataType BITS12   = new ArrayDataType(  "Bits12",   null,              BIT,   12    );
    final public static DataType BOOL8    = new ArrayDataType(  "Bool8",    PakBusType.Bool8,  BIT,   8     );
    final public static DataType ASCIIZ   = new ArrayDataType(  "ASCIIZ",   PakBusType.ASCIIZ, ASCII, ASCII );
    final public static DataType ASCIIS   = new ArrayDataType(  "ASCIIs",   null,              ASCII        );
    final public static DataType BYTES    = new ArrayDataType(  "Bytes",    null,              BYTE         );
    final public static DataType BYTESZ   = new ArrayDataType(  "BytesZ",   null,              BYTE,  BYTE  );

    final public static DataType NSEC     = new CompositeDataType( "NSec", PakBusType.NSec,
                new CP( "Seconds",     INT4    ),
                new CP( "Nanoseconds", INT4    ) );
    final public static DataType NSECLSF = new CompositeDataType( "NSecLsf", PakBusType.NSecLsf,
                new CP( "Seconds",     INT4LSF ),
                new CP( "Nanoseconds", INT4LSF ) );

    // this must come after the DataType initializations above...
    final private static DataTypes ourInstance = new DataTypes();

    final private Map<String,DataType> byName;
    final private Map<PakBusType,DataType> byPakBusType;


    public static DataTypes getInstance() {
        return ourInstance;
    }


    private DataTypes() {

        // some setup...
        byName = new HashMap<>( 250 );
        byPakBusType = new HashMap<>( 100 );

        // first we add our basic simple types...
        add( BYTE     );
        add( UINT2    );
        add( UINT2LSF );
        add( USHORT   );
        add( UINT4    );
        add( INT1     );
        add( INT2     );
        add( INT2LSF  );
        add( SHORT    );
        add( INT4     );
        add( INT4LSF  );
        add( LONG     );
        add( FP2      );
        add( FP3      );
        add( FP4      );
        add( IEEE4    );
        add( IEEE4LSF );
        add( IEEE4L   );
        add( IEEE8    );
        add( IEEE8LSF );
        add( IEEE8L   );
        add( BOOL     );
        add( BOOL2    );
        add( BOOL4    );
        add( SEC      );
        add( USEC     );
        add( BIT      );
        add( ASCII    );

        // then our basic array types...
        add( BITS2    );
        add( BITS4    );
        add( BITS12   );
        add( BOOL8    );
        add( ASCIIZ   );
        add( ASCIIS   );
        add( BYTES    );
        add( BYTESZ   );

        // then our basic composite types...
        add( NSEC     );
        add( NSECLSF  );
    }


    private void add( final DataType _type ) {
        if( byName.containsKey( _type.name() ) )
            throw new IllegalArgumentException( "Duplicate DataType name: " + _type.name() );
        byName.put( _type.name(), _type );
        if( _type.pakBusType() != null )
            byPakBusType.put( _type.pakBusType(), _type );
    }


    static public DataType fromName( final String _name ) {
        return ourInstance.byName.get( _name );
    }


    static public DataType fromPakBusType( final PakBusType _pakBusType ) {
        return ourInstance.byPakBusType.get( _pakBusType );
    }
}
