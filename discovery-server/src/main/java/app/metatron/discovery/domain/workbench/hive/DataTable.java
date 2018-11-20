package app.metatron.discovery.domain.workbench.hive;

import java.util.List;
import java.util.Map;

public class DataTable {
  private List<String> fields;
  private List<Map<String, Object>> records;

  public DataTable(List<String> fields, List<Map<String, Object>> records) {
    this.fields = fields;
    this.records = records;
  }

  public List<String> getFields() {
    return fields;
  }

  public List<Map<String, Object>> getRecords() {
    return records;
  }

  @Override
  public String toString() {
    return "DataTable{" +
        "fields=" + fields +
        ", records=" + records +
        '}';
  }
}
