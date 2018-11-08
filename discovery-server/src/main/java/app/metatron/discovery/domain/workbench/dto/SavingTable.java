package app.metatron.discovery.domain.workbench.dto;

public class SavingTable {
  private String tableName;
  private String webSocketId;
  private String loginUserId;
  private String storedFileId;

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getWebSocketId() {
    return webSocketId;
  }

  public void setWebSocketId(String webSocketId) {
    this.webSocketId = webSocketId;
  }

  public String getLoginUserId() {
    return loginUserId;
  }

  public void setLoginUserId(String loginUserId) {
    this.loginUserId = loginUserId;
  }

  public String getStoredFileId() {
    return storedFileId;
  }

  public void setStoredFileId(String storedFileId) {
    this.storedFileId = storedFileId;
  }
}
