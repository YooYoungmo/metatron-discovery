package app.metatron.discovery.domain.workbench.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ImportExcelFile.class, name = "excel"),
    @JsonSubTypes.Type(value = ImportCsvFile.class, name = "csv"),
})
public abstract class ImportFile {
  private Boolean firstRowHeadColumnUsed = false;
  private String tableName;
  private String uploadedFilePath;
  private String loginUserId;
  private String webSocketId;

  public String getUploadedFilePath() {
    return uploadedFilePath;
  }

  public void setUploadedFilePath(String uploadedFilePath) {
    this.uploadedFilePath = uploadedFilePath;
  }

  public String getLoginUserId() {
    return loginUserId;
  }

  public void setLoginUserId(String loginUserId) {
    this.loginUserId = loginUserId;
  }

  public String getWebSocketId() {
    return webSocketId;
  }

  public void setWebSocketId(String webSocketId) {
    this.webSocketId = webSocketId;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public Boolean getFirstRowHeadColumnUsed() {
    return firstRowHeadColumnUsed;
  }

  public void setFirstRowHeadColumnUsed(Boolean firstRowHeadColumnUsed) {
    this.firstRowHeadColumnUsed = firstRowHeadColumnUsed;
  }
}
