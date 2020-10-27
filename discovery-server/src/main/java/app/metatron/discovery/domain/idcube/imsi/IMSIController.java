package app.metatron.discovery.domain.idcube.imsi;

import app.metatron.discovery.common.exception.MetatronException;
import app.metatron.discovery.common.exception.ResourceNotFoundException;
import app.metatron.discovery.domain.idcube.IdCubeProperties;
import app.metatron.discovery.domain.idcube.imsi.dto.CipherRequest;
import app.metatron.discovery.domain.idcube.imsi.entity.DataDownloadHistory;
import app.metatron.discovery.domain.idcube.imsi.entity.IdentityVerification;
import app.metatron.discovery.domain.idcube.imsi.repository.DataDownloadHistoryRepository;
import app.metatron.discovery.domain.idcube.imsi.repository.IdentityVerificationRepository;
import app.metatron.discovery.domain.user.User;
import app.metatron.discovery.domain.user.UserRepository;
import app.metatron.discovery.domain.workbench.QueryEditor;
import app.metatron.discovery.domain.workbench.QueryEditorRepository;
import app.metatron.discovery.domain.workbench.QueryEditorResult;
import app.metatron.discovery.domain.workbench.WorkbenchProperties;
import app.metatron.discovery.domain.workbench.dto.ImportCsvFile;
import app.metatron.discovery.domain.workbench.dto.ImportFile;
import app.metatron.discovery.domain.workbench.hive.DataTable;
import app.metatron.discovery.domain.workbench.hive.DataTableHiveRepository;
import app.metatron.discovery.domain.workbench.hive.HivePersonalDatasource;
import app.metatron.discovery.domain.workbench.hive.WorkbenchHiveService;
import app.metatron.discovery.util.AuthUtils;
import app.metatron.discovery.util.HttpUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/idcube/imsi")
public class IMSIController {
  private static Logger LOGGER = LoggerFactory.getLogger(IMSIController.class);

  private UserRepository userRepository;

  private IdentityVerificationRepository identityVerificationRepository;

  private QueryEditorRepository queryEditorRepository;

  private DataDownloadHistoryRepository dataDownloadHistoryRepository;

  private WorkbenchHiveService workbenchHiveService;

  private DataTableHiveRepository dataTableHiveRepository;

  @Autowired
  public void setWorkbenchHiveService(WorkbenchHiveService workbenchHiveService) {
    this.workbenchHiveService = workbenchHiveService;
  }

  @Autowired
  private IdCubeProperties idCubeProperties;

  @Autowired
  public void setIdentityVerificationRepository(IdentityVerificationRepository identityVerificationRepository) {
    this.identityVerificationRepository = identityVerificationRepository;
  }

