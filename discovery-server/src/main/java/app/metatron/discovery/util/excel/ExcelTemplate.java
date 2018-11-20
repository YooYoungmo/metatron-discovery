package app.metatron.discovery.util.excel;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelTemplate {
  private Workbook workbook;

  public ExcelTemplate(File targetFile) throws IOException {
    if ("xls".equals(FilenameUtils.getExtension(targetFile.getName()))) {       // 97~2003
      this.workbook = new HSSFWorkbook(new FileInputStream(targetFile));
    } else {   // 2007 ~
      // old POI library
      InputStream is = new FileInputStream(targetFile);
      this.workbook = StreamingReader.builder()
          .rowCacheSize(100)
          .bufferSize(4096)
          .open(is);
    }
  }

  public <C,R> ExcelSheet<C, R> getSheet(String sheetName,
                                         ExcelFirstRowMapper<C> firstRowMapper,
                                         ExcelRowMapper<C, R> rowMapper,
                                         boolean skipFirstRow) {
    int sheetIndex = workbook.getSheetIndex(sheetName);
    Sheet sheet = workbook.getSheetAt(sheetIndex);

    C headers = null;
    List<R> rows = new ArrayList<>();
    for (Row row : sheet) {
      if(row.getRowNum() == 0) {
        headers = firstRowMapper.mapRow(row);
        if(skipFirstRow == false) {
          rows.add(rowMapper.mapRow(headers, row));
        }
      } else {
        rows.add(rowMapper.mapRow(headers, row));
      }
    }
    return new ExcelSheet<>(headers, rows);
  }
}
