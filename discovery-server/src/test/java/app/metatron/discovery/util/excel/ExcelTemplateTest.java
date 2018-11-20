package app.metatron.discovery.util.excel;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ExcelTemplateTest {

  @Test
  public void getSheetRows_when_skipFirstRow_true() throws IOException {
    // given
    final String filePath = getClass().getClassLoader().getResource("sales-product.xlsx").getPath();
    ExcelTemplate excelTemplate = new ExcelTemplate(new File(filePath));
    final String sheetName = "sales";
    final boolean firstHeaderRow = true;

    // when
    ExcelSheet<Map<Integer, String>, Map<String, String>> excelSheet = excelTemplate.getSheet(sheetName, firstRow -> {
      Map<Integer, String> headers = Maps.newTreeMap();
      for (Cell cell : firstRow) {
        int columnIndex = cell.getColumnIndex();
        if(firstHeaderRow) {
          headers.put(columnIndex, StringUtils.defaultString(cell.getStringCellValue(), "col_" + (columnIndex + 1)));
        } else {
          headers.put(columnIndex,  "col_" + (columnIndex + 1));
        }
      }
      return headers;
    }, (headers, row) -> {
      Map<String, String> rowMap = Maps.newTreeMap();
      for (Cell cell : row) {
        int columnIndex = cell.getColumnIndex();
        if (headers.containsKey(columnIndex)) {
          rowMap.put(headers.get(columnIndex), cell.getStringCellValue());
        }
      }
      return rowMap;
    }, true);

    // then
    assertThat(excelSheet.getHeaders()).hasSize(5);
    assertThat(excelSheet.getHeaders()).containsValues("time", "order_id", "amount", "product_id", "sale_count");

    assertThat(excelSheet.getRows()).hasSize(9);
    assertThat(excelSheet.getRows()).extracting("time").contains("20/04/2017","21/04/2017","22/04/2017","23/04/2017","24/04/2017","25/04/2017","26/04/2017","27/04/2017","28/04/2017");
    assertThat(excelSheet.getRows()).extracting("order_id").contains("1", "2", "3", "4", "5", "6", "7", "8", "9");
    assertThat(excelSheet.getRows()).extracting("amount").contains("20","300","400","550","129","212","412","412","2111");
    assertThat(excelSheet.getRows()).extracting("product_id").contains("1","1","2","2","3","3","4","4","5");
    assertThat(excelSheet.getRows()).extracting("sale_count").contains("1","2","3","4","1","2","3","4","5");
  }

  @Test
  public void getSheetRows_when_skipFirstRow_false() throws IOException {
    // given
    final String filePath = getClass().getClassLoader().getResource("sales-product.xlsx").getPath();
    ExcelTemplate excelTemplate = new ExcelTemplate(new File(filePath));
    final String sheetName = "sales";
    final boolean firstHeaderRow = false;

    // when
    ExcelSheet<Map<Integer, String>, Map<String, String>> excelSheet = excelTemplate.getSheet(sheetName, firstRow -> {
      Map<Integer, String> headers = Maps.newTreeMap();
      for (Cell cell : firstRow) {
        int columnIndex = cell.getColumnIndex();
        if(firstHeaderRow) {
          headers.put(columnIndex, StringUtils.defaultString(cell.getStringCellValue(), "col_" + (columnIndex + 1)));
        } else {
          headers.put(columnIndex,  "col_" + (columnIndex + 1));
        }
      }
      return headers;
    }, (headers, row) -> {
      Map<String, String> rowMap = Maps.newTreeMap();
      for (Cell cell : row) {
        int columnIndex = cell.getColumnIndex();
        if (headers.containsKey(columnIndex)) {
          rowMap.put(headers.get(columnIndex), cell.getStringCellValue());
        }
      }
      return rowMap;
    }, false);

    // then
    assertThat(excelSheet.getHeaders()).hasSize(5);
    assertThat(excelSheet.getHeaders()).containsValues("col_1", "col_2", "col_3", "col_4", "col_5");

    assertThat(excelSheet.getRows()).hasSize(10);
    assertThat(excelSheet.getRows()).extracting("col_1").contains("time", "20/04/2017","21/04/2017","22/04/2017","23/04/2017","24/04/2017","25/04/2017","26/04/2017","27/04/2017","28/04/2017");
    assertThat(excelSheet.getRows()).extracting("col_2").contains("order_id", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    assertThat(excelSheet.getRows()).extracting("col_3").contains("amount", "20","300","400","550","129","212","412","412","2111");
    assertThat(excelSheet.getRows()).extracting("col_4").contains("product_id", "1","1","2","2","3","3","4","4","5");
    assertThat(excelSheet.getRows()).extracting("col_5").contains("sale_count", "1","2","3","4","1","2","3","4","5");
  }
}