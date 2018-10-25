package com.dilatush.pakbus.types;

import com.dilatush.pakbus.util.Checks;

/**
 * Instances of this class represent PakBus array data types.  There are three types of PakBus arrays: fixed-length, variable-length terminated by a
 * zero, and terminated by data bounding.  Instances of this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ArrayDataType extends ADataType {


    final private DataType itemType;        // present for all array types; the type of the array's elements...
    final private DataType terminatorType;  // for zero-terminated arrays, the simple type of the terminator; null for other array types...
    final private int length;               // for fixed-length arrays, the number of elements in the array; zero for other array types...


    /**
     * Constructs a new instance of this type representing a zero-terminated variable-length array, with the given name and code.  The given item type
     * is the data type of the array's elements, and the terminator type is the data type of the terminating zero.
     *
     * @param _name the unique name of the data type
     * @param _pakBusType the PakBus data type equivalent to this type, or null if none
     * @param _itemType the data type for the array's elements
     * @param _terminatorType the data type for the array's terminating zero
     */
    public ArrayDataType( final String _name, final PakBusType _pakBusType, final DataType _itemType, final DataType _terminatorType ) {
        super( _name, _pakBusType, 0, GeneralDataType.Array, null );

        // sanity check...
        Checks.required( _itemType, _terminatorType );

        itemType = _itemType;
        terminatorType = _terminatorType;
        length = 0;
    }


    /**
     * Constructs a new instance of this type representing a fixed-length array, with the given name and code.  The given item type is the data type
     * of the array's elements, and the given length is the fixed number of elements in the array.
     *
     * @param _name the unique name of the data type
     * @param _pakBusType the PakBus data type equivalent to this type, or null if none
     * @param _itemType the data type for the array's elements
     * @param _length the fixed number of elements in the array
     */
    public ArrayDataType( final String _name, final PakBusType _pakBusType, final DataType _itemType, final int _length ) {
        super( _name, _pakBusType, _length * _itemType.bits(), GeneralDataType.Array, null );

        // sanity check...
        Checks.inBounds( _length, 1, 1000, "Length of array is invalid: " + _length );

        itemType = _itemType;
        terminatorType = null;
        length = _length;
    }


    /**
     * Constructs a new instance of this type representing a data-bound variable-length array, with the given name and code.  The given item type is
     * the data type of the array's elements.
     *
     * @param _name the unique name of the data type
     * @param _pakBusType the PakBus data type equivalent to this type, or null if none
     * @param _itemType the data type for the array's elements
     */
    public ArrayDataType( final String _name, final PakBusType _pakBusType, final DataType _itemType ) {
        super( _name, _pakBusType, 0, GeneralDataType.Array, null );

        // sanity check...
        Checks.required( _itemType );

        itemType = _itemType;
        terminatorType = null;
        length = 0;
    }


    public DataType getItemType() {
        return itemType;
    }


    public DataType getTerminatorType() {
        return terminatorType;
    }


    public int getLength() {
        return length;
    }


    public boolean isFixedLength() {
        return length != 0;
    }


    public boolean isZeroTerminated() {
        return terminatorType != null;
    }


    public boolean isDataBound() {
        return (length == 0) && (terminatorType == null);
    }
}
