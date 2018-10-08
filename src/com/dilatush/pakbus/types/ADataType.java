package com.dilatush.pakbus.types;

import java.nio.ByteOrder;

/**
 * Abstract base class for all data type implementations.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class ADataType implements DataType {

          protected int             bits;         // length of the data type in bits, or 0 if it is a variable length field...
    final protected String          name;         // the name of the type...
    final protected int             code;         // the Campbell Scientific code used to indicate this type (0 if none)...
    final protected ByteOrder       byteOrder;    // Big-Endian or Little-Endian, or null if irrelevant...
    final protected GeneralDataType generalType;  // Signed integer, float, string, etc.; null if not a simple types...


    /*package*/ ADataType( final String _name, final int _code, final int _bits, final GeneralDataType _generalType, final ByteOrder _byteOrder ) {
        bits        = _bits;
        name        = _name;
        code        = _code;
        byteOrder   = _byteOrder;
        generalType = _generalType;
    }


    /**
     * Returns the bit length of this data type, or 0 if its length is variable.
     *
     * @return the bit length of this data type, or 0 if its length is variable.
     */
    @Override
    public int bits() {
        return bits;
    }


    /**
     * Returns the unique name of this data type.
     *
     * @return the unique name of this data type
     */
    @Override
    public String name() {
        return name;
    }


    /**
     * Returns the PakBus data type code for this data type, or 0 if there is no PakBus-defined code.
     *
     * @return the PakBus data type code for this data type
     */
    @Override
    public int code() {
        return code;
    }


    /**
     * Returns the byte byteOrder (big-endian or little-endian) for this data type, or null if the byte byteOrder is irrelevant.
     *
     * @return the byte byteOrder (big-endian or little-endian) for this data type
     */
    @Override
    public ByteOrder byteOrder() {
        return byteOrder;
    }


    /**
     * Returns the general type of this data type.
     *
     * @return the general type of this data type
     */
    @Override
    public GeneralDataType generalType() {
        return generalType;
    }


    public String toString() {
        return name;
    }
}
