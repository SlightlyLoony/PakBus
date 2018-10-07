package com.dilatush.pakbus.types;

import java.nio.ByteOrder;

/**
 * Implemented by all classes that implement PakBus data types, including composite and array types.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface DataType {


    /**
     * Returns the bit length of this data type, or 0 if its length is variable.
     *
     * @return the bit length of this data type, or 0 if its length is variable.
     */
    int bits();


    /**
     * Returns the unique name of this data type.
     *
     * @return the unique name of this data type
     */
    String name();


    /**
     * Returns the PakBus data type code for this data type, or 0 if there is no PakBus-defined code.
     *
     * @return the PakBus data type code for this data type
     */
    int code();


    /**
     * Returns the byte order (big-endian or little-endian) for this data type, or null if the byte order is irrelevant.
     *
     * @return the byte order (big-endian or little-endian) for this data type
     */
    ByteOrder byteOrder();


    /**
     * Returns the general type of this data type.
     *
     * @return the general type of this data type
     */
    GeneralDataType generalType();
}
