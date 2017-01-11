package datalogger.web;

import datalogger.configuration.DataLoggerConfiguration;
import datalogger.modbus.ModbusService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Konstantin Kosmachevskiy
 */
@WebServlet(urlPatterns = {"/api/setting"})
public class SettingsServlet extends HttpServlet {

    @Autowired
    private ModbusService modbusService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        Util.sendFile(response, DataLoggerConfiguration.getConfigFile());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
            if (items.size() != 1) {
                resp.setStatus(400);
                return;
            }
            for (FileItem item : items) {
                if (!item.isFormField()) {

                    // TODO: maybe better to create a new config via static method of DataLoggerConfiguration class or via ConfigService?
                    File file = new File(DataLoggerConfiguration.DEFAULT_CONFIG_FILE_LOCATION);
                    file.delete();
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    InputStream fileContent = item.getInputStream();
                    while (fileContent.available() > 0)
                        fileOutputStream.write(fileContent.read());
                    fileOutputStream.close();

                    //TODO: restart modbusService here
                    resp.setStatus(200);
                }
            }
        } catch (FileUploadException e) {
            resp.setStatus(400);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
        super.init(config);
    }
}
