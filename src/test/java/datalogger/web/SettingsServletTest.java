package datalogger.web;

import datalogger.configuration.DataLoggerConfiguration;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Konstantin Kosmachevskiy
 */
public class SettingsServletTest {
    private static final String PART_NAME = "file";
    private static final String TEST_DATA = "data";
    private static final String FILE_NAME = "some.file";

    @Test
    public void testDoPost() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        SettingsServlet servlet = new SettingsServlet();

        createMultipartFormDataRequest(request);

        File file = new File(DataLoggerConfiguration.DEFAULT_CONFIG_FILE_LOCATION);
        file.delete();
        Assert.assertFalse(file.exists());

        servlet.doPost(request, response);

        Assert.assertEquals(200, response.getStatus());

        byte[] bytes = Files.readAllBytes(Paths.get(DataLoggerConfiguration.DEFAULT_CONFIG_FILE_LOCATION));
        Assert.assertArrayEquals(TEST_DATA.getBytes(), bytes);
    }

    @Test
    public void youCanGetAnError() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        SettingsServlet servlet = new SettingsServlet();

        servlet.doPost(request, response);

        Assert.assertEquals(400, response.getStatus());
    }


    public void createMultipartFormDataRequest(MockHttpServletRequest request) throws IOException {

        Part[] parts = new Part[]{
                new FilePart(PART_NAME, new ByteArrayPartSource(FILE_NAME, TEST_DATA.getBytes()))};
        MultipartRequestEntity multipartRequestEntity =
                new MultipartRequestEntity(parts, new PostMethod().getParams());
        // Serialize request body
        ByteArrayOutputStream requestContent = new ByteArrayOutputStream();
        multipartRequestEntity.writeRequest(requestContent);
        // Set request body to HTTP servlet request
        request.setContent(requestContent.toByteArray());
        // Set content type to HTTP servlet request (important, includes Mime boundary string)
        request.setContentType(multipartRequestEntity.getContentType());
    }
}