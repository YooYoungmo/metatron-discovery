package app.metatron.discovery.domain.workbench.hive;

import app.metatron.discovery.common.exception.BadRequestException;
import app.metatron.discovery.common.exception.GlobalErrorCodes;
import app.metatron.discovery.common.exception.MetatronException;
import app.metatron.discovery.domain.datasource.connection.jdbc.HiveConnection;
import app.metatron.discovery.domain.datasource.connection.jdbc.JdbcConnectionService;
import app.metatron.discovery.domain.workbench.WorkbenchErrorCodes;
import app.metatron.discovery.domain.workbench.WorkbenchException;
import app.metatron.discovery.domain.workbench.WorkbenchProperties;
import app.metatron.discovery.domain.workbench.dto.ImportCsvFile;
import app.metatron.discovery.domain.workbench.dto.ImportExcelFile;
import app.metatron.discovery.domain.workbench.dto.ImportFile;
import app.metatron.discovery.domain.workbench.dto.SavingTable;
import app.metatron.discovery.domain.workbench.util.WorkbenchDataSource;
import app.metatron.discovery.domain.workbench.util.WorkbenchDataSourceUtils;
import app.metatron.discovery.util.csv.CsvData;
import app.metatron.discovery.util.csv.CsvTemplate;
import app.metatron.discovery.util.excel.ExcelSheet;
import app.metatron.discovery.util.excel.ExcelTemplate;
import com.google.common.collect.Maps;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.poi.ss.usermodel.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkbenchHiveService {
  private static Logger LOGGER = LoggerFactory.getLogger(WorkbenchHiveService.class);

  private WorkbenchProperties workbenchProperties;

  private DataTableHiveRepository dataTableHiveRepository;

  private JdbcConnectionService jdbcConnectionService;

  @Autowired
  public void setWorkbenchProperties(WorkbenchProperties workbenchProperties) {
    this.workbenchProperties = workbenchProperties;
  }

  @Autowired
  public void setJdbcConnectionService(JdbcConnectionService jdbcConnectionService) {
    this.jdbcConnectionService = jdbcConnectionService;
  }

  @Autowired
  public void setDataTableHiveRepository(DataTableHiveRepository dataTableHiveRepository) {
    this.dataTableHiveRepository = dataTableHiveRepository;
  }

  public String saveDataTableToHdfs(HiveConnection hiveConnection, String metatronUserId, DataTable dataTable, String additionalPath) {
    Path path = makeMetatronUserHomePath(metatronUserId);
    if(StringUtils.isNotEmpty(additionalPath)) {
      path = new Path(path, additionalPath);
    }
    return dataTableHiveRepository.saveToHdfs(hiveConnection, path, dataTable);
  }

  private Path makeMetatronUserHomePath(String metatronUserId) {
    return new Path(String.format("%s/%s/", workbenchProperties.getTempDataTableHdfsPath(),
        HiveNamingRule.replaceNotAllowedCharacters(metatronUserId)));
  }

  public void saveAsHiveTableFromHdfsDataTable(HiveConnection hiveConnection, SavingTable savingTable, String additionalPath) {
    String saveAsTableScript = generateSaveAsTableScript(hiveConnection, savingTable.getLoginUserId(), savingTable.getStoredFileId(), savingTable.getTableName(), additionalPath);
    WorkbenchDataSource dataSourceInfo = WorkbenchDataSourceUtils.findDataSourceInfo(savingTable.getWebSocketId());
    SingleConnectionDataSource secondaryDataSource = dataSourceInfo.getSecondarySingleConnectionDataSource();

    List<String> queryList = Arrays.asList(saveAsTableScript.split(";"));
    for (String query : queryList) {
      try {
        jdbcConnectionService.ddlQuery(hiveConnection, secondaryDataSource, query);
      } catch(Exception e) {
        LOGGER.error("Failed save as hive table", e);
        if(e.getMessage().indexOf("AlreadyExistsException") > -1) {
          throw new WorkbenchException(WorkbenchErrorCodes.TABLE_ALREADY_EXISTS, "Table already exists.");
        }
        throw new MetatronException(GlobalErrorCodes.DEFAULT_GLOBAL_ERROR_CODE, "Failed save as hive table.");
      }
    }
  }

  private String generateSaveAsTableScript(HiveConnection hiveConnection, String metatronUserId, String dataTableId, String tableName, String additionalPath) {
    StringBuffer script = new StringBuffer();
    // 1. Create Database
    final String personalDatabaseName = String.format("%s_%s", hiveConnection.getPersonalDatabasePrefix(),
        HiveNamingRule.replaceNotAllowedCharacters(metatronUserId));
    script.append(String.format("CREATE DATABASE IF NOT EXISTS %s;", personalDatabaseName));

    // 2. Create Table
    Path path = makeMetatronUserHomePath(metatronUserId);
    if(StringUtils.isNotEmpty(additionalPath)) {
      path = new Path(path, additionalPath);
    }

    DataTableMeta dataTableMeta = dataTableHiveRepository.findDataTableMetaFromHdfs(hiveConnection, path, dataTableId);

    List<String> headers = dataTableMeta.getHeaders();
    String storedRecordFilePath = dataTableMeta.getStoredRecordFilePath();

    String columns = headers.stream().map(header -> {
      if(header.contains(".")) {
        return String.format("`%s`", header.substring(header.indexOf(".") + 1, header.length()));
      } else {
        return String.format("`%s`", header);
      }
    }).collect(Collectors.joining(" STRING, ", "", " STRING"));
    script.append(String.format("CREATE TABLE %s.%s (%s) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\001' LINES TERMINATED BY '\\n';",
        personalDatabaseName, tableName, columns));

    // 3. Load Data to Table
    script.append(String.format("LOAD DATA INPATH '%s' OVERWRITE INTO TABLE %s.%s;", storedRecordFilePath, personalDatabaseName, tableName));

    LOGGER.info("Save as Hive Table Query : " + script.toString());
    return script.toString();
  }

  public void importFileToPersonalDatabase(HiveConnection hiveConnection, ImportFile importFile) throws IOException {
    DataTable dataTable = convertUploadFileToDataTable(importFile);
    String storedFileId = this.saveDataTableToHdfs(hiveConnection, importFile.getLoginUserId(), dataTable, null);

    SavingTable savingTable = new SavingTable();
    savingTable.setTableName(importFile.getTableName());
    savingTable.setWebSocketId(importFile.getWebSocketId());
    savingTable.setLoginUserId(importFile.getLoginUserId());
    savingTable.setStoredFileId(storedFileId);
    this.saveAsHiveTableFromHdfsDataTable(hiveConnection, savingTable, null);
  }

  private DataTable convertUploadFileToDataTable(ImportFile importFile) throws IOException {
    // TODO 파일 예외 처리... 존재 여부 기타 등등...
    File uploadedFile = new File(importFile.getUploadedFilePath());
    if(importFile instanceof ImportExcelFile) {
      return convertExcelFileToDataTable(uploadedFile, ((ImportExcelFile)importFile).getSheetName(), importFile.getFirstRowHeadColumnUsed());
    } else if(importFile instanceof ImportCsvFile){
      return convertCsvFileToDataTable(uploadedFile, importFile.getFirstRowHeadColumnUsed(),
          ((ImportCsvFile)importFile).getDelimiter(), ((ImportCsvFile)importFile).getLineSep());
    } else {
      throw new BadRequestException("Invalid temporary file.");
    }
  }

  private DataTable convertCsvFileToDataTable(File uploadedFile, Boolean firstRowHeadColumnUsed, String delimiter, String lineSep) {
    CsvTemplate csvTemplate = new CsvTemplate(uploadedFile);
    CsvData<Map<Integer, String>, Map<String, Object>> csvData = csvTemplate.getData(lineSep, delimiter,
        firstRow -> {
          Map<Integer, String> headers = Maps.newTreeMap();
          for (int i = 0; i < firstRow.length; i++) {
            if (firstRowHeadColumnUsed) {
              headers.put(i, firstRow[i]);
            } else {
              headers.put(i, "col_" + (i + 1));
            }
          }
          return headers;
        },
        (headers, row) -> {
          Map<String, Object> rowMap = Maps.newTreeMap();
          for (int i = 0; i < row.length; i++) {
            if (headers.containsKey(i)) {
              rowMap.put(headers.get(i), row[i]);
            }
          }

          return rowMap;
        }, firstRowHeadColumnUsed);

    List<String> fields = csvData.getHeaders().values().stream().collect(Collectors.toList());
    return new DataTable(fields, csvData.getRows());
  }

  private DataTable convertExcelFileToDataTable(File uploadedFile, String sheetName, Boolean firstRowHeadColumnUsed) throws IOException {
    ExcelTemplate excelTemplate = new ExcelTemplate(uploadedFile);
    ExcelSheet<Map<Integer, String>, Map<String, Object>> excelSheet = excelTemplate.getSheet(sheetName, firstRow -> {
      Map<Integer, String> headers = Maps.newTreeMap();
      for (Cell cell : firstRow) {
        int columnIndex = cell.getColumnIndex();
        if(firstRowHeadColumnUsed) {
          headers.put(columnIndex, StringUtils.defaultString(cell.getStringCellValue(), "col_" + (columnIndex + 1)));
        } else {
          headers.put(columnIndex,  "col_" + (columnIndex + 1));
        }
      }
      return headers;
    }, (headers, row) -> {
      Map<String, Object> rowMap = Maps.newTreeMap();
      for (Cell cell : row) {
        int columnIndex = cell.getColumnIndex();
        if (headers.containsKey(columnIndex)) {
          rowMap.put(headers.get(columnIndex), cell.getStringCellValue());
        }
      }
      return rowMap;
    }, firstRowHeadColumnUsed);
    List<String> fields = excelSheet.getHeaders().values().stream().collect(Collectors.toList());
    return new DataTable(fields, excelSheet.getRows());
  }

  public void deleteHdfsDirectory(HiveConnection hiveConnection, String metatronUserId, String additionalPath) {
    Path path = new Path(makeMetatronUserHomePath(metatronUserId), additionalPath);
    dataTableHiveRepository.deleteHdfsDirectory(hiveConnection, path);
  }
}
