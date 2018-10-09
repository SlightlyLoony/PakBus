package com.dilatush.pakbus.types;

import java.lang.String;
import java.util.*;

import static com.dilatush.pakbus.types.GeneralDataType.*;
import static com.dilatush.pakbus.types.PakBusType.*;
import static java.nio.ByteOrder.*;

/**
 * Singleton class that provides access to all PakBus data types by name or by Campbell Scientific code.  The singleton instance of this class is
 * mutable, but is threadsafe through synchronization.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DataTypes {

    final private static DataTypes ourInstance = new DataTypes();

    final private Map<String,DataType> byName;
    final private Map<PakBusType,DataType> byPakBusType;


    public static DataTypes getInstance() {
        return ourInstance;
    }


    private DataTypes() {
        byName = Collections.synchronizedMap( new HashMap<>( 250 ) );
        byPakBusType = Collections.synchronizedMap( new HashMap<>( 100 ) );
        init();
    }


    private void init() {

        // first we add our basic simple types...
        add(new SimpleDataType( "Byte",     Byte,      8, UnsignedInteger, null          ));
        add(new SimpleDataType( "UInt2",    UInt2,    16, UnsignedInteger, BIG_ENDIAN    ));
        add(new SimpleDataType( "UInt2Lsf", UInt2Lsf, 16, UnsignedInteger, LITTLE_ENDIAN ));
        add(new SimpleDataType( "UShort",   UShort,   16, UnsignedInteger, LITTLE_ENDIAN ));
        add(new SimpleDataType( "UInt4",    UInt4,    32, UnsignedInteger, BIG_ENDIAN    ));
        add(new SimpleDataType( "Int1",     Int1,      8, SignedInteger,   null          ));
        add(new SimpleDataType( "Int2",     Int2,     16, SignedInteger,   BIG_ENDIAN    ));
        add(new SimpleDataType( "Int2Lsf",  Int2Lsf,  16, SignedInteger,   LITTLE_ENDIAN ));
        add(new SimpleDataType( "Short",    Short,    16, SignedInteger,   LITTLE_ENDIAN ));
        add(new SimpleDataType( "Int4",     Int4,     32, SignedInteger,   BIG_ENDIAN    ));
        add(new SimpleDataType( "Int4Lsf",  Int4Lsf,  32, SignedInteger,   LITTLE_ENDIAN ));
        add(new SimpleDataType( "Long",     Long,     32, SignedInteger,   LITTLE_ENDIAN ));
        add(new SimpleDataType( "FP2",      FP2,      16, Float,           BIG_ENDIAN    ));
        add(new SimpleDataType( "FP3",      FP3,      24, Float,           BIG_ENDIAN    ));
        add(new SimpleDataType( "FP4",      FP4,      32, Float,           BIG_ENDIAN    ));
        add(new SimpleDataType( "IEEE4",    IEEE4,    32, Float,           BIG_ENDIAN    ));
        add(new SimpleDataType( "IEEE4Lsf", IEEE4Lsf, 32, Float,           LITTLE_ENDIAN ));
        add(new SimpleDataType( "IEEE4L",   IEEE4L,   32, Float,           LITTLE_ENDIAN ));
        add(new SimpleDataType( "IEEE8",    IEEE8,    64, Float,           BIG_ENDIAN    ));
        add(new SimpleDataType( "IEEE8Lsf", IEEE8Lsf, 64, Float,           LITTLE_ENDIAN ));
        add(new SimpleDataType( "IEEE8L",   IEEE8L,   64, Float,           LITTLE_ENDIAN ));
        add(new SimpleDataType( "Bool",     Bool,      8, Boolean,         BIG_ENDIAN    ));
        add(new SimpleDataType( "Bool2",    Bool2,    16, Boolean,         BIG_ENDIAN    ));
        add(new SimpleDataType( "Bool4",    Bool4,    32, Boolean,         BIG_ENDIAN    ));
        add(new SimpleDataType( "Sec",      Sec,      32, SignedInteger,   BIG_ENDIAN    ));
        add(new SimpleDataType( "USec",     USec,     48, UnsignedInteger, BIG_ENDIAN    ));
        add(new SimpleDataType( "Bit",      null,      1, UnsignedInteger, null          ));
        add(new SimpleDataType( "ASCII",    ASCII,     8, UnsignedInteger, null          ));

        // now remember some that we're gonna use...
        DataType BIT     = byName.get( "Bit"     );
        DataType ASCII   = byName.get( "ASCII"   );
        DataType BYTE    = byName.get( "Byte"    );
        DataType INT4    = byName.get( "Int4"    );
        DataType INT4LSF = byName.get( "Int4Lsf" );

        // then our basic array types...
        add( new ArrayDataType( "Bits2",  null,   BIT,   2     ) );
        add( new ArrayDataType( "Bits4",  null,   BIT,   4     ) );
        add( new ArrayDataType( "Bits12", null,   BIT,   12    ) );
        add( new ArrayDataType( "Bool8",  Bool8,  BIT,   8     ) );
        add( new ArrayDataType( "ASCIIZ", ASCIIZ, ASCII, ASCII ) );
        add( new ArrayDataType( "ASCIIs", null,   ASCII        ) );
        add( new ArrayDataType( "Bytes",  null,   BYTE         ) );
        add( new ArrayDataType( "BytesZ", null,   BYTE,  BYTE  ) );

        // now remember some that we're gonna use...
        DataType BITS2   = byName.get( "Bits2"   );
        DataType BITS4   = byName.get( "Bits4"   );
        DataType BITS12  = byName.get( "Bits12"  );
        DataType BYTES   = byName.get( "Bytes"   );

        // then our basic composite types...
        add( new CompositeDataType( "NSec", NSec,
                new CP( "Seconds",     INT4    ),
                new CP( "Nanoseconds", INT4    ) ) );
        add( new CompositeDataType( "NSecLsf", NSecLsf,
                new CP( "Seconds",     INT4LSF ),
                new CP( "Nanoseconds", INT4LSF ) ) );

        // a fundamental composite type: the PakBus packet...
        add( new CompositeDataType( "Packet", null,
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
