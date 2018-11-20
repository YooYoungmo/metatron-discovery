package app.metatron.discovery.domain.workbench.hive;

import app.metatron.discovery.domain.datasource.connection.jdbc.HiveConnection;
import app.metatron.discovery.domain.datasource.connection.jdbc.JdbcConnectionService;
import app.metatron.discovery.domain.workbench.dto.ImportCsvFile;
import app.metatron.discovery.domain.workbench.dto.ImportExcelFile;
import app.metatron.discovery.domain.workbench.util.WorkbenchDataSourceUtils;
import org.apache.hadoop.fs.Path;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

public class WorkbenchHiveServiceTest {

  @Test
  public void importFileToPersonalDatabase_excel() throws IOException {
    // given
    final String webSocketId = "test-ws";
    final String metatronUserId = "polaris";
    HiveConnection hiveConnection = getHiveConnection();
    WorkbenchDataSourceUtils.createDataSourceInfo(hiveConnection, webSocketId, hiveConnection.getUsername(), hiveConnection.getPassword(), false);

    WorkbenchHiveService workbenchHiveService = new WorkbenchHiveService();

    DataTableHiveRepository mockDataTableHiveRepository = mock(DataTableHiveRepository.class);
    final String dataTableId = UUID.randomUUID().toString();
    when(mockDataTableHiveRepository.saveToHdfs(eq(hiveConnection), anyObject(), anyObject())).thenReturn(dataTableId);
    final String storedRecordFilePath = String.format("/tmp/metatron/%s/test.dat", metatronUserId);
    DataTableMeta dataTableMeta = new DataTableMeta(Arrays.asList("time", "order_id", "amount", "product_id", "sale_count"), storedRecordFilePath);
    when(mockDataTableHiveRepository.findDataTableMetaFromHdfs(eq(hiveConnection), anyObject(), eq(dataTableId))).thenReturn(dataTableMeta);
    workbenchHiveService.setDataTableHiveRepository(mockDataTableHiveRepository);

    JdbcConnectionService mockJdbcConnectionService = mock(JdbcConnectionService.class);
    workbenchHiveService.setJdbcConnectionService(mockJdbcConnectionService);

    // when
    ImportExcelFile importFile = new ImportExcelFile();
    importFile.setLoginUserId(metatronUserId);
    importFile.setUploadedFilePath(getClass().getClassLoader().getResource("sales-product.xlsx").getPath());
    importFile.setWebSocketId(webSocketId);
    importFile.setSheetName("sales");
    importFile.setTableName("sales_2018");
    importFile.setFirstRowHeadColumnUsed(true);

    workbenchHiveService.importFileToPersonalDatabase(hiveConnection, importFile);

    // then
    // saveToHdfs
    ArgumentCaptor<Path> argPath = ArgumentCaptor.forClass(Path.class);
    ArgumentCaptor<DataTable> argDataTable = ArgumentCaptor.forClass(DataTable.class);
    verify(mockDataTableHiveRepository).saveToHdfs(eq(hiveConnection), argPath.capture(), argDataTable.capture());
    assertThat(argPath.getValue().toString()).isEqualTo("/tmp/metatron/" + metatronUserId);
    assertThat(argDataTable.getValue().getFields()).hasSize(5);
    assertThat(argDataTable.getValue().getFields()).contains("time", "order_id", "amount", "product_id", "sale_count");
    assertThat(argDataTable.getValue().getRecords()).hasSize(9);
    assertThat(argDataTable.getValue().getRecords()).extracting("time").contains("20/04/2017","21/04/2017","22/04/2017","23/04/2017","24/04/2017","25/04/2017","26/04/2017","27/04/2017","28/04/2017");
    assertThat(argDataTable.getValue().getRecords()).extracting("order_id").contains("1", "2", "3", "4", "5", "6", "7", "8", "9");
    assertThat(argDataTable.getValue().getRecords()).extracting("amount").contains("20","300","400","550","129","212","412","412","2111");
    assertThat(argDataTable.getValue().getRecords()).extracting("product_id").contains("1","1","2","2","3","3","4","4","5");
    assertThat(argDataTable.getValue().getRecords()).extracting("sale_count").contains("1","2","3","4","1","2","3","4","5");

    // saveAsHiveTable
    ArgumentCaptor<String> queries = ArgumentCaptor.forClass(String.class);
    verify(mockJdbcConnectionService, times(3)).ddlQuery(eq(hiveConnection),
        eq(WorkbenchDataSourceUtils.findDataSourceInfo(webSocketId).getSecondarySingleConnectionDataSource()),
        queries.capture());
    assertThat(queries.getAllValues()).hasSize(3);
    assertThat(queries.getAllValues().get(0))
        .isEqualTo(String.format("CREATE DATABASE IF NOT EXISTS %s_%s", hiveConnection.getPersonalDatabasePrefix(), metatronUserId));
    assertThat(queries.getAllValues().get(1))
        .isEqualTo(
            String.format("CREATE TABLE %s_%s.%s (`time` STRING, `order_id` STRING, `amount` STRING, `product_id` STRING, `sale_count` STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\001' LINES TERMINATED BY '\\n'",
                hiveConnection.getPersonalDatabasePrefix(), metatronUserId, importFile.getTableName()));
    assertThat(queries.getAllValues().get(2))
        .isEqualTo(String.format("LOAD DATA INPATH '%s' OVERWRITE INTO TABLE %s_%s.%s",
            storedRecordFilePath, hiveConnection.getPersonalDatabasePrefix(), metatronUserId, importFile.getTableName()));
  }

