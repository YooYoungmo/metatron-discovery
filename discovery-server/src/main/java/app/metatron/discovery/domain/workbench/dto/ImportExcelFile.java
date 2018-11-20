package app.metatron.discovery.domain.workbench.dto;

public class ImportExcelFile extends ImportFile {
  private String sheetName;

  public String getSheetName() {
    return sheetName;
  }

  public void setSheetName(String sheetName) {
    this.sheetName = sheetName;
  }

  @Override
  public String toString() {
    return "ImportExcelFile{" +
        "sheetName='" + sheetName + '\'' +
        '}';
  }
}
