package datalogger.web;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Konstantin Kosmachevskiy
 */
public class UtilTest {

    private static File createTestFile() throws IOException {
        String data = "SomeData";
        File file = new File("./some.file");
        file.createNewFile();
        file.deleteOnExit();

        FileOutputStream stream = new FileOutputStream(file);
        stream.write(data.getBytes());
        stream.close();
        return file;
    }

    @Test
    public void sendFile() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        File originalFile = createTestFile();
        Util.sendFile(response, originalFile);

        File file = new File("./tmp.file");
        file.deleteOnExit();
        file.createNewFile();

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(response.getContentAsByteArray());
        fileOutputStream.close();

        Assert.assertEquals("application/octet-stream", response.getContentType());
        Assert.assertEquals(originalFile.length(), response.getContentLength());
        Assert.assertEquals(String.format("attachment; filename=\"%s\"", originalFile.getName()),
                response.getHeader("Content-Disposition"));
        Assert.assertEquals(originalFile.length(), file.length());

    }

}