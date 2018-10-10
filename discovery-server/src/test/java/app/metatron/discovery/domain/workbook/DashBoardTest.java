package app.metatron.discovery.domain.workbook;

import app.metatron.discovery.domain.datasource.DataSource;
import app.metatron.discovery.domain.workbook.widget.Widget;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DashBoardTest {

  @Test
  public void changeDataSource_when_datasources_is_null() {
    // given
    DashBoard dashBoard = new DashBoard();

    // when
    dashBoard.changeDataSource(new DataSource(), new DataSource());

    // then
    assertThat(dashBoard.getDataSources()).isNull();
  }

  @Test
  public void changeDataSource_when_datasource_type_default() {
    // given
    DashBoard dashBoard = new DashBoard();
    dashBoard.setId(UUID.randomUUID().toString());
    dashBoard.setConfiguration(configurationDefaultDatasource());

    DataSource productSalesDs = new DataSource();
    productSalesDs.setId("08572bd4-5b43-41fe-8dd5-5fbf1d8955ba");
    productSalesDs.setName("product-sales-ds");
    productSalesDs.setEngineName("productsalesds");

    Set dataSourcesInDashboard = new HashSet<DataSource>();
    dataSourcesInDashboard.add(productSalesDs);
    dashBoard.setDataSources(dataSourcesInDashboard);

    Set widgetsInDashboard = new HashSet<Widget>();
    Widget mockWidget1 = mock(Widget.class);
    Widget mockWidget2 = mock(Widget.class);
    widgetsInDashboard.add(mockWidget1);
    widgetsInDashboard.add(mockWidget2);
    dashBoard.setWidgets(widgetsInDashboard);

    // when
    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    dashBoard.changeDataSource(productSalesDs, changingDs);

    // then
    assertThat(dashBoard.getDataSources()).hasSize(1);
    assertThat(dashBoard.getDataSources())
        .contains(changingDs)
        .doesNotContain(productSalesDs);

    Map<String, Object> dashboardConfiguration = dashBoard.getConfigurationToMap();

    Map<String, Object> dataSource = (Map<String, Object>)dashboardConfiguration.get("dataSource");
    assertThat((String)dataSource.get("id")).isEqualTo(changingDs.getId());
    assertThat((String)dataSource.get("name")).isEqualTo(changingDs.getEngineName());

    ((List<Map<String, Object>>)dashboardConfiguration.get("filters")).forEach(filter -> {
      assertThat((String)filter.get("dataSource")).isEqualTo(changingDs.getEngineName());
    });

    verify(mockWidget1).changeDataSource(productSalesDs, changingDs);
    verify(mockWidget2).changeDataSource(productSalesDs, changingDs);
  }

  private String configurationDefaultDatasource() {
    return "{" +
        "  \"content\": []," +
        "  \"dataSource\": {" +
        "    \"id\": \"08572bd4-5b43-41fe-8dd5-5fbf1d8955ba\"," +
        "    \"joins\": []," +
        "    \"name\": \"productsalesds\"," +
        "    \"temporary\": false," +
        "    \"type\": \"default\"," +
        "    \"uiDescription\": \"\"" +
        "  }," +
        "  \"filters\": [" +
        "    {" +
        "      \"dataSource\": \"productsalesds\"," +
        "      \"field\": \"time\"," +
        "      \"type\": \"time_all\"" +
        "    }," +
        "    {" +
        "      \"dataSource\": \"productsalesds\"," +
        "      \"field\": \"amount\"," +
        "      \"max\": 2111," +
        "      \"min\": 129," +
        "      \"type\": \"bound\"" +
        "    }" +
        "  ]," +
        "  \"options\": {" +
        "    \"layout\": {" +
        "      \"layoutType\": \"FIT_TO_SCREEN\"," +
        "      \"widgetPadding\": 5" +
        "    }," +
        "    \"widget\": {" +
        "      \"showLegend\": \"BY_WIDGET\"," +
        "      \"showMinimap\": \"BY_WIDGET\"," +
        "      \"showTitle\": \"BY_WIDGET\"" +
        "    }" +
        "  }," +
        "  \"widgets\": []" +
        "}";
  }

  @Test
  public void changeDataSource_when_datasource_type_multi() {
    // given
    DashBoard dashBoard = new DashBoard();
    dashBoard.setId(UUID.randomUUID().toString());
    dashBoard.setConfiguration(configurationMultiDatasource());

    DataSource productSalesDs = new DataSource();
    productSalesDs.setId("08572bd4-5b43-41fe-8dd5-5fbf1d8955ba");
    productSalesDs.setName("product-sales-ds");
    productSalesDs.setEngineName("productsalesds");

    DataSource productDs = new DataSource();
    productDs.setId("ba3257ee-fe53-4a42-885c-b3143eff02ac");
    productDs.setName("product-ds");
    productDs.setEngineName("productds");

    Set dataSourcesInDashboard = new HashSet<DataSource>();
    dataSourcesInDashboard.add(productSalesDs);
    dataSourcesInDashboard.add(productDs);
    dashBoard.setDataSources(dataSourcesInDashboard);

    // when
    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    dashBoard.changeDataSource(productSalesDs, changingDs);

    // then
    assertThat(dashBoard.getDataSources()).hasSize(2);
    assertThat(dashBoard.getDataSources())
        .contains(changingDs, productDs)
        .doesNotContain(productSalesDs);

    Map<String, Object> dashboardConfiguration = dashBoard.getConfigurationToMap();

    Map<String, Object> dataSource = (Map<String, Object>)dashboardConfiguration.get("dataSource");
    List<Map<String, Object>> associations = (List<Map<String, Object>>)dataSource.get("associations");
    assertThat(associations).hasSize(0);

    List<Map<String, Object>> dataSources = (List<Map<String, Object>>)dataSource.get("dataSources");
    assertThat(dataSources).hasSize(2);
    assertThat(dataSources).extracting("id")
        .contains(productDs.getId(), changingDs.getId())
        .doesNotContain(productSalesDs.getId());
    assertThat(dataSources).extracting("engineName")
        .contains(productDs.getEngineName(), changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
    assertThat(dataSources).extracting("name")
        .contains(productDs.getEngineName(), changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());

    List<Map<String, Object>> filters = ((List<Map<String, Object>>)dashboardConfiguration.get("filters"));
    assertThat(filters).hasSize(2);
    assertThat(filters).extracting("dataSource")
        .contains(productDs.getEngineName(), changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
  }

  public String configurationMultiDatasource() {
    return "{" +
        "  \"content\": []," +
        "  \"dataSource\": {" +
        "    \"associations\": []," +
        "    \"dataSources\": [" +
        "      {" +
        "        \"connType\": \"ENGINE\"," +
        "        \"engineName\": \"productds\"," +
        "        \"id\": \"ba3257ee-fe53-4a42-885c-b3143eff02ac\"," +
        "        \"joins\": []," +
        "        \"name\": \"productds\"," +
        "        \"temporary\": false," +
        "        \"type\": \"default\"," +
        "        \"uiDescription\": \"\"" +
        "      }," +
        "      {" +
        "        \"connType\": \"ENGINE\"," +
        "        \"engineName\": \"productsalesds\"," +
        "        \"id\": \"08572bd4-5b43-41fe-8dd5-5fbf1d8955ba\"," +
        "        \"joins\": []," +
        "        \"name\": \"productsalesds\"," +
        "        \"temporary\": false," +
        "        \"type\": \"default\"," +
        "        \"uiDescription\": \"\"" +
        "      }" +
        "    ]," +
        "    \"joins\": []," +
        "    \"temporary\": false," +
        "    \"type\": \"multi\"" +
        "  }," +
        "  \"filters\": [" +
        "    {" +
        "      \"dataSource\": \"productds\"," +
        "      \"field\": \"current_datetime\"," +
        "      \"type\": \"time_all\"" +
        "    }," +
        "    {" +
        "      \"dataSource\": \"productsalesds\"," +
        "      \"field\": \"time\"," +
        "      \"type\": \"time_all\"" +
        "    }" +
        "  ]," +
        "  \"options\": {" +
        "    \"layout\": {" +
        "      \"layoutType\": \"FIT_TO_SCREEN\"," +
        "      \"widgetPadding\": 5" +
        "    }," +
        "    \"widget\": {" +
        "      \"showLegend\": \"BY_WIDGET\"," +
        "      \"showMinimap\": \"BY_WIDGET\"," +
        "      \"showTitle\": \"BY_WIDGET\"" +
        "    }" +
        "  }," +
        "  \"widgets\": []" +
        "}";
  }

  @Test
  public void changeDataSource_when_datasource_type_multi_with_associations() {
    // given
    DashBoard dashBoard = new DashBoard();
    dashBoard.setId(UUID.randomUUID().toString());
    dashBoard.setConfiguration(configurationMultiDatasourceWithAssociations());

    DataSource productSalesDs = new DataSource();
    productSalesDs.setId("08572bd4-5b43-41fe-8dd5-5fbf1d8955ba");
    productSalesDs.setName("product-sales-ds");
    productSalesDs.setEngineName("productsalesds");

    DataSource productDs = new DataSource();
    productDs.setId("ba3257ee-fe53-4a42-885c-b3143eff02ac");
    productDs.setName("product-ds");
    productDs.setEngineName("productds");

    Set dataSourcesInDashboard = new HashSet<DataSource>();
    dataSourcesInDashboard.add(productSalesDs);
    dataSourcesInDashboard.add(productDs);
    dashBoard.setDataSources(dataSourcesInDashboard);

    // when
    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    dashBoard.changeDataSource(productSalesDs, changingDs);

    // then
    assertThat(dashBoard.getDataSources()).hasSize(2);
    assertThat(dashBoard.getDataSources())
        .contains(changingDs, productDs)
        .doesNotContain(productSalesDs);

    Map<String, Object> dashboardConfiguration = dashBoard.getConfigurationToMap();

    Map<String, Object> dataSource = (Map<String, Object>) dashboardConfiguration.get("dataSource");
    List<Map<String, Object>> associations = (List<Map<String, Object>>) dataSource.get("associations");
    assertThat(associations).hasSize(1);
    assertThat((String) associations.get(0).get("source")).isEqualTo(changingDs.getEngineName());

    List<Map<String, Object>> dataSources = (List<Map<String, Object>>) dataSource.get("dataSources");
    assertThat(dataSources).hasSize(2);
    assertThat(dataSources).extracting("id")
        .contains(productDs.getId(), changingDs.getId())
        .doesNotContain(productSalesDs.getId());
    assertThat(dataSources).extracting("engineName")
        .contains(productDs.getEngineName(), changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
    assertThat(dataSources).extracting("name")
        .contains(productDs.getEngineName(), changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());

    assertThat(dashboardConfiguration.get("filters")).isNull();
  }

  private String configurationMultiDatasourceWithAssociations() {
    return "{" +
        "  \"content\": []," +
        "  \"dataSource\": {" +
        "    \"associations\": [" +
        "      {" +
        "        \"columnPair\": {" +
        "          \"product_id\": \"product_id\"" +
        "        }," +
        "        \"source\": \"productsalesds\"," +
        "        \"target\": \"productds\"" +
        "      }" +
        "    ]," +
        "    \"dataSources\": [" +
        "      {" +
        "        \"connType\": \"ENGINE\"," +
        "        \"engineName\": \"productds\"," +
        "        \"id\": \"ba3257ee-fe53-4a42-885c-b3143eff02ac\"," +
        "        \"joins\": []," +
        "        \"name\": \"productds\"," +
        "        \"temporary\": false," +
        "        \"type\": \"default\"," +
        "        \"uiDescription\": \"\"" +
        "      }," +
        "      {" +
        "        \"connType\": \"ENGINE\"," +
        "        \"engineName\": \"productsalesds\"," +
        "        \"id\": \"08572bd4-5b43-41fe-8dd5-5fbf1d8955ba\"," +
        "        \"joins\": []," +
        "        \"name\": \"productsalesds\"," +
        "        \"temporary\": false," +
        "        \"type\": \"default\"," +
        "        \"uiDescription\": \"\"" +
        "      }" +
        "    ]," +
        "    \"joins\": []," +
        "    \"temporary\": false," +
        "    \"type\": \"multi\"" +
        "  }," +
        "  \"options\": {" +
        "    \"layout\": {" +
        "      \"layoutType\": \"FIT_TO_SCREEN\"," +
        "      \"widgetPadding\": 5" +
        "    }," +
        "    \"widget\": {" +
        "      \"showLegend\": \"BY_WIDGET\"," +
        "      \"showMinimap\": \"BY_WIDGET\"," +
        "      \"showTitle\": \"BY_WIDGET\"" +
        "    }" +
        "  }," +
        "  \"widgets\": []" +
        "}";
  }

  @Test
  public void changeDataSource_when_datasource_type_multi_with_join() {
    // given
    DashBoard dashBoard = new DashBoard();
    dashBoard.setId(UUID.randomUUID().toString());
    dashBoard.setConfiguration(configurationMultiDatasourceWithJoin());

    Set dataSourcesInDashboard = new HashSet<DataSource>();

    DataSource productSalesDs = new DataSource();
    productSalesDs.setId("08572bd4-5b43-41fe-8dd5-5fbf1d8955ba");
    productSalesDs.setName("product-sales-ds");
    productSalesDs.setEngineName("productsalesds");
    dataSourcesInDashboard.add(productSalesDs);

    DataSource productDs = new DataSource();
    productDs.setId("ba3257ee-fe53-4a42-885c-b3143eff02ac");
    productDs.setName("product-ds");
    productDs.setEngineName("productds");
    dataSourcesInDashboard.add(productDs);

    DataSource testDs = new DataSource();
    testDs.setId("e9f2fb63-6cb7-4445-9b8b-5d0bafc9c0d6");
    testDs.setName("test-ds");
    testDs.setEngineName("testds");
    dataSourcesInDashboard.add(testDs);

    DataSource abcDs = new DataSource();
    abcDs.setId("937f9d4a-5b83-48e8-b528-1dcf5724c960");
    abcDs.setName("abc-ds");
    abcDs.setEngineName("abcds");
    dataSourcesInDashboard.add(abcDs);

    dashBoard.setDataSources(dataSourcesInDashboard);

    // when
    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    dashBoard.changeDataSource(testDs, changingDs);

    // then
    assertThat(dashBoard.getDataSources()).hasSize(4);
    assertThat(dashBoard.getDataSources())
        .contains(changingDs, productSalesDs, productDs, abcDs)
        .doesNotContain(testDs);

    Map<String, Object> configuration = dashBoard.getConfigurationToMap();
    Map<String, Object> dataSource = (Map<String, Object>)configuration.get("dataSource");
    List<Map<String, Object>> dataSources = (List<Map<String, Object>>) dataSource.get("dataSources");
    assertThat(dataSources).hasSize(2);

    Map<String, Object> findDs = dataSources.stream().filter(ds -> ds.get("id").equals(abcDs.getId())).findFirst().get();
    List<Map<String, Object>> joins = (List<Map<String, Object>>)findDs.get("joins");
    assertThat(joins).extracting("id")
        .contains(changingDs.getId())
        .doesNotContain(testDs.getId());
    assertThat(joins).extracting("name")
        .contains(changingDs.getEngineName())
        .doesNotContain(testDs.getEngineName());
  }

  private String configurationMultiDatasourceWithJoin() {
    return "{" +
        "  \"dataSource\": {" +
        "    \"associations\": []," +
        "    \"dataSources\": [" +
        "      {" +
        "        \"connType\": \"ENGINE\"," +
        "        \"engineName\": \"productsalesds\"," +
        "        \"id\": \"08572bd4-5b43-41fe-8dd5-5fbf1d8955ba\"," +
        "        \"joins\": [" +
        "          {" +
        "            \"id\": \"ba3257ee-fe53-4a42-885c-b3143eff02ac\"," +
        "            \"joinAlias\": \"join_1\"," +
        "            \"keyPair\": {" +
        "              \"product_id\": \"product_id\"" +
        "            }," +
        "            \"name\": \"productds\"," +
        "            \"type\": \"LEFT_OUTER\"" +
        "          }" +
        "        ]," +
        "        \"name\": \"product-sales-ds\"," +
        "        \"temporary\": false," +
        "        \"type\": \"mapping\"," +
        "        \"uiDescription\": \"\"" +
        "      }," +
        "      {" +
        "        \"connType\": \"ENGINE\"," +
        "        \"engineName\": \"abcds\"," +
        "        \"id\": \"937f9d4a-5b83-48e8-b528-1dcf5724c960\"," +
        "        \"joins\": [" +
        "          {" +
        "            \"id\": \"e9f2fb63-6cb7-4445-9b8b-5d0bafc9c0d6\"," +
        "            \"joinAlias\": \"join_1\"," +
        "            \"keyPair\": {" +
        "              \"d\": \"d\"" +
        "            }," +
        "            \"name\": \"testds\"," +
        "            \"type\": \"LEFT_OUTER\"" +
        "          }" +
        "        ]," +
        "        \"name\": \"abcd\"," +
        "        \"temporary\": false," +
        "        \"type\": \"mapping\"," +
        "        \"uiDescription\": \"\"" +
        "      }" +
        "    ]," +
        "    \"joins\": []," +
        "    \"temporary\": false," +
        "    \"type\": \"multi\"" +
        "  }," +
        "  \"options\": {" +
        "    \"layout\": {" +
        "      \"layoutType\": \"FIT_TO_SCREEN\"," +
        "      \"widgetPadding\": 5" +
        "    }," +
        "    \"widget\": {" +
        "      \"showLegend\": \"BY_WIDGET\"," +
        "      \"showMinimap\": \"BY_WIDGET\"," +
        "      \"showTitle\": \"BY_WIDGET\"" +
        "    }" +
        "  }" +
        "}";
  }

  @Test
  public void changeDataSource_when_datasource_type_mapping_change_main_datasource() {
    // given
    DashBoard dashBoard = new DashBoard();
    dashBoard.setId(UUID.randomUUID().toString());
    dashBoard.setConfiguration(configurationMappingDatasource());

    DataSource productSalesDs = new DataSource();
    productSalesDs.setId("08572bd4-5b43-41fe-8dd5-5fbf1d8955ba");
    productSalesDs.setName("product-sales-ds");
    productSalesDs.setEngineName("productsalesds");

    DataSource productDs = new DataSource();
    productDs.setId("ba3257ee-fe53-4a42-885c-b3143eff02ac");
    productDs.setName("product-ds");
    productDs.setEngineName("productds");

    Set dataSourcesInDashboard = new HashSet<DataSource>();
    dataSourcesInDashboard.add(productSalesDs);
    dataSourcesInDashboard.add(productDs);
    dashBoard.setDataSources(dataSourcesInDashboard);

    // when
    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    dashBoard.changeDataSource(productSalesDs, changingDs);

    // then
    assertThat(dashBoard.getDataSources()).hasSize(2);
    assertThat(dashBoard.getDataSources())
        .contains(changingDs, productDs)
        .doesNotContain(productSalesDs);

    Map<String, Object> configuration = dashBoard.getConfigurationToMap();
    Map<String, Object> dataSource = (Map<String, Object>)configuration.get("dataSource");
    assertThat(dataSource.get("id")).isEqualTo(changingDs.getId());
    assertThat(dataSource.get("engineName")).isEqualTo(changingDs.getEngineName());
    assertThat(dataSource.get("name")).isEqualTo(changingDs.getEngineName());

    List<Map<String, Object>> filters = ((List<Map<String, Object>>)configuration.get("filters"));
    assertThat(filters).hasSize(2);
    assertThat(filters).extracting("dataSource")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
    assertThat(filters).extracting("ref")
        .contains(changingDs.getEngineName(), productDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
  }

  @Test
  public void changeDataSource_when_datasource_type_mapping_change_join_datasource() {
    // given
    DashBoard dashBoard = new DashBoard();
    dashBoard.setId(UUID.randomUUID().toString());
    dashBoard.setConfiguration(configurationMappingDatasource());

    DataSource productSalesDs = new DataSource();
    productSalesDs.setId("08572bd4-5b43-41fe-8dd5-5fbf1d8955ba");
    productSalesDs.setName("product-sales-ds");
    productSalesDs.setEngineName("productsalesds");

    DataSource productDs = new DataSource();
    productDs.setId("ba3257ee-fe53-4a42-885c-b3143eff02ac");
    productDs.setName("product-ds");
    productDs.setEngineName("productds");

    Set dataSourcesInDashboard = new HashSet<DataSource>();
    dataSourcesInDashboard.add(productSalesDs);
    dataSourcesInDashboard.add(productDs);
    dashBoard.setDataSources(dataSourcesInDashboard);

    // when
    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    dashBoard.changeDataSource(productDs, changingDs);

    // then
    assertThat(dashBoard.getDataSources()).hasSize(2);
    assertThat(dashBoard.getDataSources())
        .contains(changingDs, productSalesDs)
        .doesNotContain(productDs);

    Map<String, Object> configuration = dashBoard.getConfigurationToMap();
    Map<String, Object> dataSource = (Map<String, Object>)configuration.get("dataSource");
    List<Map<String, Object>> joins = (List<Map<String, Object>>)dataSource.get("joins");
    assertThat(joins).hasSize(1);
    assertThat((joins.get(0)).get("id")).isEqualTo(changingDs.getId());
    assertThat((joins.get(0)).get("engineName")).isEqualTo(changingDs.getEngineName());
    assertThat((joins.get(0)).get("name")).isEqualTo(changingDs.getEngineName());

    List<Map<String, Object>> filters = ((List<Map<String, Object>>)configuration.get("filters"));
    assertThat(filters).hasSize(2);
    assertThat(filters).extracting("dataSource")
        .contains(productSalesDs.getEngineName());
    assertThat(filters).extracting("ref")
        .contains(productSalesDs.getEngineName(), changingDs.getEngineName())
        .doesNotContain(productDs.getEngineName());
  }

  public String configurationMappingDatasource() {
    return "{" +
        "  \"content\": []," +
        "  \"dataSource\": {" +
        "    \"id\": \"08572bd4-5b43-41fe-8dd5-5fbf1d8955ba\"," +
        "    \"joins\": [" +
        "      {" +
        "        \"engineName\": \"productds\"," +
        "        \"id\": \"ba3257ee-fe53-4a42-885c-b3143eff02ac\"," +
        "        \"joinAlias\": \"join_1\"," +
        "        \"keyPair\": {" +
        "          \"product_id\": \"product_id\"" +
        "        }," +
        "        \"name\": \"productds\"," +
        "        \"type\": \"LEFT_OUTER\"" +
        "      }" +
        "    ]," +
        "    \"name\": \"productsalesds\"," +
        "    \"temporary\": false," +
        "    \"type\": \"mapping\"," +
        "    \"uiDescription\": \"\"" +
        "  }," +
        "  \"filters\": [" +
        "    {" +
        "      \"dataSource\": \"productsalesds\"," +
        "      \"field\": \"time\"," +
        "      \"ref\": \"productsalesds\"," +
        "      \"type\": \"time_all\"" +
        "    }," +
        "    {" +
        "      \"candidateValues\": []," +
        "      \"dataSource\": \"productsalesds\"," +
        "      \"definedValues\": []," +
        "      \"field\": \"name\"," +
        "      \"preFilters\": [" +
        "        {" +
        "          \"aggregation\": \"SUM\"," +
        "          \"inequality\": \"EQUAL_TO\"," +
        "          \"type\": \"measure_inequality\"," +
        "          \"ui\": {" +
        "            \"importanceType\": \"general\"" +
        "          }," +
        "          \"value\": 10" +
        "        }," +
        "        {" +
        "          \"aggregation\": \"SUM\"," +
        "          \"position\": \"TOP\"," +
        "          \"type\": \"measure_position\"," +
        "          \"ui\": {" +
        "            \"importanceType\": \"general\"" +
        "          }," +
        "          \"value\": 10" +
        "        }," +
        "        {" +
        "          \"contains\": \"AFTER\"," +
        "          \"field\": \"name\"," +
        "          \"type\": \"wildcard\"," +
        "          \"ui\": {" +
        "            \"importanceType\": \"general\"" +
        "          }," +
        "          \"value\": \"\"" +
        "        }" +
        "      ]," +
        "      \"ref\": \"productds\"," +
        "      \"selector\": \"SINGLE_COMBO\"," +
        "      \"sort\": {" +
        "        \"by\": \"TEXT\"," +
        "        \"direction\": \"ASC\"" +
        "      }," +
        "      \"type\": \"include\"," +
        "      \"valueList\": []" +
        "    }" +
        "  ]," +
        "  \"options\": {" +
        "    \"layout\": {" +
        "      \"layoutType\": \"FIT_TO_SCREEN\"," +
        "      \"widgetPadding\": 5" +
        "    }," +
        "    \"widget\": {" +
        "      \"showLegend\": \"BY_WIDGET\"," +
        "      \"showMinimap\": \"BY_WIDGET\"," +
        "      \"showTitle\": \"BY_WIDGET\"" +
        "    }" +
        "  }," +
        "  \"widgets\": []" +
        "}";
  }

  @Test
  public void changeDataSource_when_datasource_type_mapping_multi_join() {
    // given
    DashBoard dashBoard = new DashBoard();
    dashBoard.setId(UUID.randomUUID().toString());
    dashBoard.setConfiguration(configurationMappingDatasourceMultiJoin());

    Set dataSourcesInDashboard = new HashSet<DataSource>();

    DataSource productSalesDs = new DataSource();
    productSalesDs.setId("08572bd4-5b43-41fe-8dd5-5fbf1d8955ba");
    productSalesDs.setName("product-sales-ds");
    productSalesDs.setEngineName("productsalesds");
    dataSourcesInDashboard.add(productSalesDs);

    DataSource productDs = new DataSource();
    productDs.setId("ba3257ee-fe53-4a42-885c-b3143eff02ac");
    productDs.setName("product-ds");
    productDs.setEngineName("productds");
    dataSourcesInDashboard.add(productDs);

    DataSource testDs = new DataSource();
    testDs.setId("e9f2fb63-6cb7-4445-9b8b-5d0bafc9c0d6");
    testDs.setName("test-ds");
    testDs.setEngineName("testds");
    dataSourcesInDashboard.add(testDs);

    DataSource abcDs = new DataSource();
    abcDs.setId("937f9d4a-5b83-48e8-b528-1dcf5724c960");
    abcDs.setName("abc-ds");
    abcDs.setEngineName("abcds");
    dataSourcesInDashboard.add(abcDs);

    dashBoard.setDataSources(dataSourcesInDashboard);

    // when
    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    dashBoard.changeDataSource(testDs, changingDs);

    // then
    assertThat(dashBoard.getDataSources()).hasSize(4);
    assertThat(dashBoard.getDataSources())
        .contains(changingDs, productSalesDs, productDs, abcDs)
        .doesNotContain(testDs);

    Map<String, Object> configuration = dashBoard.getConfigurationToMap();
    Map<String, Object> dataSource = (Map<String, Object>)configuration.get("dataSource");
    Map<String, Object> joinProduct = ((List<Map<String, Object>>) dataSource.get("joins")).stream()
        .filter(join -> join.get("id").equals(productDs.getId()))
        .findFirst().get();
    Map<String, Object> join = (Map<String, Object>)joinProduct.get("join");
    assertThat(join.get("id")).isEqualTo(changingDs.getId());
    assertThat(join.get("engineName")).isEqualTo(changingDs.getEngineName());
    assertThat(join.get("name")).isEqualTo(changingDs.getEngineName());
  }

  public String configurationMappingDatasourceMultiJoin() {
    return "{" +
        "  \"dataSource\": {" +
        "    \"id\": \"08572bd4-5b43-41fe-8dd5-5fbf1d8955ba\"," +
        "    \"joins\": [" +
        "      {" +
        "        \"id\": \"ba3257ee-fe53-4a42-885c-b3143eff02ac\"," +
        "        \"join\": {" +
        "          \"id\": \"e9f2fb63-6cb7-4445-9b8b-5d0bafc9c0d6\"," +
        "          \"joinAlias\": \"join_1_1\"," +
        "          \"keyPair\": {" +
        "            \"product_id\": \"d\"" +
        "          }," +
        "          \"name\": \"testds\"," +
        "          \"type\": \"LEFT_OUTER\"" +
        "        }," +
        "        \"joinAlias\": \"join_1\"," +
        "        \"keyPair\": {" +
        "          \"product_id\": \"product_id\"" +
        "        }," +
        "        \"name\": \"productds\"," +
        "        \"type\": \"LEFT_OUTER\"" +
        "      }," +
        "      {" +
        "        \"id\": \"937f9d4a-5b83-48e8-b528-1dcf5724c960\"," +
        "        \"joinAlias\": \"join_2\"," +
        "        \"keyPair\": {" +
        "          \"order_id\": \"d\"" +
        "        }," +
        "        \"name\": \"abcds\"," +
        "        \"type\": \"LEFT_OUTER\"" +
        "      }" +
        "    ]," +
        "    \"name\": \"product-sales-ds\"," +
        "    \"temporary\": false," +
        "    \"type\": \"mapping\"," +
        "    \"uiDescription\": \"\"" +
        "  }," +
        "  \"options\": {" +
        "    \"layout\": {" +
        "      \"layoutType\": \"FIT_TO_SCREEN\"," +
        "      \"widgetPadding\": 5" +
        "    }," +
        "    \"widget\": {" +
        "      \"showLegend\": \"BY_WIDGET\"," +
        "      \"showMinimap\": \"BY_WIDGET\"," +
        "      \"showTitle\": \"BY_WIDGET\"" +
        "    }" +
        "  }" +
        "}";
  }
}