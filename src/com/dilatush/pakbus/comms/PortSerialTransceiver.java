package com.dilatush.pakbus.comms;

import com.dilatush.pakbus.util.Checks;
import com.fazecast.jSerialComm.SerialPort;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

/**
 * Instances of this class are serial transceivers that send and receive data through a serial port.  This class is dependent on
 * {@link com.fazecast.jSerialComm.SerialPort jSerialComm}.  Two daemon threads are created and managed by this class (a reader and writer).
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class PortSerialTransceiver implements SerialTransceiver {

    final static private Logger LOGGER = Logger.getLogger( new Object(){}.getClass().getEnclosingClass().getCanonicalName() );

    final static public int STOP_BITS_ONE            = 1;
    final static public int STOP_BITS_ONE_POINT_FIVE = 2;
    final static public int STOP_BITS_TWO            = 3;

    final static public int PARITY_ODD               = 1;
    final static public int PARITY_EVEN              = 2;
    final static public int PARITY_NONE              = 3;
    final static public int PARITY_MARK              = 4;
    final static public int PARITY_SPACE             = 5;

    final static public int FLOW_CTRL_NONE           = 1;
    final static public int FLOW_CTRL_CTS            = 2;
    final static public int FLOW_CTRL_DSR            = 3;
    final static public int FLOW_CTRL_CTS_RTS        = 4;
    final static public int FLOW_CTRL_DTR_RTS        = 5;
    final static public int FLOW_CTRL_XONOFF         = 6;

    final static private int   TRANSMIT_QUEUE_SIZE = 15000;
    final static private int   RECEIVE_QUEUE_SIZE  = 1200;
    final static private int[] VALID_BAUD_RATES =
            { 50, 75, 110, 135, 150, 300, 600, 1200, 1800, 2400, 4800, 7200, 9600, 14400, 19200, 38400, 56000, 57600, 115200, 115200, 128000};


    final private SerialPort port;
    final private Reader     reader;
    final private Writer     writer;

    private boolean stop;


    /**
     * Creates a new instance of this class with the given opened port.
     *
     * @param _port the port to use for this transceiver
     */
    private PortSerialTransceiver( final SerialPort _port ) {

        // sanity check...
        Checks.required( _port );
        Checks.isTrue( _port.isOpen(), "Serial port is not open" );

        // some setup...
        port = _port;
        stop = false;
        reader = new Reader();
        writer = new Writer();
    }


    /**
     * Creates and return a new instance of this class with the given parameters, or null if the port could not be found or opened.
     * <p>The port system name must match the {@link AvailablePort#portSystemName} in one of the {@link AvailablePort}s returned by
     * {@link #getAvailablePorts()}.</p>
     * <p>The baud rate must be one of the industry-standard baud rates (50, 75, 110, 135, 150, 300, 600, 1200, 1800, 2400, 4800, 7200,
     * 9600, 14400, 19200, 38400, 56000, 57600, 115200, 115200, or 128000).</p>
     * <p>The data bits must be number of data bits in each character transmitted or received on this port ([5..8]).</p>
     * <p>The stop bit code defines how many stop bits to use on this port (one of {@link #STOP_BITS_ONE}, {@link #STOP_BITS_ONE_POINT_FIVE}, or
     * {@link #STOP_BITS_TWO}).</p>
     * <p>The parity code defines what kind of parity to use on this port (one of {@link #PARITY_ODD}, {@link #PARITY_EVEN}, {@link #PARITY_NONE},
     * {@link #PARITY_MARK}, or {@link #PARITY_SPACE}).</p>
     * <p>The flow control kind defines the type of flow control to use on this port (one of {@link #FLOW_CTRL_NONE}, {@link #FLOW_CTRL_CTS},
     * {@link #FLOW_CTRL_DSR}, {@link #FLOW_CTRL_CTS_RTS}, {@link #FLOW_CTRL_DTR_RTS}, {@link #FLOW_CTRL_XONOFF}).</p>
     *
     * @param _portSystemName  the port system name
     * @param _baudRate the baud rate for the port
     * @param _dataBits the number of data bits for the port
     * @param _stopBitCode the code indicating how many stop bits for the port
     * @param _parityCode the code indicating the parity for the port
     * @param _flowControlCode the code indicating the flow control mode for the port
     */
    static public PortSerialTransceiver getSerialTransceiver( final String _portSystemName, final int _baudRate, final int _dataBits,
                                  final int _stopBitCode, final int _parityCode, final int _flowControlCode ) {

        // sanity checks...
        Checks.notEmpty( _portSystemName );
        Checks.isTrue( isValidBaudRate( _baudRate ), "Baud rate is invalid: " + _baudRate );
        Checks.inBounds( _dataBits,   5,        8,                "Number of data bits is out of range [5..8]: " + _dataBits );
        Checks.inBounds( _stopBitCode,     STOP_BITS_ONE,  STOP_BITS_TWO,    "Stop bit code is invalid: " + _stopBitCode                );
        Checks.inBounds( _parityCode,      PARITY_ODD,     PARITY_SPACE,     "Parity mode code is invalid: " + _parityCode              );
        Checks.inBounds( _flowControlCode, FLOW_CTRL_NONE, FLOW_CTRL_XONOFF, "Flow control mode code is invalid: " + _flowControlCode   );

        // get the port...
        SerialPort port = SerialPort.getCommPort( _portSystemName );

        // set our basic configuration parameters...
        int sb = getJSerialPortStopBitsCode(  _stopBitCode );
        int pm = getJSerialPortParityModeCode( _parityCode );
        int fc = getJSerialPortFlowControlCode( _flowControlCode );
        port.setComPortParameters( _baudRate, _dataBits, sb, pm );
        port.setFlowControl( fc );

        // now set our timeout mode for full-on blocking, read and write...
        port.setComPortTimeouts( SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0 );

        // now let's see if we can open this port (with small queues, as we're providing them here)...
        if( !port.openPort( 0, 100, 100 ) )
            return null;

        // ok, we have a port and it's open...
        return new PortSerialTransceiver( port );
    }


    /**
     * Adds the given byte to the ordered queue of bytes to be transmitted from this serial transceiver, returning true if the byte was successfully
     * added.  A false return indicates that the transmission queue is full and the byte could not be added.
     *
     * @param _byte the byte to be added to the transmission queue
     * @return true if the byte was successfully added
     */
    @Override
    public boolean tx( final byte _byte ) {
        return writer.queue.offerFirst( _byte );
    }


    /**
     * Returns the next byte received from this serial transceiver, blocking until it becomes available.
     *
     * @return the next byte received
     * @throws InterruptedException if interrupted while blocked
     */
    @Override
    public byte rx() throws InterruptedException {
        return reader.queue.takeLast();
    }


    /**
     * Returns how much time has elapsed since the last byte was received.
     *
     * @return how much time has elapsed since the last byte was received
     */
    @Override
    public Duration sinceRx() {
        return Duration.between( reader.lastByte, Instant.now() );
    }


    /**
     * Returns how much time has elapsed since the last byte was transmitted.
     *
     * @return how much time has elapsed since the last byte was transmitted
     */
    @Override
    public Duration sinceTx() {
        return Duration.between( writer.lastByte, Instant.now() );
    }


    /**
     * Returns the jSerialPort stop bit code that is equivalent to this class' stop bit code.
     *
     * @param _stopBitCode the stop bit code to convert
     * @return the jSerialPort stop bit code
     */
    static private int getJSerialPortStopBitsCode( final int _stopBitCode ) {
        switch( _stopBitCode ) {
            case STOP_BITS_ONE:            return SerialPort.ONE_STOP_BIT;
            case STOP_BITS_ONE_POINT_FIVE: return SerialPort.ONE_POINT_FIVE_STOP_BITS;
            case STOP_BITS_TWO:            return SerialPort.TWO_STOP_BITS;
            default: throw new IllegalArgumentException( "Invalid stop bit code: " + _stopBitCode );
        }
    }


    /**
     * Returns the jSerialPort parity mode code that is equivalent to this class' parity mode code.
     *
     * @param _parityModeCode the parity mode code to convert
     * @return the jSerialPort parity mode code
     */
    static private int getJSerialPortParityModeCode( final int _parityModeCode ) {
        switch( _parityModeCode ) {
            case PARITY_EVEN:  return SerialPort.EVEN_PARITY;
            case PARITY_ODD:   return SerialPort.ODD_PARITY;
            case PARITY_NONE:  return SerialPort.NO_PARITY;
            case PARITY_MARK:  return SerialPort.MARK_PARITY;
            case PARITY_SPACE: return SerialPort.SPACE_PARITY;
            default: throw new IllegalArgumentException( "Invalid parity mode code: " + _parityModeCode );
        }
    }


    /**
     * Returns the jSerialPort flow control mode code that is equivalent to this class' flow control mode code.
     *
     * @param _flowControlCode the flow control code to convert
     * @return the jSerialPort flow control mode code
     */
    static private int getJSerialPortFlowControlCode( final int _flowControlCode ) {
        switch( _flowControlCode ) {
            case FLOW_CTRL_NONE:     return SerialPort.FLOW_CONTROL_DISABLED;
            case FLOW_CTRL_CTS:      return SerialPort.FLOW_CONTROL_CTS_ENABLED;
            case FLOW_CTRL_CTS_RTS:  return SerialPort.FLOW_CONTROL_RTS_ENABLED        | SerialPort.FLOW_CONTROL_CTS_ENABLED;
            case FLOW_CTRL_DSR:      return SerialPort.FLOW_CONTROL_DSR_ENABLED;
            case FLOW_CTRL_DTR_RTS:  return SerialPort.FLOW_CONTROL_RTS_ENABLED        | SerialPort.FLOW_CONTROL_DTR_ENABLED;
            case FLOW_CTRL_XONOFF:   return SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED;
            default: throw new IllegalArgumentException( "Invalid flow control mode code: " + _flowControlCode );
        }
    }


    /**
     * Returns true if the given baud rate is one of the valid rates.
     *
     * @param _rate the baud rate to check
     * @return true if the given baud rate is valid
     */
    static private boolean isValidBaudRate( final int _rate ) {
        for( int validRate : VALID_BAUD_RATES ) {
            if( _rate == validRate )
                return true;
        }
        return false;
    }


    /**
     * Returns a list of all the serial ports available in this system, enabling programmatic discovery of them.
     *
     * @return the list of available ports
     */
    public static List<AvailablePort> getAvailablePorts() {

        List<AvailablePort> result = new ArrayList<>();
        SerialPort[] ports = SerialPort.getCommPorts();
        for( SerialPort port : ports ) {
            AvailablePort ap = new AvailablePort();
            ap.portDescription = port.getPortDescription();
            ap.portName = port.getPortDescription();
            ap.portSystemName = port.getSystemPortName();
            result.add( ap );
        }
        return result;
    }


    public static class AvailablePort {
        public String portDescription;
        public String portName;
        public String portSystemName;
    }


    /**
     * Returns true if this instance has been stopped (because of a serial port error).
     *
     * @return true if this instance has been stopped
     */
    public boolean isStopped() {
        return stop;
    }


    /**
     * Stops this instance when an error occurs on the port.
     */
    private void stop() {
        stop = true;
    }


    private class Reader extends Thread {

        final private LinkedBlockingDeque<Byte> queue;
        private Instant lastByte;


        private Reader() {
            setName( "PortSerialTransceiver.Reader" );
            setDaemon( true );
            queue = new LinkedBlockingDeque<>( RECEIVE_QUEUE_SIZE );
            lastByte = Instant.now();
            start();
        }


        public void run() {

            byte[] buffer = new byte[1];

            while( !stop ) {

                try {
                    // get a byte from the serial port...
                    int result = port.readBytes( buffer, 1 );

                    // if we had an error, time to shut it all down...
                    if( result != 1 ) {
                        LOGGER.warning( "Read error on serial port" );
                        PortSerialTransceiver.this.stop();
                        continue;
                    }

                    // we really got a byte, so mark the time and stuff it in our receive queue...
                    lastByte = Instant.now();
                    queue.putFirst( buffer[0] );
                }
                catch( InterruptedException _e ) {
                    PortSerialTransceiver.this.stop();
                }
            }
        }
    }


    private class Writer extends Thread {

        final private LinkedBlockingDeque<Byte> queue;
        private Instant lastByte;


        private Writer() {
            setName( "PortSerialTransceiver.Writer" );
            setDaemon( true );
            start();
            queue = new LinkedBlockingDeque<>( TRANSMIT_QUEUE_SIZE );
            lastByte = Instant.now();
        }


        public void run() {

            byte[] buffer = new byte[1];

            while( !stop ) {

                try {
                    // get a byte from the queue...
                    buffer[0] = queue.takeLast();

                    // write the byte to the serial port...
                    int result = port.writeBytes( buffer, 1 );

                    // if we had an error, time to shut it all down...
                    if( result != 1 ) {
                        PortSerialTransceiver.this.stop();
                        continue;
                    }

                    // we really got a byte, so mark the time...
                    lastByte = Instant.now();
                }
                catch( InterruptedException _e ) {
                    PortSerialTransceiver.this.stop();
                }
            }
        }
    }
}
