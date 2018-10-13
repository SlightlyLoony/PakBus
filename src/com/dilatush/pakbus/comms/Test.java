package com.dilatush.pakbus.comms;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import static java.lang.Thread.sleep;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Test {

    public static void main( final String[] _args ) throws InterruptedException {

        SerialPort[] ports = SerialPort.getCommPorts();
        SerialPort port = SerialPort.getCommPort( "/dev/cu.usbserial-AC01R521" );

        port.setComPortParameters( 9600, 8, 1, SerialPort.NO_PARITY );
        port.setFlowControl( SerialPort.FLOW_CONTROL_DISABLED );
        port.addDataListener( new Listener() );

        boolean opened = port.openPort();

        byte[] wb = new byte[] { 0x55, 0x66, 0x77, 0x11, 0x22, 0x33 };
        port.writeBytes( wb, wb.length );

        sleep( 2000 );

        port.hashCode();
    }

    private static class Listener implements SerialPortDataListener {

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }


        @Override
        public void serialEvent( final SerialPortEvent _serialPortEvent ) {

            byte[] bytes = _serialPortEvent.getReceivedData();
            System.out.println( "Got " + bytes.length + " bytes" );
            for( int i = 0; i < bytes.length; i++ )
                System.out.println( "Got data: " + Integer.toHexString( 0xFF & bytes[i] ) );
        }
    }

}
