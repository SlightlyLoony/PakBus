package com.dilatush.pakbus.objects;

import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.ArrayDatum;
import com.dilatush.pakbus.values.CompositeDatum;

import static com.dilatush.pakbus.objects.TableDefinitions.*;

/**
 * Instances of this class represent one of the field definitions in a table definition from a datalogger.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class FieldDefinition {

    final public String  name;
    final public int     index;
    final public boolean readOnly;
    final public int     fieldType;
    final public String  processing;
    final public String  units;
    final public String  description;
    final public int     beginIndex;
    final public int     pieceSize;
    final public int[]   dimensions;


    public FieldDefinition( final CompositeDatum _datum, final int _index ) {

        // sanity checks...
        Checks.required( _datum );

        // populate the non-field stuff...
        index = _index;
        name        = _datum.at( FIELD_FIELD_NAME  ).getAsString();
        readOnly    = _datum.at( FIELD_READ_ONLY   ).getAsBoolean();
        fieldType   = _datum.at( FIELD_FIELD_TYPE  ).getAsInt();
        processing  = _datum.at( FIELD_PROCESSING  ).getAsString();
        units       = _datum.at( FIELD_UNITS       ).getAsString();
        description = _datum.at( FIELD_DESCRIPTION ).getAsString();
        beginIndex  = _datum.at( FIELD_BEGIN_INDEX ).getAsInt();
        pieceSize   = _datum.at( FIELD_PIECE_SIZE  ).getAsInt();
        ArrayDatum dimsArray = (ArrayDatum) _datum.at( FIELD_DIMENSIONS );
        dimensions = new int[dimsArray.elements()];
        for( int i = 0; i < dimsArray.elements(); i++ ) {
            dimensions[i] = dimsArray.get( i ).getAsInt();
        }
    }
}
