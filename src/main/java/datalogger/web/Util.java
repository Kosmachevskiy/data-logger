package datalogger.web;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Konstantin Kosmachevskiy
 */
public class Util {
    static void sendFile(HttpServletResponse response, File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);

        // TODO: remove this?
//        ServletContext context = servlet.getServletContext();
//
//        String mimeType = context.getMimeType(file.getPath());
//        if (mimeType == null) {
//            mimeType = "application/octet-stream";
//        }
        response.setContentType("application/octet-stream");
        response.setContentLength((int) file.length());

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", file.getName());
        response.setHeader(headerKey, headerValue);

        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;

        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        fileInputStream.close();
        outStream.close();
    }

}