  @Test
  public void importFileToPersonalDatabase_csv() throws IOException {
    // given
    final String webSocketId = "test-ws";
    final String metatronUserId = "polaris";
    HiveConnection hiveConnection = getHiveConnection();
    WorkbenchDataSourceUtils.createDataSourceInfo(hiveConnection, webSocketId, hiveConnection.getUsername(), hiveConnection.getPassword(), false);

    WorkbenchHiveService workbenchHiveService = new WorkbenchHiveService();

    DataTableHiveRepository mockDataTableHiveRepository = mock(DataTableHiveRepository.class);
    final String dataTableId = UUID.randomUUID().toString();
    when(mockDataTableHiveRepository.saveToHdfs(eq(hiveConnection), anyObject(), anyObject())).thenReturn(dataTableId);
    final String storedRecordFilePath = String.format("/tmp/metatron/%s/test.dat", metatronUserId);
    DataTableMeta dataTableMeta = new DataTableMeta(Arrays.asList("time", "order_id", "amount", "product_id", "sale_count"), storedRecordFilePath);
    when(mockDataTableHiveRepository.findDataTableMetaFromHdfs(eq(hiveConnection), anyObject(), eq(dataTableId))).thenReturn(dataTableMeta);
    workbenchHiveService.setDataTableHiveRepository(mockDataTableHiveRepository);

    JdbcConnectionService mockJdbcConnectionService = mock(JdbcConnectionService.class);
    workbenchHiveService.setJdbcConnectionService(mockJdbcConnectionService);

    // when
    ImportCsvFile importFile = new ImportCsvFile();
    importFile.setLoginUserId(metatronUserId);
    importFile.setUploadedFilePath(getClass().getClassLoader().getResource("sales.csv").getPath());
    importFile.setWebSocketId(webSocketId);
    importFile.setTableName("sales_2018");
    importFile.setFirstRowHeadColumnUsed(true);
    importFile.setLineSep("\n");
    importFile.setDelimiter(",");

    workbenchHiveService.importFileToPersonalDatabase(hiveConnection, importFile);

    // then
    // saveToHdfs
    ArgumentCaptor<Path> argPath = ArgumentCaptor.forClass(Path.class);
    ArgumentCaptor<DataTable> argDataTable = ArgumentCaptor.forClass(DataTable.class);
    verify(mockDataTableHiveRepository).saveToHdfs(eq(hiveConnection), argPath.capture(), argDataTable.capture());
    assertThat(argPath.getValue().toString()).isEqualTo("/tmp/metatron/" + metatronUserId);
    assertThat(argDataTable.getValue().getFields()).hasSize(5);
    assertThat(argDataTable.getValue().getFields()).contains("time", "order_id", "amount", "product_id", "sale_count");
    assertThat(argDataTable.getValue().getRecords()).hasSize(9);
    assertThat(argDataTable.getValue().getRecords()).extracting("time").contains("20/04/2017","21/04/2017","22/04/2017","23/04/2017","24/04/2017","25/04/2017","26/04/2017","27/04/2017","28/04/2017");
    assertThat(argDataTable.getValue().getRecords()).extracting("order_id").contains("1", "2", "3", "4", "5", "6", "7", "8", "9");
    assertThat(argDataTable.getValue().getRecords()).extracting("amount").contains("20","300","400","550","129","212","412","412","2111");
    assertThat(argDataTable.getValue().getRecords()).extracting("product_id").contains("1","1","2","2","3","3","4","4","5");
    assertThat(argDataTable.getValue().getRecords()).extracting("sale_count").contains("1","2","3","4","1","2","3","4","5");

    // saveAsHiveTable
    ArgumentCaptor<String> queries = ArgumentCaptor.forClass(String.class);
    verify(mockJdbcConnectionService, times(3)).ddlQuery(eq(hiveConnection),
        eq(WorkbenchDataSourceUtils.findDataSourceInfo(webSocketId).getSecondarySingleConnectionDataSource()),
        queries.capture());
    assertThat(queries.getAllValues()).hasSize(3);
    assertThat(queries.getAllValues().get(0))
        .isEqualTo(String.format("CREATE DATABASE IF NOT EXISTS %s_%s", hiveConnection.getPersonalDatabasePrefix(), metatronUserId));
    assertThat(queries.getAllValues().get(1))
        .isEqualTo(
            String.format("CREATE TABLE %s_%s.%s (`time` STRING, `order_id` STRING, `amount` STRING, `product_id` STRING, `sale_count` STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\001' LINES TERMINATED BY '\\n'",
                hiveConnection.getPersonalDatabasePrefix(), metatronUserId, importFile.getTableName()));
    assertThat(queries.getAllValues().get(2))
        .isEqualTo(String.format("LOAD DATA INPATH '%s' OVERWRITE INTO TABLE %s_%s.%s",
            storedRecordFilePath, hiveConnection.getPersonalDatabasePrefix(), metatronUserId, importFile.getTableName()));
  }

  private HiveConnection getHiveConnection() {
    HiveConnection hiveConnection = new HiveConnection();
    hiveConnection.setHdfsConfigurationPath(Paths.get("src/test/hdfs-conf").toAbsolutePath().toString());
    hiveConnection.setPersonalDatabasePrefix("private");
    hiveConnection.setSecondaryUsername("admin");
    hiveConnection.setSecondaryPassword("111");
    return hiveConnection;
  }

}