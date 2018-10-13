package com.dilatush.pakbus;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.CompositeDataType;
import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.values.CompositeDatum;

import static com.dilatush.pakbus.types.DataTypes.*;

/**
 * Instances of this class represent the low-level PakBus packet.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class PacketDatum extends CompositeDatum {


    final static private CompositeDataType HILEVEL = new CompositeDataType( "HiLevel", null,
            new CP( "HiProtoCode", BITS4  ),
            new CP( "DstNodeId",   BITS12 ),
            new CP( "HopCnt",      BITS4  ),
            new CP( "SrcNodeId",   BITS12 ),
            new CP( "Message",     BYTES, true  ) );


    final static private DataType TYPE = getType();


    public PacketDatum() {
        super( TYPE );
    }


    private static DataType getType() {

        return new CompositeDataType( "Packet", null,
                new CP( "LinkState",   BITS4  ),
                new CP( "DstPhyAddr",  BITS12 ),
                new CP( "ExpMoreCode", BITS2  ),
                new CP( "Priority",    BITS2  ),
                new CP( "SrcPhyAddr",  BITS12 ),
                new CP( "HiLevel",     HILEVEL, true ) );
    }
}
