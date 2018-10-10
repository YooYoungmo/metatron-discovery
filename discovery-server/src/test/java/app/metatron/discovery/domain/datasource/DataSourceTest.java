/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.metatron.discovery.domain.datasource;

import app.metatron.discovery.common.datasource.DataType;
import com.google.common.collect.Sets;

import com.facebook.presto.jdbc.internal.guava.collect.Maps;

import com.sun.xml.internal.ws.api.model.ExceptionType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.crypto.Data;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by kyungtaak on 2016. 10. 20..
 */
public class DataSourceTest {
  @Test
  public void getRegexDataSourceName() throws Exception {

    DataSource dataSource = new DataSource();
    dataSource.setName("fdc_summary");
    dataSource.setPartitionKeys("fab,eqp_id");
    dataSource.setPartitionSeparator("_");

    // 2개 모두 비어있는 경우
    LinkedHashMap<String, Set<String>> model1 = Maps.newLinkedHashMap();
    model1.put("fab", Sets.newHashSet());
    model1.put("eqp_id", Sets.newHashSet());
    System.out.println(dataSource.getRegexDataSourceName(model1));

    // 첫번째 값이 비어있는 경우
    LinkedHashMap<String, Set<String>> model2 = Maps.newLinkedHashMap();
    model2.put("fab", Sets.newHashSet());
    model2.put("eqp_id", Sets.newHashSet("CMP102"));
    System.out.println(dataSource.getRegexDataSourceName(model2));

    // 마지막 값이 비어있는 경우
    LinkedHashMap<String, Set<String>> model3 = Maps.newLinkedHashMap();
    model3.put("fab", Sets.newHashSet("M10"));
    model3.put("eqp_id", Sets.newHashSet());
    System.out.println(dataSource.getRegexDataSourceName(model3));
  }

  @Test
  public void validateCompatibleDatasource_when_incompatible_status() {
    // given
    DataSource mainDs = new DataSource();
    mainDs.setStatus(DataSource.Status.ENABLED);

    DataSource targetDs = new DataSource();
    targetDs.setStatus(DataSource.Status.DISABLED);

    try {
      // when
      mainDs.validateCompatibleDatasource(targetDs);
      fail("Expected exception");
    } catch (DataSourceIncompatibleException dsie) {
      // then
      assertThat(dsie.getCode()).isEqualTo(DataSourceIncompatibleException.DataSourceIncompatibleErrorCodes.STATUS_ERROR_CODE);
    } catch (Exception e) {
      fail("Expected exception");
    }
  }

  @Test
  public void validateCompatibleDatasource_when_incompatible_connection_type() {
    // given
    DataSource mainDs = new DataSource();
    mainDs.setStatus(DataSource.Status.ENABLED);
    mainDs.setConnType(DataSource.ConnectionType.ENGINE);

    DataSource targetDs = new DataSource();
    targetDs.setStatus(DataSource.Status.ENABLED);
    targetDs.setConnType(DataSource.ConnectionType.LINK);

    try {
      // when
      mainDs.validateCompatibleDatasource(targetDs);
      fail("Expected exception");
    } catch (DataSourceIncompatibleException dsie) {
      // then
      assertThat(dsie.getCode()).isEqualTo(DataSourceIncompatibleException.DataSourceIncompatibleErrorCodes.CONNECTION_TYPE_DIFFERENT_CODE);
    } catch (Exception e) {
      fail("Expected exception");
    }
  }

  @Test
  public void validateCompatibleDatasource_when_incompatible_granularity() {
    // given
    DataSource mainDs = new DataSource();
    mainDs.setStatus(DataSource.Status.ENABLED);
    mainDs.setConnType(DataSource.ConnectionType.ENGINE);
    mainDs.setGranularity(DataSource.GranularityType.DAY);

    DataSource targetDs = new DataSource();
    targetDs.setStatus(DataSource.Status.ENABLED);
    targetDs.setConnType(DataSource.ConnectionType.ENGINE);
    targetDs.setGranularity(DataSource.GranularityType.MONTH);

    try {
      // when
      mainDs.validateCompatibleDatasource(targetDs);
      fail("Expected exception");
    } catch (DataSourceIncompatibleException dsie) {
      // then
      assertThat(dsie.getCode()).isEqualTo(DataSourceIncompatibleException.DataSourceIncompatibleErrorCodes.GRANULARITY_DIFFERENT_CODE);
    } catch (Exception e) {
      fail("Expected exception");
    }
  }

