package datalogger.modbus;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Konstantin Kosmachevskiy
 */
public class PipeTest {
    @Test
    public void getInputStream() throws Exception {
        SerialPort.FakePortWrapper.Pipe pipe = new SerialPort.FakePortWrapper.Pipe();
        pipe.buffer = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        pipe.pointer = 9;

        InputStream inputStream = pipe.getInputStream();

        Assert.assertEquals(inputStream.available(), 9);
        Assert.assertEquals(inputStream.read(), 1);
        Assert.assertEquals(inputStream.available(), 8);

        byte[] dst = new byte[10];
        inputStream.read(dst, 5, 5);
        Assert.assertArrayEquals(dst, new byte[]{0, 0, 0, 0, 0, 2, 3, 4, 5, 6});
        Assert.assertEquals(inputStream.available(), 3);

        inputStream.read(dst);
        Assert.assertArrayEquals(dst, new byte[]{7, 8, 9, 0, 0, 2, 3, 4, 5, 6});

        Assert.assertEquals(inputStream.skip(10), 0);

        pipe.buffer = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        pipe.pointer = 9;
        Assert.assertEquals(inputStream.available(), 9);

        Assert.assertEquals(inputStream.skip(5), 5);

        dst = new byte[10];
        Assert.assertEquals(inputStream.read(dst), 4);
        Assert.assertArrayEquals(dst, new byte[]{6, 7, 8, 9, 0, 0, 0, 0, 0, 0});
    }

    @Test
    public void getOutputStream() throws Exception {
        SerialPort.FakePortWrapper.Pipe pipe = new SerialPort.FakePortWrapper.Pipe();
        OutputStream outputStream = pipe.getOutputStream();

        pipe.buffer = new byte[10];

        outputStream.write(12);
        Assert.assertArrayEquals(new byte[]{12, 0, 0, 0, 0, 0, 0, 0, 0, 0}, pipe.buffer);

        outputStream.write(16);
        Assert.assertArrayEquals(new byte[]{12, 16, 0, 0, 0, 0, 0, 0, 0, 0}, pipe.buffer);

        outputStream.write(new byte[]{1, 2, 3, 0, 5});
        Assert.assertArrayEquals(new byte[]{12, 16, 1, 2, 3, 0, 5, 0, 0, 0,}, pipe.buffer);

        outputStream.write(new byte[]{99, 88, 77}, 1, 2);
        Assert.assertArrayEquals(new byte[]{12, 16, 1, 2, 3, 0, 5, 88, 77, 0}, pipe.buffer);
    }

    @Test
    public void readAndWrite() throws IOException {
        SerialPort.FakePortWrapper.Pipe pipe = new SerialPort.FakePortWrapper.Pipe();
        String data = "SomeData\nToTransmit.\n";

        pipe.getOutputStream().write(data.getBytes());

        byte[] b = new byte[pipe.getInputStream().available()];
        pipe.getInputStream().read(b);
        Assert.assertEquals(data, new String(b));
    }

}