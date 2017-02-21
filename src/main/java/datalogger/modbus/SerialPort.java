package datalogger.modbus;

import com.serotonin.modbus4j.serial.SerialPortWrapper;
import datalogger.modbus.configuration.SerialConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Konstantin Kosmachevskiy
 */
public class SerialPort {

    private static final String DEMO_SERIAL_PORT_NAME_PREFIX = "DEMO_SERIAL_PIPE:POINT_";
    public static final String DEMO_SERIAL_PORT_POINT_A = DEMO_SERIAL_PORT_NAME_PREFIX + "A";
    public static final String DEMO_SERIAL_PORT_POINT_B = DEMO_SERIAL_PORT_NAME_PREFIX + "B";

    private SerialPort() {
    }

    public static SerialPortWrapper getWrapper(SerialConfiguration configuration) {
        SerialPortWrapper wrapper;
        if (configuration.getPort().startsWith(DEMO_SERIAL_PORT_NAME_PREFIX)) {
            wrapper = new FakePortWrapper(configuration);
        } else {
            wrapper = new JSerialCommWrapper(configuration);
        }
        return wrapper;
    }

    private static class JSerialCommWrapper implements SerialPortWrapper {

        private com.fazecast.jSerialComm.SerialPort serialPort;

        public JSerialCommWrapper(SerialConfiguration configuration) {
            serialPort = com.fazecast.jSerialComm.SerialPort.getCommPort(configuration.getPort());

            serialPort.setBaudRate(configuration.getBaudRate());
            serialPort.setNumDataBits(configuration.getDataBits());
            serialPort.setNumStopBits(configuration.getStopBits());
            serialPort.setParity(configuration.getParity());
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

    static class FakePortWrapper implements SerialPortWrapper {
        private static Pipe pipeA = new Pipe();
        private static Pipe pipeB = new Pipe();

        private SerialConfiguration serialConfiguration;
        private OutputStream outputStream;
        private InputStream inputStream;

        FakePortWrapper(SerialConfiguration serialConfiguration) {
            this.serialConfiguration = serialConfiguration;
            if (serialConfiguration.getPort().equals(DEMO_SERIAL_PORT_POINT_A)) {
                inputStream = pipeA.getInputStream();
                outputStream = pipeB.getOutputStream();
            }
            if (serialConfiguration.getPort().equals(DEMO_SERIAL_PORT_POINT_B)) {
                inputStream = pipeB.getInputStream();
                outputStream = pipeA.getOutputStream();
            }
        }


        @Override
        public void close() throws Exception {
            outputStream.close();
            inputStream.close();
        }

        @Override
        public void open() throws Exception {
            outputStream.close();
            inputStream.close();

        }

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public OutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public int getBaudRate() {
            return serialConfiguration.getBaudRate();
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
            return serialConfiguration.getDataBits();
        }

        @Override
        public int getStopBits() {
            return serialConfiguration.getStopBits();
        }

        @Override
        public int getParity() {
            return serialConfiguration.getParity();
        }

        static class Pipe {
            byte buffer[] = new byte[1024];
            int pointer = 0;
            private InputStream inputStream = new FakeInputStream();
            private OutputStream outputStream = new FakeOutputStream();

            public InputStream getInputStream() {
                return inputStream;
            }

            public OutputStream getOutputStream() {
                return outputStream;
            }

            private class FakeInputStream extends InputStream {
                @Override
                public int read() throws IOException {
                    if (available() == 0)
                        return -1;

                    // -- Reading --//
                    int value = buffer[0] & 255;

                    //-- Shifting --//
                    System.arraycopy(buffer, 1, buffer, 0, pointer - 1);
                    pointer--;

                    return value;
                }

                @Override
                public int read(byte[] b) throws IOException {
                    return read(b, 0, b.length);
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    if (b == null)
                        throw new NullPointerException();
                    if (available() == 0)
                        return -1;
                    if (len == 0)
                        return 0;
                    if (available() - len < 0)
                        len = available();

                    //-- Reading --//
                    System.arraycopy(buffer, 0, b, off, len);

                    //-- Shifting--//
                    System.arraycopy(buffer, len, buffer, 0, available() - len);
                    pointer = pointer - len;

                    return len;
                }

                @Override
                public long skip(long n) throws IOException {
                    if (n <= 0)
                        return 0;
                    if (available() == 0)
                        return 0;
                    if (available() - n < 0)
                        n = available();

                    //-- Shifting --//
                    System.arraycopy(buffer, (int) n, buffer, 0, pointer - (int) n);
                    pointer = (int) (pointer - n);
                    return n;
                }

                @Override
                public int available() throws IOException {
                    return pointer;
                }

                @Override
                public void close() throws IOException {
                    pointer = 0;
                }
            }

            private class FakeOutputStream extends OutputStream {
                @Override
                public void write(int b) throws IOException {
                    buffer[pointer] = (byte) b;
                    pointer++;
                }

                @Override
                public void write(byte[] b) throws IOException {
                    write(b, 0, b.length);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    if (b == null)
                        throw new NullPointerException();
                    if (b.length <= 0)
                        return;
                    if (len == 0)
                        return;
                    if (off < 0)
                        return;
                    System.arraycopy(b, off, buffer, pointer, len);
                    pointer += len;
                }

                @Override
                public void close() throws IOException {
                    pointer = 0;
                }
            }
        }
    }
}