package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.util.BitBuffer;

/**
 * Implemented by all classes that represent a PakBus datum.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Datum {

    boolean isSet();

    BitBuffer get();

    void set( final BitBuffer _bits );

    DataType type();

    int size();
}
