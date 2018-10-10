package app.metatron.discovery.domain.workbook.dto;

public class ChangingWorkbookDataSource {
  private String fromDataSourceId;
  private String toDataSourceId;

  public String getFromDataSourceId() {
    return fromDataSourceId;
  }

  public void setFromDataSourceId(String fromDataSourceId) {
    this.fromDataSourceId = fromDataSourceId;
  }

  public String getToDataSourceId() {
    return toDataSourceId;
  }

  public void setToDataSourceId(String toDataSourceId) {
    this.toDataSourceId = toDataSourceId;
  }

  @Override
  public String toString() {
    return "ChangingWorkbookDataSource{" +
        "fromDataSourceId='" + fromDataSourceId + '\'' +
        ", toDataSourceId='" + toDataSourceId + '\'' +
        '}';
  }
}
