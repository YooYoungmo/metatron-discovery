package app.metatron.discovery.util.csv;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CsvTemplateTest {

  @Test
  public void getData() {
    // given
    final String filePath = getClass().getClassLoader().getResource("sales.csv").getPath();
    final boolean firstHeaderRow = true;

    // when
    CsvTemplate csvTemplate = new CsvTemplate(new File(filePath));
    CsvData<Map<Integer, String>, Map<String, String>> csvData = csvTemplate.getData("\n", ",",
        firstRow -> {
          Map<Integer, String> headers = Maps.newTreeMap();
          for (int i = 0; i < firstRow.length; i++) {
            if (firstHeaderRow) {
              headers.put(i, firstRow[i]);
            } else {
              headers.put(i, "col_" + (i + 1));
            }
          }
          return headers;
        },
        (headers, row) -> {
          Map<String, String> rowMap = Maps.newTreeMap();
          for (int i = 0; i < row.length; i++) {
            if (headers.containsKey(i)) {
              rowMap.put(headers.get(i), row[i]);
            }
          }

          return rowMap;
        }, true);

    // then
    assertThat(csvData.getHeaders()).hasSize(5);
    assertThat(csvData.getHeaders()).containsValues("time", "order_id", "amount", "product_id", "sale_count");

    assertThat(csvData.getRows()).hasSize(9);
    assertThat(csvData.getRows()).extracting("time").contains("20/04/2017", "21/04/2017", "22/04/2017", "23/04/2017", "24/04/2017", "25/04/2017", "26/04/2017", "27/04/2017", "28/04/2017");
    assertThat(csvData.getRows()).extracting("order_id").contains("1", "2", "3", "4", "5", "6", "7", "8", "9");
    assertThat(csvData.getRows()).extracting("amount").contains("20", "300", "400", "550", "129", "212", "412", "412", "2111");
    assertThat(csvData.getRows()).extracting("product_id").contains("1", "1", "2", "2", "3", "3", "4", "4", "5");
    assertThat(csvData.getRows()).extracting("sale_count").contains("1", "2", "3", "4", "1", "2", "3", "4", "5");
  }

  @Test
  public void getData_when_skipFirstRow_false() {

    // given
    final String filePath = getClass().getClassLoader().getResource("sales.csv").getPath();
    final boolean firstHeaderRow = false;

    // when
    CsvTemplate csvTemplate = new CsvTemplate(new File(filePath));
    CsvData<Map<Integer, String>, Map<String, String>> csvData = csvTemplate.getData("\n", ",",
        firstRow -> {
          Map<Integer, String> headers = Maps.newTreeMap();
          for (int i = 0; i < firstRow.length; i++) {
            if (firstHeaderRow) {
              headers.put(i, firstRow[i]);
            } else {
              headers.put(i, "col_" + (i + 1));
            }
          }
          return headers;
        },
        (headers, row) -> {
          Map<String, String> rowMap = Maps.newTreeMap();
          for (int i = 0; i < row.length; i++) {
            if (headers.containsKey(i)) {
              rowMap.put(headers.get(i), row[i]);
            }
          }

          return rowMap;
        }, false);

    // then
    assertThat(csvData.getHeaders()).hasSize(5);
    assertThat(csvData.getHeaders()).containsValues("col_1", "col_2", "col_3", "col_4", "col_5");

    assertThat(csvData.getRows()).hasSize(10);
    assertThat(csvData.getRows()).extracting("col_1").contains("time", "20/04/2017", "21/04/2017", "22/04/2017", "23/04/2017", "24/04/2017", "25/04/2017", "26/04/2017", "27/04/2017", "28/04/2017");
    assertThat(csvData.getRows()).extracting("col_2").contains("order_id", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    assertThat(csvData.getRows()).extracting("col_3").contains("amount", "20", "300", "400", "550", "129", "212", "412", "412", "2111");
    assertThat(csvData.getRows()).extracting("col_4").contains("product_id", "1", "1", "2", "2", "3", "3", "4", "4", "5");
    assertThat(csvData.getRows()).extracting("col_5").contains("sale_count", "1", "2", "3", "4", "1", "2", "3", "4", "5");
  }
}