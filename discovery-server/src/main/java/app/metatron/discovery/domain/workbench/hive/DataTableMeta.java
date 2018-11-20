package app.metatron.discovery.domain.workbench.hive;

import java.util.ArrayList;
import java.util.List;

public class DataTableMeta {
  private List<String> headers = new ArrayList<>();
  private String storedRecordFilePath;

  public DataTableMeta(List<String> headers, String storedRecordFilePath) {
    this.headers = headers;
    this.storedRecordFilePath = storedRecordFilePath;
  }

  public List<String> getHeaders() {
    return headers;
  }

  public String getStoredRecordFilePath() {
    return storedRecordFilePath;
  }
}
