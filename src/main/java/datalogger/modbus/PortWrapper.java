package datalogger.modbus;

import com.fazecast.jSerialComm.SerialPort;
import com.serotonin.modbus4j.serial.SerialPortWrapper;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Konstantin Kosmachevskiy
 */
public class PortWrapper implements SerialPortWrapper {

    private SerialPort serialPort;

    public PortWrapper(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public void close() throws Exception {
        serialPort.closePort();
    }

    @Override
    public void open() throws Exception {
        serialPort.openPort();
    }

    @Override
    public InputStream getInputStream() {
        return serialPort.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return serialPort.getOutputStream();
    }

    @Override
    public int getBaudRate() {
        return serialPort.getBaudRate();
    }

    @Override
    public int getFlowControlIn() {
        return 0;
    }

    @Override
    public int getFlowControlOut() {
        return 0;
    }

    @Override
    public int getDataBits() {
        return serialPort.getNumDataBits();
    }

    @Override
    public int getStopBits() {
        return serialPort.getNumStopBits();
    }

    @Override
    public int getParity() {
        return serialPort.getParity();
    }
}