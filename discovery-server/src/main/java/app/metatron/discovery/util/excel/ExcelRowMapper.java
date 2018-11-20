package app.metatron.discovery.util.excel;

import org.apache.poi.ss.usermodel.Row;

public interface ExcelRowMapper<C, T> {
  T mapRow(C headers , Row row);
}
