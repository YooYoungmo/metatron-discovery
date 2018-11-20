package app.metatron.discovery.util.csv;

import java.util.List;

public class CsvData<C, R> {
  private C headers;
  private List<R> rows;

  public CsvData(C headers, List<R> rows) {
    this.headers = headers;
    this.rows = rows;
  }

  public C getHeaders() {
    return headers;
  }

  public List<R> getRows() {
    return rows;
  }
}
