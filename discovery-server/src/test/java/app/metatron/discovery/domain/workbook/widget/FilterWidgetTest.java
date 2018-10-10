package app.metatron.discovery.domain.workbook.widget;

import app.metatron.discovery.domain.datasource.DataSource;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


public class FilterWidgetTest {

  @Test
  public void changeDataSource() {
    // given
    Widget widget = new FilterWidget();
    widget.setConfiguration(configuration());

    // when
    DataSource productSalesDs = new DataSource();
    productSalesDs.setId(UUID.randomUUID().toString());
    productSalesDs.setName("product-sales-ds");
    productSalesDs.setEngineName("productsalesds");

    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    widget.changeDataSource(productSalesDs, changingDs);

    // then
    Map<String, Object> configuration = widget.getConfigurationToMap();
    Map<String, Object> filter = (Map<String, Object>)configuration.get("filter");
    assertThat(filter.get("dataSource")).isEqualTo(changingDs.getEngineName());
  }

  public String configuration() {
    return "{" +
        "  \"filter\": {" +
        "    \"dataSource\": \"productsalesds\"," +
        "    \"discontinuous\": false," +
        "    \"field\": \"time\"," +
        "    \"timeUnit\": \"NONE\"," +
        "    \"type\": \"time_list\"" +
        "  }," +
        "  \"type\": \"filter\"" +
        "}";
  }

  @Test
  public void changeDataSource_with_ref() {
    // given
    Widget widget = new FilterWidget();
    widget.setConfiguration(configurationWithRef());

    // when
    DataSource productSalesDs = new DataSource();
    productSalesDs.setId(UUID.randomUUID().toString());
    productSalesDs.setName("product-sales-ds");
    productSalesDs.setEngineName("productsalesds");

    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    widget.changeDataSource(productSalesDs, changingDs);

    // then
    Map<String, Object> configuration = widget.getConfigurationToMap();
    Map<String, Object> filter = (Map<String, Object>)configuration.get("filter");
    assertThat(filter.get("dataSource")).isEqualTo(changingDs.getEngineName());
    assertThat(filter.get("ref")).isEqualTo(changingDs.getEngineName());
  }

  public String configurationWithRef() {
    return "{" +
        "  \"filter\": {" +
        "    \"candidateValues\": []," +
        "    \"dataSource\": \"productsalesds\"," +
        "    \"definedValues\": []," +
        "    \"field\": \"product_id\"," +
        "    \"preFilters\": [" +
        "      {" +
        "        \"aggregation\": \"SUM\"," +
        "        \"inequality\": \"EQUAL_TO\"," +
        "        \"type\": \"measure_inequality\"," +
        "        \"ui\": {" +
        "          \"importanceType\": \"general\"" +
        "        }," +
        "        \"value\": 10" +
        "      }," +
        "      {" +
        "        \"aggregation\": \"SUM\"," +
        "        \"position\": \"TOP\"," +
        "        \"type\": \"measure_position\"," +
        "        \"ui\": {" +
        "          \"importanceType\": \"general\"" +
        "        }," +
        "        \"value\": 10" +
        "      }," +
        "      {" +
        "        \"contains\": \"AFTER\"," +
        "        \"field\": \"product_id\"," +
        "        \"type\": \"wildcard\"," +
        "        \"ui\": {" +
        "          \"importanceType\": \"general\"" +
        "        }," +
        "        \"value\": \"\"" +
        "      }" +
        "    ]," +
        "    \"ref\": \"productsalesds\"," +
        "    \"selector\": \"MULTI_COMBO\"," +
        "    \"sort\": {" +
        "      \"by\": \"TEXT\"," +
        "      \"direction\": \"ASC\"" +
        "    }," +
        "    \"type\": \"include\"," +
        "    \"valueList\": []" +
        "  }," +
        "  \"type\": \"filter\"" +
        "}";
  }

  @Test
  public void changeDataSource_with_ref_change_ref() {
    // given
    Widget widget = new FilterWidget();
    widget.setConfiguration(configurationWithRefForChangeRef());

    // when
    DataSource productDs = new DataSource();
    productDs.setId(UUID.randomUUID().toString());
    productDs.setName("product-ds");
    productDs.setEngineName("productds");


    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    widget.changeDataSource(productDs, changingDs);

    // then
    Map<String, Object> configuration = widget.getConfigurationToMap();
    Map<String, Object> filter = (Map<String, Object>)configuration.get("filter");
    assertThat(filter.get("dataSource")).isEqualTo("productsalesds");
    assertThat(filter.get("ref")).isEqualTo(changingDs.getEngineName());
  }

  public String configurationWithRefForChangeRef() {
    return "{" +
        "  \"filter\": {" +
        "    \"candidateValues\": []," +
        "    \"dataSource\": \"productsalesds\"," +
        "    \"definedValues\": []," +
        "    \"field\": \"name\"," +
        "    \"preFilters\": [" +
        "      {" +
        "        \"aggregation\": \"SUM\"," +
        "        \"inequality\": \"EQUAL_TO\"," +
        "        \"type\": \"measure_inequality\"," +
        "        \"ui\": {" +
        "          \"importanceType\": \"general\"" +
        "        }," +
        "        \"value\": 10" +
        "      }," +
        "      {" +
        "        \"aggregation\": \"SUM\"," +
        "        \"position\": \"TOP\"," +
        "        \"type\": \"measure_position\"," +
        "        \"ui\": {" +
        "          \"importanceType\": \"general\"" +
        "        }," +
        "        \"value\": 10" +
        "      }," +
        "      {" +
        "        \"contains\": \"AFTER\"," +
        "        \"field\": \"name\"," +
        "        \"type\": \"wildcard\"," +
        "        \"ui\": {" +
        "          \"importanceType\": \"general\"" +
        "        }," +
        "        \"value\": \"\"" +
        "      }" +
        "    ]," +
        "    \"ref\": \"productds\"," +
        "    \"selector\": \"SINGLE_COMBO\"," +
        "    \"sort\": {" +
        "      \"by\": \"TEXT\"," +
        "      \"direction\": \"ASC\"" +
        "    }," +
        "    \"type\": \"include\"," +
        "    \"valueList\": []" +
        "  }," +
        "  \"type\": \"filter\"" +
        "}";
  }



}