  @Test
  public void validateCompatibleDatasource_when_incompatible_fields_target_datasource_field_does_not_exist() {
    // given
    DataSource mainDs = new DataSource();
    mainDs.setStatus(DataSource.Status.ENABLED);
    mainDs.setConnType(DataSource.ConnectionType.ENGINE);
    mainDs.setGranularity(DataSource.GranularityType.DAY);
    mainDs.addField(new Field("time", DataType.TIMESTAMP, Field.FieldRole.TIMESTAMP, 0l));
    mainDs.addField(new Field("d", DataType.STRING, Field.FieldRole.DIMENSION, 1l));
    mainDs.addField(new Field("m", DataType.INTEGER, Field.FieldRole.MEASURE, 2l));

    DataSource targetDs = new DataSource();
    targetDs.setStatus(DataSource.Status.ENABLED);
    targetDs.setConnType(DataSource.ConnectionType.ENGINE);
    targetDs.setGranularity(DataSource.GranularityType.DAY);
    targetDs.addField(new Field("time", DataType.TIMESTAMP, Field.FieldRole.TIMESTAMP, 0l));
    targetDs.addField(new Field("d", DataType.STRING, Field.FieldRole.DIMENSION, 1l));

    try {
      // when
      mainDs.validateCompatibleDatasource(targetDs);
      fail("Expected exception");
    } catch (DataSourceIncompatibleException dsie) {
      // then
      assertThat(dsie.getCode()).isEqualTo(DataSourceIncompatibleException.DataSourceIncompatibleErrorCodes.FIELD_ERROR_CODE);
    } catch (Exception e) {
      fail("Expected exception");
    }

  }

  @Test
  public void validateCompatibleDatasource_when_incompatible_fields_target_datasource_field_name_different() {
    // given
    DataSource mainDs = new DataSource();
    mainDs.setStatus(DataSource.Status.ENABLED);
    mainDs.setConnType(DataSource.ConnectionType.ENGINE);
    mainDs.setGranularity(DataSource.GranularityType.DAY);
    mainDs.addField(new Field("time", DataType.TIMESTAMP, Field.FieldRole.TIMESTAMP, 0l));
    mainDs.addField(new Field("d", DataType.STRING, Field.FieldRole.DIMENSION, 1l));
    mainDs.addField(new Field("m", DataType.INTEGER, Field.FieldRole.MEASURE, 2l));

    DataSource targetDs = new DataSource();
    targetDs.setStatus(DataSource.Status.ENABLED);
    targetDs.setConnType(DataSource.ConnectionType.ENGINE);
    targetDs.setGranularity(DataSource.GranularityType.DAY);
    targetDs.addField(new Field("time", DataType.TIMESTAMP, Field.FieldRole.TIMESTAMP, 0l));
    targetDs.addField(new Field("d", DataType.STRING, Field.FieldRole.DIMENSION, 1l));
    targetDs.addField(new Field("m1", DataType.STRING, Field.FieldRole.DIMENSION, 1l));

    // when
    try {
      // when
      mainDs.validateCompatibleDatasource(targetDs);
      fail("Expected exception");
    } catch (DataSourceIncompatibleException dsie) {
      // then
      assertThat(dsie.getCode()).isEqualTo(DataSourceIncompatibleException.DataSourceIncompatibleErrorCodes.FIELD_ERROR_CODE);
    } catch (Exception e) {
      fail("Expected exception");
    }
  }

  @Test
  public void validateCompatibleDatasource_when_incompatible_fields_target_datasource_field_role_different() {
    // given
    DataSource mainDs = new DataSource();
    mainDs.setStatus(DataSource.Status.ENABLED);
    mainDs.setConnType(DataSource.ConnectionType.ENGINE);
    mainDs.setGranularity(DataSource.GranularityType.DAY);
    mainDs.addField(new Field("time", DataType.TIMESTAMP, Field.FieldRole.TIMESTAMP, 0l));
    mainDs.addField(new Field("d", DataType.STRING, Field.FieldRole.DIMENSION, 1l));
    mainDs.addField(new Field("m", DataType.INTEGER, Field.FieldRole.MEASURE, 2l));

    DataSource targetDs = new DataSource();
    targetDs.setStatus(DataSource.Status.ENABLED);
    targetDs.setConnType(DataSource.ConnectionType.ENGINE);
    targetDs.setGranularity(DataSource.GranularityType.DAY);
    targetDs.addField(new Field("time", DataType.TIMESTAMP, Field.FieldRole.TIMESTAMP, 0l));
    targetDs.addField(new Field("d", DataType.STRING, Field.FieldRole.DIMENSION, 1l));
    targetDs.addField(new Field("m", DataType.STRING, Field.FieldRole.DIMENSION, 2l));

    // when
    try {
      // when
      mainDs.validateCompatibleDatasource(targetDs);
      fail("Expected exception");
    } catch (DataSourceIncompatibleException dsie) {
      // then
      assertThat(dsie.getCode()).isEqualTo(DataSourceIncompatibleException.DataSourceIncompatibleErrorCodes.FIELD_ERROR_CODE);
    } catch (Exception e) {
      fail("Expected exception");
    }
  }

