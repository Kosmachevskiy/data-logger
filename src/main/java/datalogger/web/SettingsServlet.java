package datalogger.web;

import datalogger.modbus.ConfigurationService;
import datalogger.modbus.ModbusPollerService;
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
import java.io.IOException;
import java.util.List;

/**
 * @author Konstantin Kosmachevskiy
 */
@WebServlet(urlPatterns = {"/api/setting"})
public class SettingsServlet extends HttpServlet {

    @Autowired(required = false)
    private ModbusPollerService modbusPollerService;
    @Autowired
    private ConfigurationService configurationService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        Util.sendFile(response, configurationService.getConfigFileFullPath());
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

                    configurationService.save(item.getInputStream());

                    modbusPollerService.start(configurationService.load());

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

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
