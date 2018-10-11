package com.dilatush.pakbus.logger;

import com.dilatush.pakbus.types.PakBusType;

/**
 * Instances of this class describe a particular field within a particular table in a Campbell Scientific datalogger.  Instances of this class are
 * immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class FieldDef {

    final public String     name;            // name of the field in the datalogger
    final public PakBusType type;            // the type of data stored in the field
    final public boolean    readOnly;        // true if the field is read-only
    final public String     processing;      // type of processing used to generate this field
    final public String     units;           // the units for this field
    final public String     description;     // a description of this field
    final public int        beginIndex;      // linear address of the first array element for this field (1-based; is 1 for scalars)
    final public int        elements;        // the number of array elements for this field (is 1 for scalars; product of dimension sizes for arrays)
    final public int[]      dimensionSizes;  // the size for each of the array dimensions; empty for scalars


    public FieldDef( final String _name, final PakBusType _type, final boolean _readOnly, final String _processing,
                     final String _units, final String _description, final int _beginIndex, final int _elements, final int[] _dimensionSizes ) {

        name            = _name;
        type            = _type;
        readOnly        = _readOnly;
        processing      = _processing;
        units           = _units;
        description     = _description;
        beginIndex      = _beginIndex;
        elements        = _elements;
        dimensionSizes  = _dimensionSizes;
    }
}
