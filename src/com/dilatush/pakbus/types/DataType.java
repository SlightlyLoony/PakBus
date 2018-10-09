package com.dilatush.pakbus.types;

import java.nio.ByteOrder;

/**
 * Implemented by all classes that implement data types, including composite and array types.
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
     * Returns the PakBus data type for this data type, or null if there is no PakBus-defined type for this data type.
     *
     * @return the PakBus data type for this data type
     */
    PakBusType pakBusType();


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