  @Autowired
  public void setUserRepository(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Autowired
  private WorkbenchProperties workbenchProperties;

  @Autowired
  public void setQueryEditorRepository(QueryEditorRepository queryEditorRepository) {
    this.queryEditorRepository = queryEditorRepository;
  }

  @Autowired
  public void setDataDownloadHistoryRepository(DataDownloadHistoryRepository dataDownloadHistoryRepository) {
    this.dataDownloadHistoryRepository = dataDownloadHistoryRepository;
  }

  @Autowired
  public void setDataTableHiveRepository(DataTableHiveRepository dataTableHiveRepository) {
    this.dataTableHiveRepository = dataTableHiveRepository;
  }

  @PostMapping(value = "/identity-verification")
  public ResponseEntity<?> doIdentityVerification() {
    User user = userRepository.findByUsername(AuthUtils.getAuthUserName());
    IdentityVerification identityVerification = new IdentityVerification(user.getId(), user.getTel(), user.getFullName());

    identityVerification.sendAuthNumberWithSMS();
    // 전화번호 저장시에는 암호화 한다.
    identityVerification.setReceiverTelNo(user.getEncryptedTel());
    identityVerificationRepository.save(identityVerification);

    Map<String, Object> result = new HashMap<>();
    result.put("identityVerificationId", identityVerification.getId());
    return ResponseEntity.ok(result);
  }

  @GetMapping(value = "/identity-verification")
  public ResponseEntity<?> checkIdentityVerificationByAuthenticationNumber(@RequestParam(value = "identityVerificationId") Long identityVerificationId,
                                                @RequestParam(value = "receivedAuthenticationNumber") String receivedAuthenticationNumber) {
    IdentityVerification identityVerification = identityVerificationRepository.findOne(identityVerificationId);
    boolean verified = identityVerification.verifyAuthNumber(receivedAuthenticationNumber);
    identityVerificationRepository.save(identityVerification);

    Map<String, Object> result = new HashMap<>();
    result.put("verified", verified);
    return ResponseEntity.ok(result);
  }

  @PostMapping(value = "/identity-verification/purpose-of-use")
  public ResponseEntity<?> addPurposeOfUse(@RequestBody Map<String, Object> requestBody) {
    final Long identityVerificationId = Long.valueOf((Integer)requestBody.get("identityVerificationId"));
    final String purposeOfUse = (String)requestBody.get("purposeOfUse");
    final String details = (String)requestBody.get("details");

    IdentityVerification identityVerification = identityVerificationRepository.findOne(identityVerificationId);
    identityVerification.setPurposeOfUse(purposeOfUse, details);
    identityVerificationRepository.save(identityVerification);

    return ResponseEntity.ok(null);
  }

  @PostMapping(value = "/encryption-or-decryption")
  public ResponseEntity<?> encryptOrDecrypt(@RequestBody CipherRequest cipherRequest) {
    IdentityVerification identityVerification = identityVerificationRepository.findOne(cipherRequest.getIdentityVerificationId());
    if(identityVerification.getVerified() == false) {
      throw new MetatronException("identity verification error");
    }

    String csvBaseDir = workbenchProperties.getTempCSVPath();
    if(!csvBaseDir.endsWith(File.separator)){
      csvBaseDir = csvBaseDir + File.separator;
    }

    ImsiCipher cipher = new ImsiCipher(cipherRequest.getCipherType(), csvBaseDir, cipherRequest.getCsvFile(),
        cipherRequest.getFields(), cipherRequest.getCipherFieldName());
    List<Map<String, Object>> data = cipher.encryptOrDecrypt();
    String csvFileName = cipher.writeToCSV(data);

    Long dataDownloadHistoryId = null;
    try {
      dataDownloadHistoryId = logDataDownloadHistory(cipherRequest.getQueryEditorId(), cipherRequest.getCsvFile(),
          cipherRequest.getCipherType(), cipherRequest.getCipherFieldName());
    } catch (Exception e) {
      LOGGER.error("error Logging workspace audit logs for data downloads from workbenches and workbook", e);
    }

    Map<String, Object> result = new HashMap<>();
    result.put("fields", cipher.getCipherFields());
    result.put("data", data);
    result.put("csvFileName", csvFileName);
    result.put("dataDownloadHistoryId", dataDownloadHistoryId);

    return ResponseEntity.ok(result);
  }

  @PostMapping(path = "/encryption-or-decryption/download")
  public void download(@RequestBody Map<String, Object> requestBody, HttpServletResponse response) throws IOException {
    final String transformFileName = (String)requestBody.get("transformFileName");
    final Long dataDownloadHistoryId = Long.valueOf((Integer)requestBody.get("dataDownloadHistoryId"));

    String csvBaseDir = workbenchProperties.getTempCSVPath();
    if(!csvBaseDir.endsWith(File.separator)){
      csvBaseDir = csvBaseDir + File.separator;
    }

    final String csvFilePath = csvBaseDir + transformFileName;
    HttpUtils.downloadCSVFile(response, transformFileName, csvFilePath, "text/csv; charset=utf-8");

    try {
      DataDownloadHistory findDataDownloadHistory = dataDownloadHistoryRepository.findOne(dataDownloadHistoryId);
      findDataDownloadHistory.setDownloaded(true);

      try {
        String savedHdfsDataFilePath = saveToHdfs(transformFileName);
        findDataDownloadHistory.setDownloadedBackupHdfsFilePath(savedHdfsDataFilePath);
      } catch (Exception e) {
        LOGGER.error("error Logging hdfs file path", e);
      }

      dataDownloadHistoryRepository.save(findDataDownloadHistory);
    } catch (Exception e) {
      LOGGER.error("error Logging workspace audit logs for data downloads from workbenches and workbook", e);
    }
  }

  private String saveToHdfs(String file) {
    if(idCubeProperties != null) {
      final String hdfsConfPath = idCubeProperties.getImsi().getDownloadHistoryBackupHdfs().getHdfsConfPath();
      final String user = idCubeProperties.getImsi().getDownloadHistoryBackupHdfs().getUser();
      final String backupPath = idCubeProperties.getImsi().getDownloadHistoryBackupHdfs().getBackupPath();

      if(StringUtils.isNotEmpty(hdfsConfPath) && StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(backupPath)) {
        try {
          ImportFile importFile = new ImportCsvFile();
          importFile.setUploadedFile(file);
          DataTable dataTable = workbenchHiveService.convertUploadFileToDataTable(importFile);
          HivePersonalDatasource datasource = new HivePersonalDatasource(hdfsConfPath, user, "", "");

          String path = backupPath.endsWith(File.separator) ?
              backupPath + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) :
              backupPath + File.separator + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

          return dataTableHiveRepository.saveToHdfs(datasource, new Path(path), dataTable, "");
        } catch (Exception e) {
          throw new RuntimeException("error save hdfs", e);
        }
      } else {
        throw new RuntimeException("error id-cube property hdfs-conf-path or user, backup-path are empty");
      }
    } else {
      throw new RuntimeException("error id-cube property are empty");
    }
  }

  private Long logDataDownloadHistory(String queryEditorId, String csvFilePath, String cryptoType, String cryptoFieldName) {
    QueryEditor queryEditor = queryEditorRepository.findOne(queryEditorId);
    if(queryEditor == null){
      throw new ResourceNotFoundException("QueryEditor(" + queryEditorId + ")");
    }

    Optional<QueryEditorResult> queryResult = queryEditor.getQueryResults().stream()
        .filter(result -> result.getFilePath().equalsIgnoreCase(csvFilePath))
        .findFirst();

    if(queryResult.isPresent()) {
      DataDownloadHistory dataDownloadHistory =
          new DataDownloadHistory(queryEditor.getWorkbench().getId(), queryResult.get().getQuery(), cryptoFieldName, cryptoType);
      dataDownloadHistoryRepository.save(dataDownloadHistory);
      return dataDownloadHistory.getId();
    } else {
      throw new MetatronException("Not found queryResult.");
    }
  }

  @GetMapping(value = "/max-result-size")
  public ResponseEntity<?>  getMaxResultSize() {
    int maxResultSize = idCubeProperties != null ? idCubeProperties.getImsi().getMaxResultSize() : 0;

    Map<String, Object> result = new HashMap<>();
    result.put("maxResultSize", maxResultSize);
    return ResponseEntity.ok(result);
  }
}