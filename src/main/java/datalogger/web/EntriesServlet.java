package datalogger.web;

import com.google.gson.Gson;
import datalogger.dao.EntryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Konstantin Kosmachevskiy on 18.11.16.
 */
@WebServlet(urlPatterns = {"/api/entries"})
public class EntriesServlet extends HttpServlet {

    @Autowired
    private EntryDao entryDao;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        String entries = gson.toJson(entryDao.getAll());
        ServletOutputStream outputStream = resp.getOutputStream();
        outputStream.write(entries.getBytes());
        outputStream.flush();
        resp.setStatus(200);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
        super.init(config);
    }
}
