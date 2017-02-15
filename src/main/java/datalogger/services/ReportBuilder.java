package datalogger.services;

import datalogger.model.Entry;
import datalogger.model.dao.EntryDao;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@PropertySource("classpath:app.properties")
public class ReportBuilder {

    @Autowired
    private EntryDao entryDao;
    @Autowired
    private Environment environment;
    private String tmpFile;

    @PostConstruct
    public void init() {
        tmpFile = environment.getProperty("app.home") + "data-logger-services.xlsx";
    }

    public File buildReport() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        CellStyle cellDateStyle = workbook.createCellStyle();
        CellStyle cellTimeStyle = workbook.createCellStyle();

        CreationHelper creationHelper = workbook.getCreationHelper();
        cellDateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("d/m/yy"));
        cellTimeStyle.setDataFormat(creationHelper.createDataFormat().getFormat("h:mm:s"));

        XSSFRow row;

        int rowNum = 0;
        row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue("Date");
        row.createCell(1).setCellValue("Time");
        row.createCell(2).setCellValue("Value");
        row.createCell(3).setCellValue("Unit");

        for (Entry entry : entryDao.getAll()) {
            int cellNum = 0;
            row = sheet.createRow(++rowNum);

            XSSFCell date = row.createCell(cellNum++);
            date.setCellValue(entry.getDate());
            date.setCellStyle(cellDateStyle);

            XSSFCell time = row.createCell(cellNum++);
            time.setCellValue(entry.getTime());
            time.setCellStyle(cellTimeStyle);

            row.createCell(cellNum++).setCellValue(entry.getValue());
            row.createCell(cellNum++).setCellValue(entry.getUnit());
        }

        File file = new File(tmpFile);
        if (file.exists()) file.delete();

        try {
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            workbook.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
