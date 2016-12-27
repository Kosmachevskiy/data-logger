package datalogger;

import datalogger.dao.EntryDao;
import datalogger.model.Entry;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class ReportBuilder {

    //TODO: to fix trouble with file location and permission
    private static final String REPORT_TMP_FILE_PATH = "./dataType-logger-report.xlsx";
    @Autowired
    private EntryDao entryDao;

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

        File file = new File(REPORT_TMP_FILE_PATH);
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
