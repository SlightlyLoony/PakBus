package com.dilatush.pakbus.types;

import java.nio.ByteOrder;

/**
 * Instances of this class represent simple PakBus data types (i.e., those that are neither composite nor array).  Instances of this class are
 * immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class SimpleDataType extends ADataType {


    public SimpleDataType( final String _name, final int _code, final int _bits, final GeneralDataType _generalType, final ByteOrder _byteOrder ) {
        super( _name, _code, _bits, _generalType, _byteOrder );
    }
}
