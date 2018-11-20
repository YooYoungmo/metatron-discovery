package app.metatron.discovery.domain.workbench.hive;

import app.metatron.discovery.TestLocalHdfs;
import app.metatron.discovery.domain.datasource.connection.jdbc.HiveConnection;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class DataTableHiveRepositoryTest {

  final String metatronUserId = "testrepouser";

  final TestLocalHdfs testLocalHdfs = new TestLocalHdfs();

  final DataTableHiveRepository dataTableHiveRepository = new DataTableHiveRepository();

  @Before
  public void setUp() throws IOException, InterruptedException {
    testLocalHdfs.delete(new Path(String.format("/tmp/metatron/%s", metatronUserId)));
  }

  @Test
  public void saveToHdfs() throws IOException, InterruptedException {
    // given
    HiveConnection hiveConnection = getHiveConnection();

    List<String> fields = Arrays.asList("records.year", "records.temperature", "records.quality");

    List<Map<String, Object>> records = new ArrayList<>();
    Map<String, Object> record1 = new HashMap<>();
    record1.put("records.year", "1990");
    record1.put("records.temperature", 10);
    record1.put("records.quality", 0);
    records.add(record1);
    Map<String, Object> record2 = new HashMap<>();
    record2.put("records.year", "1991");
    record2.put("records.temperature", 20);
    record2.put("records.quality", 1);
    records.add(record2);

    DataTable dataTable = new DataTable(fields, records);

    // when
    String fileId = dataTableHiveRepository.saveToHdfs(hiveConnection, new Path(String.format("/tmp/metatron/%s", metatronUserId)), dataTable);

    // then
    assertThat(testLocalHdfs.exists(new Path(String.format("/tmp/metatron/%s/%s.json", metatronUserId, fileId))))
        .isTrue();

    assertThat(testLocalHdfs.exists(new Path(String.format("/tmp/metatron/%s/%s.dat", metatronUserId, fileId))))
        .isTrue();
  }

  private HiveConnection getHiveConnection() {
    HiveConnection hiveConnection = new HiveConnection();
    hiveConnection.setHdfsConfigurationPath(Paths.get("src/test/hdfs-conf").toAbsolutePath().toString());
    hiveConnection.setPersonalDatabasePrefix("private");
    hiveConnection.setSecondaryUsername("admin");
    hiveConnection.setSecondaryPassword("111");
    return hiveConnection;
  }

  @Test
  public void findDataTableMetaFromHdfs() throws IOException, InterruptedException {
    // given
    HiveConnection hiveConnection = getHiveConnection();

    Path path = new Path(String.format("/tmp/metatron/%s", metatronUserId));
    String fileId = UUID.randomUUID().toString();

    String headerContents = "[\"records.year\",\"records.temperature\",\"records.quality\"]";
    testLocalHdfs.writeFile(hiveConnection.getSecondaryUsername(), path, String.format("%s.json", fileId), headerContents);

    String dataContents = "1990\00110\0010\n";
    testLocalHdfs.writeFile(hiveConnection.getSecondaryUsername(), path, String.format("%s.dat", fileId), dataContents);

    // when
    DataTableMeta actual = dataTableHiveRepository.findDataTableMetaFromHdfs(hiveConnection, path, fileId);

    // then
    assertThat(actual.getHeaders()).hasSize(3);
    assertThat(actual.getHeaders()).contains("records.year", "records.temperature", "records.quality");
    assertThat(actual.getStoredRecordFilePath()).isEqualTo(new Path(path, String.format("%s.dat", fileId)).toString());
  }

  @Test
  public void deleteHdfsDirectory() throws IOException, InterruptedException {
    // given
    HiveConnection hiveConnection = getHiveConnection();

    Path path = new Path(String.format("/tmp/metatron/%s/%s", metatronUserId, UUID.randomUUID().toString()));
    String fileContents = "aabbcc11";

    testLocalHdfs.writeFile(hiveConnection.getSecondaryUsername(), path, String.format("%s.dat", UUID.randomUUID().toString()), fileContents);

    // when
    dataTableHiveRepository.deleteHdfsDirectory(hiveConnection, path);

    // then
    assertThat(testLocalHdfs.exists(path)).isFalse();
  }
}