  @Test
  public void validateCompatibleDatasource_when_incompatible_fields_target_datasource_field_data_type_different() {
    // given
    DataSource mainDs = new DataSource();
    mainDs.setStatus(DataSource.Status.ENABLED);
    mainDs.setConnType(DataSource.ConnectionType.ENGINE);
    mainDs.setGranularity(DataSource.GranularityType.DAY);
    mainDs.addField(new Field("time", DataType.TIMESTAMP, Field.FieldRole.TIMESTAMP, 0l));
    mainDs.addField(new Field("d", DataType.STRING, Field.FieldRole.DIMENSION, 1l));
    mainDs.addField(new Field("m", DataType.INTEGER, Field.FieldRole.MEASURE, 2l));

    DataSource targetDs = new DataSource();
    targetDs.setStatus(DataSource.Status.ENABLED);
    targetDs.setConnType(DataSource.ConnectionType.ENGINE);
    targetDs.setGranularity(DataSource.GranularityType.DAY);
    targetDs.addField(new Field("time", DataType.TIMESTAMP, Field.FieldRole.TIMESTAMP, 0l));
    targetDs.addField(new Field("d", DataType.STRING, Field.FieldRole.DIMENSION, 1l));
    targetDs.addField(new Field("m", DataType.DOUBLE, Field.FieldRole.MEASURE, 2l));

    // when
    try {
      // when
      mainDs.validateCompatibleDatasource(targetDs);
      fail("Expected exception");
    } catch (DataSourceIncompatibleException dsie) {
      // then
      assertThat(dsie.getCode()).isEqualTo(DataSourceIncompatibleException.DataSourceIncompatibleErrorCodes.FIELD_ERROR_CODE);
    } catch (Exception e) {
      fail("Expected exception");
    }
  }

  @Test
  public void validateCompatibleDatasource_when_compatible_fields() {
    // given
    DataSource mainDs = new DataSource();
    mainDs.setStatus(DataSource.Status.ENABLED);
    mainDs.setConnType(DataSource.ConnectionType.ENGINE);
    mainDs.setGranularity(DataSource.GranularityType.DAY);
    mainDs.addField(new Field("time", DataType.TIMESTAMP, Field.FieldRole.TIMESTAMP, 0l));
    mainDs.addField(new Field("d", DataType.STRING, Field.FieldRole.DIMENSION, 1l));
    mainDs.addField(new Field("m", DataType.INTEGER, Field.FieldRole.MEASURE, 2l));

    DataSource targetDs = new DataSource();
    targetDs.setStatus(DataSource.Status.ENABLED);
    targetDs.setConnType(DataSource.ConnectionType.ENGINE);
    targetDs.setGranularity(DataSource.GranularityType.DAY);
    targetDs.addField(new Field("time", DataType.TIMESTAMP, Field.FieldRole.TIMESTAMP, 0l));
    targetDs.addField(new Field("d", DataType.STRING, Field.FieldRole.DIMENSION, 1l));
    targetDs.addField(new Field("m", DataType.INTEGER, Field.FieldRole.MEASURE, 2l));

    try {
      // when
      mainDs.validateCompatibleDatasource(targetDs);

      // then
      // void..

    } catch (Exception e) {
      fail("Expected exception");
    }
  }

  @Test
  public void validateCompatibleDatasource_when_compatible_fields_2() {
    // given
    DataSource mainDs = new DataSource();
    mainDs.setStatus(DataSource.Status.ENABLED);
    mainDs.setConnType(DataSource.ConnectionType.ENGINE);
    mainDs.setGranularity(DataSource.GranularityType.DAY);
    mainDs.addField(new Field("time", DataType.TIMESTAMP, Field.FieldRole.TIMESTAMP, 0l));
    mainDs.addField(new Field("d", DataType.STRING, Field.FieldRole.DIMENSION, 1l));
    mainDs.addField(new Field("m", DataType.INTEGER, Field.FieldRole.MEASURE, 2l));

    DataSource targetDs = new DataSource();
    targetDs.setStatus(DataSource.Status.ENABLED);
    targetDs.setConnType(DataSource.ConnectionType.ENGINE);
    targetDs.setGranularity(DataSource.GranularityType.DAY);
    targetDs.addField(new Field("time", DataType.TIMESTAMP, Field.FieldRole.TIMESTAMP, 0l));
    targetDs.addField(new Field("d", DataType.STRING, Field.FieldRole.DIMENSION, 1l));
    targetDs.addField(new Field("m", DataType.INTEGER, Field.FieldRole.MEASURE, 2l));
    targetDs.addField(new Field("m2", DataType.INTEGER, Field.FieldRole.MEASURE, 3l));
    targetDs.addField(new Field("d2", DataType.STRING, Field.FieldRole.DIMENSION, 4l));

    try {
      // when
      mainDs.validateCompatibleDatasource(targetDs);

      // then
      // void..

    } catch (Exception e) {
      fail("Expected exception");
    }
  }
}
