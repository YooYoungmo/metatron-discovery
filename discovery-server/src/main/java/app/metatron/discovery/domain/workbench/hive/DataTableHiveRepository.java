package app.metatron.discovery.domain.workbench.hive;

import app.metatron.discovery.common.GlobalObjectMapper;
import app.metatron.discovery.common.exception.MetatronException;
import app.metatron.discovery.domain.datasource.connection.jdbc.HiveConnection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.OutputStream;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DataTableHiveRepository {

  public String saveToHdfs(HiveConnection hiveConnection, Path path, DataTable dataTable) {
    final String hiveAdminUser = hiveConnection.getSecondaryUsername();
    FileSystem fs = null;
    try {
      fs = getFileSystem(hiveConnection.getHdfsConfigurationPath(), hiveAdminUser);
      if (!fs.exists(path)) {
        try {
          fs.mkdirs(path);
        } catch(IOException e) {
          String errorMessage = String.format("failed make user query result directory to HDFS : %s", path.toString());
          throw new MetatronException(errorMessage, e);
        }
      }

      final String fileId = UUID.randomUUID().toString();
      Path headerFile = new Path(path, String.format("%s.json", fileId));
      try(OutputStream out = fs.create(headerFile)) {
        out.write(GlobalObjectMapper.getDefaultMapper().writeValueAsString(dataTable.getFields()).getBytes());
      } catch(Exception e) {
        String errorMessage = String.format("failed write file to HDFS : %s", headerFile.toString());
        throw new MetatronException(errorMessage, e);
      }

      Path recordsFile = new Path(path, String.format("%s.dat", fileId));
      try(OutputStream out = fs.create(recordsFile)) {
        for (Map<String, Object> record : dataTable.getRecords()) {
          String newRecord = dataTable.getFields().stream()
              .map(field -> Optional.ofNullable(String.valueOf(record.get(field))).orElse(""))
              .collect(Collectors.joining("\001")) +  "\n";

          out.write(newRecord.getBytes());
        }
      } catch(Exception e) {
        String errorMessage = String.format("failed write file to HDFS : %s", recordsFile.toString());
        throw new MetatronException(errorMessage, e);
      }

      return fileId;
    } catch (Exception e) {
      String errorMessage = "failed write file to HDFS";
      throw new MetatronException(errorMessage, e);
    } finally {
      if(fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public DataTableMeta findDataTableMetaFromHdfs(HiveConnection hiveConnection, Path path, String fileId) {
    final String hiveAdminUser = hiveConnection.getSecondaryUsername();
    FileSystem fs = null;
    try {
      fs = getFileSystem(hiveConnection.getHdfsConfigurationPath(), hiveAdminUser);

      Path headerFile = new Path(path, String.format("%s.json", fileId));
      Path recordsFile = new Path(path, String.format("%s.dat", fileId));

      if(fs.exists(headerFile) && fs.exists(recordsFile)) {
        try(FSDataInputStream in = fs.open(headerFile)) {
          byte[] bs = new byte[in.available()];
          in.read(bs);

          List<String> headers = GlobalObjectMapper.readListValue(new String(bs), String.class);
          return new DataTableMeta(headers, recordsFile.toString());
        } catch(Exception e){
          throw new MetatronException(
              String.format("Failed find query result metadata", e));
        }
      } else {
        throw new MetatronException(
            String.format("Not found query result file : %s, %s", headerFile.toString(), recordsFile.toString()));
      }
    } catch (Exception e) {
      throw new MetatronException(
          String.format("Failed find query result metadata", e));
    } finally {
      if(fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void deleteHdfsDirectory(HiveConnection hiveConnection, Path path) {
    final String hiveAdminUser = hiveConnection.getSecondaryUsername();

    FileSystem fs = null;
    try {
      fs = getFileSystem(hiveConnection.getHdfsConfigurationPath(), hiveAdminUser);

      if(path.toString().startsWith("/tmp")) {
        if(fs.exists(path)) {
          fs.delete(path, true);
        }
      } else {
        throw new MetatronException(
            String.format("failed delete query result directory(%s) - Deleting directory must start with tmp",
                path.toString()));
      }
    } catch (Exception e) {
      throw new MetatronException("failed delete query result directory", e);
    } finally {
      if(fs != null) {
        try {
          fs.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private FileSystem getFileSystem(String hdfsConfigurationPath, String remoteUser) throws IOException, InterruptedException {
    final Configuration conf = getHdfsConfiguration(hdfsConfigurationPath);
    UserGroupInformation ugi = UserGroupInformation.createRemoteUser(remoteUser);
    return ugi.doAs((PrivilegedExceptionAction<FileSystem>) () -> FileSystem.get(conf));
  }

  private Configuration getHdfsConfiguration(String hdfsConfigurationPath) {
    final Configuration conf = new Configuration();
    conf.addResource(new Path(String.format("%s/core-site.xml", hdfsConfigurationPath)));
    conf.addResource(new Path(String.format("%s/hdfs-site.xml", hdfsConfigurationPath)));

    return conf;
  }

}