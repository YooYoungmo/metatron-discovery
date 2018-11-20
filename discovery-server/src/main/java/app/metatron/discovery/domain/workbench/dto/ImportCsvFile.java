package app.metatron.discovery.domain.workbench.dto;

public class ImportCsvFile extends ImportFile {
  private String lineSep = "\n";
  private String delimiter = ",";

  public String getLineSep() {
    return lineSep;
  }

  public void setLineSep(String lineSep) {
    this.lineSep = lineSep;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  @Override
  public String toString() {
    return "ImportCsvFile{" +
        "lineSep='" + lineSep + '\'' +
        ", delimiter='" + delimiter + '\'' +
        '}';
  }
}
