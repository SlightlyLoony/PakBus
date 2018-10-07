package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.util.Transformer;

/**
 * Instances of this class represent a simple datum (that is, neither an array nor a composite type).  Instances of this class are mutable and NOT
 * threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class SimpleDatum extends ADatum {


    public SimpleDatum( final DataType _type ) {
        super( _type );
    }


    static public void main( String[] _args ) {

        Datum d1 = new SimpleDatum( DataTypes.fromName( "Bits12" ) );
        Transformer.fromInt( 0xabc, d1 );
        int i1 = Transformer.toInt( d1 );

        d1.hashCode();
    }
}
