package app.metatron.discovery.domain.workbook.widget;

import app.metatron.discovery.domain.datasource.DataSource;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PageWidgetTest {

  @Test
  public void changeDataSource_when_change_main_datasource() {
    // given
    Widget widget = new PageWidget();
    widget.setConfiguration(configuration());

    // when
    DataSource productSalesDs = new DataSource();
    productSalesDs.setId("08572bd4-5b43-41fe-8dd5-5fbf1d8955ba");
    productSalesDs.setName("product-sales-ds");
    productSalesDs.setEngineName("productsalesds");

    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    widget.changeDataSource(productSalesDs, changingDs);

    // then
    Map<String, Object> actualConfiguration = widget.getConfigurationToMap();

    Map<String, Object> dataSource = (Map<String, Object>)actualConfiguration.get("dataSource");
    assertThat(dataSource.get("id")).isEqualTo(changingDs.getId());
    assertThat(dataSource.get("name")).isEqualTo(changingDs.getEngineName());
    assertThat(dataSource.get("engineName")).isEqualTo(changingDs.getEngineName());

    List<Map<String, Object>> fields = (List<Map<String, Object>>)actualConfiguration.get("fields");
    assertThat(fields).hasSize(1);
    assertThat(fields).extracting("dataSource")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());

    List<Map<String, Object>> filters = (List<Map<String, Object>>)actualConfiguration.get("filters");
    assertThat(filters).hasSize(1);
    assertThat(filters).extracting("dataSource")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
    assertThat(filters).extracting("ref")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());

    Map<String, Object> pivot = (Map<String, Object>)actualConfiguration.get("pivot");

    List<Map<String, Object>> aggregations = (List<Map<String, Object>>)pivot.get("aggregations");
    assertThat(aggregations).hasSize(3);
    assertThat(aggregations).extracting("ref")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
    assertThat(aggregations).extracting("field").extracting("dsId")
        .contains(changingDs.getId())
        .doesNotContain(productSalesDs.getId());
    assertThat(aggregations).extracting("field").extracting("dataSource")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
    assertThat(aggregations).extracting("field").extracting("ref")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());

    List<Map<String, Object>> columns = (List<Map<String, Object>>)pivot.get("columns");
    assertThat(columns).hasSize(1);
    assertThat(columns).extracting("ref")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
    assertThat(columns).extracting("field").extracting("dsId")
        .contains(changingDs.getId())
        .doesNotContain(productSalesDs.getId());
    assertThat(columns).extracting("field").extracting("dataSource")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
    assertThat(columns).extracting("field").extracting("ref")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());

    List<Map<String, Object>> rows = (List<Map<String, Object>>)pivot.get("rows");
    assertThat(rows).hasSize(1);
    assertThat(rows).extracting("ref")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
    assertThat(rows).extracting("field").extracting("dsId")
        .contains(changingDs.getId())
        .doesNotContain(productSalesDs.getId());
    assertThat(rows).extracting("field").extracting("dataSource")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
    assertThat(rows).extracting("field").extracting("ref")
        .contains(changingDs.getEngineName())
        .doesNotContain(productSalesDs.getEngineName());
  }

  @Test
  public void changeDataSource_when_change_join_datasource() {
    // given
    Widget widget = new PageWidget();
    widget.setConfiguration(configuration());

    DataSource productSalesDs = new DataSource();
    productSalesDs.setId("08572bd4-5b43-41fe-8dd5-5fbf1d8955ba");
    productSalesDs.setName("product-sales-ds");
    productSalesDs.setEngineName("productsalesds");

    // when
    DataSource productDs = new DataSource();
    productDs.setId("ba3257ee-fe53-4a42-885c-b3143eff02ac");
    productDs.setName("product-ds");
    productDs.setEngineName("productds");

    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    widget.changeDataSource(productDs, changingDs);

    // then
    Map<String, Object> actualConfiguration = widget.getConfigurationToMap();

    Map<String, Object> dataSource = (Map<String, Object>)actualConfiguration.get("dataSource");
    assertThat(dataSource.get("id")).isEqualTo(productSalesDs.getId());
    assertThat(dataSource.get("name")).isEqualTo(productSalesDs.getEngineName());
    assertThat(dataSource.get("engineName")).isEqualTo(productSalesDs.getEngineName());
    List<Map<String, Object>> joins = (List<Map<String, Object>>)dataSource.get("joins");
    assertThat(joins).hasSize(1);
    assertThat(joins).extracting("id").contains(changingDs.getId()).doesNotContain(productDs.getId());
    assertThat(joins).extracting("engineName").contains(changingDs.getEngineName()).doesNotContain(productDs.getEngineName());
    assertThat(joins).extracting("name").contains(changingDs.getEngineName()).doesNotContain(productDs.getEngineName());

    Map<String, Object> pivot = (Map<String, Object>)actualConfiguration.get("pivot");
    List<Map<String, Object>> aggregations = (List<Map<String, Object>>)pivot.get("aggregations");
    assertThat(aggregations).hasSize(3);
    assertThat(aggregations).extracting("ref")
        .contains(changingDs.getEngineName())
        .doesNotContain(productDs.getEngineName());
    assertThat(aggregations).extracting("field").extracting("dsId")
        .contains(changingDs.getId())
        .doesNotContain(productDs.getId());
    assertThat(aggregations).extracting("field").extracting("ref")
        .contains(changingDs.getEngineName())
        .doesNotContain(productDs.getEngineName());
  }

  @Test
  public void changeDataSource_when_change_join_of_join_datasource() {
    // given
    Widget widget = new PageWidget();
    widget.setConfiguration(configuration());

    DataSource productSalesDs = new DataSource();
    productSalesDs.setId("08572bd4-5b43-41fe-8dd5-5fbf1d8955ba");
    productSalesDs.setName("product-sales-ds");
    productSalesDs.setEngineName("productsalesds");

    DataSource productDs = new DataSource();
    productDs.setId("ba3257ee-fe53-4a42-885c-b3143eff02ac");
    productDs.setName("product-ds");
    productDs.setEngineName("productds");

    // when
    DataSource testds = new DataSource();
    testds.setId("e9f2fb63-6cb7-4445-9b8b-5d0bafc9c0d6");
    testds.setName("test-ds");
    testds.setEngineName("testds");

    DataSource changingDs = new DataSource();
    changingDs.setId(UUID.randomUUID().toString());
    changingDs.setName("changing-ds");
    changingDs.setEngineName("changingds");

    widget.changeDataSource(testds, changingDs);

    // then
    Map<String, Object> actualConfiguration = widget.getConfigurationToMap();

    Map<String, Object> dataSource = (Map<String, Object>)actualConfiguration.get("dataSource");
    assertThat(dataSource.get("id")).isEqualTo(productSalesDs.getId());
    assertThat(dataSource.get("name")).isEqualTo(productSalesDs.getEngineName());
    assertThat(dataSource.get("engineName")).isEqualTo(productSalesDs.getEngineName());

    List<Map<String, Object>> joins = (List<Map<String, Object>>)dataSource.get("joins");
    assertThat(joins).hasSize(1);
    assertThat(joins).extracting("id").contains(productDs.getId());
    assertThat(joins).extracting("engineName").contains(productDs.getEngineName());
    assertThat(joins).extracting("name").contains(productDs.getEngineName());

    Map<String, Object> joinOfJoin = (Map<String, Object>)joins.get(0).get("join");
    assertThat(joinOfJoin.get("id")).isEqualTo(changingDs.getId());
    assertThat(joinOfJoin.get("engineName")).isEqualTo(changingDs.getEngineName());
    assertThat(joinOfJoin.get("name")).isEqualTo(changingDs.getEngineName());
  }

  private String configuration() {
    return "{\n" +
        "  \"chart\": {\n" +
        "    \"chartZooms\": [\n" +
        "      {\n" +
        "        \"auto\": true,\n" +
        "        \"end\": 100,\n" +
        "        \"endValue\": 2111,\n" +
        "        \"orient\": \"HORIZONTAL\",\n" +
        "        \"start\": 0,\n" +
        "        \"startValue\": 0\n" +
        "      },\n" +
        "      {\n" +
        "        \"auto\": true,\n" +
        "        \"end\": 100,\n" +
        "        \"endValue\": 5,\n" +
        "        \"orient\": \"VERTICAL\",\n" +
        "        \"start\": 0,\n" +
        "        \"startValue\": 0\n" +
        "      }\n" +
        "    ],\n" +
        "    \"color\": {\n" +
        "      \"schema\": \"SC1\",\n" +
        "      \"targetField\": \"order_id\",\n" +
        "      \"type\": \"dimension\"\n" +
        "    },\n" +
        "    \"dataLabel\": {\n" +
        "      \"showValue\": false\n" +
        "    },\n" +
        "    \"fontSize\": \"NORMAL\",\n" +
        "    \"legend\": {\n" +
        "      \"auto\": true,\n" +
        "      \"count\": 5,\n" +
        "      \"pos\": \"TOP\",\n" +
        "      \"textStyle\": {\n" +
        "        \"fontFamily\": \"SpoqaHanSans\",\n" +
        "        \"fontSize\": 13\n" +
        "      }\n" +
        "    },\n" +
        "    \"pointShape\": \"CIRCLE\",\n" +
        "    \"pointTransparency\": 1,\n" +
        "    \"type\": \"scatter\",\n" +
        "    \"valueFormat\": {\n" +
        "      \"decimal\": 2,\n" +
        "      \"each\": null,\n" +
        "      \"isAll\": true,\n" +
        "      \"sign\": \"KRW\",\n" +
        "      \"type\": \"number\",\n" +
        "      \"useThousandsSep\": true\n" +
        "    },\n" +
        "    \"version\": 2,\n" +
        "    \"xAxis\": {\n" +
        "      \"label\": {\n" +
        "        \"type\": \"value\",\n" +
        "        \"useDefault\": true\n" +
        "      },\n" +
        "      \"mode\": \"row\",\n" +
        "      \"showLabel\": true,\n" +
        "      \"showName\": true\n" +
        "    },\n" +
        "    \"yAxis\": {\n" +
        "      \"label\": {\n" +
        "        \"type\": \"value\",\n" +
        "        \"useDefault\": true\n" +
        "      },\n" +
        "      \"mode\": \"column\",\n" +
        "      \"showLabel\": true,\n" +
        "      \"showName\": true\n" +
        "    }\n" +
        "  },\n" +
        "  \"dataSource\": {\n" +
        "    \"connType\": \"ENGINE\",\n" +
        "    \"engineName\": \"productsalesds\",\n" +
        "    \"id\": \"08572bd4-5b43-41fe-8dd5-5fbf1d8955ba\",\n" +
        "    \"joins\": [\n" +
        "      {\n" +
        "        \"engineName\": \"productds\",\n" +
        "        \"id\": \"ba3257ee-fe53-4a42-885c-b3143eff02ac\",\n" +
        "        \"joinAlias\": \"join_1\",\n" +
        "        \"keyPair\": {\n" +
        "          \"product_id\": \"product_id\"\n" +
        "        },\n" +
        "        \"name\": \"productds\",\n" +
        "        \"type\": \"LEFT_OUTER\",\n" +
        "        \"join\": {\n" +
        "          \"id\": \"e9f2fb63-6cb7-4445-9b8b-5d0bafc9c0d6\",\n" +
        "          \"joinAlias\": \"join_1_1\",\n" +
        "          \"keyPair\": {\n" +
        "            \"product_id\": \"d\"\n" +
        "          },\n" +
        "          \"name\": \"testds\",\n" +
        "          \"type\": \"LEFT_OUTER\"\n" +
        "        }\n" +
        "      }\n" +
        "    ],\n" +
        "    \"name\": \"productsalesds\",\n" +
        "    \"temporary\": false,\n" +
        "    \"type\": \"mapping\",\n" +
        "    \"uiDescription\": \"\"\n" +
        "  },\n" +
        "  \"fields\": [\n" +
        "    {\n" +
        "      \"aggregated\": true,\n" +
        "      \"alias\": \"custabc\",\n" +
        "      \"dataSource\": \"productsalesds\",\n" +
        "      \"expr\": \"SUMOF( \\\"productds.price\\\"  )\",\n" +
        "      \"name\": \"custabc\",\n" +
        "      \"oriColumnName\": \"\",\n" +
        "      \"role\": \"MEASURE\",\n" +
        "      \"type\": \"user_expr\",\n" +
        "      \"useChart\": false,\n" +
        "      \"useChartFilter\": false,\n" +
        "      \"useFilter\": false\n" +
        "    }\n" +
        "  ],\n" +
        "  \"filters\": [\n" +
        "    {\n" +
        "      \"candidateValues\": [],\n" +
        "      \"dataSource\": \"productsalesds\",\n" +
        "      \"definedValues\": [],\n" +
        "      \"field\": \"product_id\",\n" +
        "      \"preFilters\": [\n" +
        "        {\n" +
        "          \"aggregation\": \"SUM\",\n" +
        "          \"inequality\": \"EQUAL_TO\",\n" +
        "          \"type\": \"measure_inequality\",\n" +
        "          \"ui\": {\n" +
        "            \"importanceType\": \"general\"\n" +
        "          },\n" +
        "          \"value\": 10\n" +
        "        },\n" +
        "        {\n" +
        "          \"aggregation\": \"SUM\",\n" +
        "          \"position\": \"TOP\",\n" +
        "          \"type\": \"measure_position\",\n" +
        "          \"ui\": {\n" +
        "            \"importanceType\": \"general\"\n" +
        "          },\n" +
        "          \"value\": 10\n" +
        "        },\n" +
        "        {\n" +
        "          \"contains\": \"AFTER\",\n" +
        "          \"field\": \"product_id\",\n" +
        "          \"type\": \"wildcard\",\n" +
        "          \"ui\": {\n" +
        "            \"importanceType\": \"general\"\n" +
        "          },\n" +
        "          \"value\": \"\"\n" +
        "        }\n" +
        "      ],\n" +
        "      \"ref\": \"productsalesds\",\n" +
        "      \"selector\": \"MULTI_COMBO\",\n" +
        "      \"sort\": {\n" +
        "        \"by\": \"TEXT\",\n" +
        "        \"direction\": \"ASC\"\n" +
        "      },\n" +
        "      \"type\": \"include\",\n" +
        "      \"valueList\": []\n" +
        "    }\n" +
        "  ],\n" +
        "  \"limit\": {\n" +
        "    \"limit\": 100000,\n" +
        "    \"sort\": []\n" +
        "  },\n" +
        "  \"pivot\": {\n" +
        "    \"aggregations\": [\n" +
        "      {\n" +
        "        \"currentPivot\": \"AGGREGATIONS\",\n" +
        "        \"field\": {\n" +
        "          \"aggrType\": \"NONE\",\n" +
        "          \"alias\": \"product_id\",\n" +
        "          \"biType\": \"DIMENSION\",\n" +
        "          \"boardId\": \"c374c599-16dc-49c4-8fe2-38bbc321e9c3\",\n" +
        "          \"dataSource\": \"productsalesds\",\n" +
        "          \"dsId\": \"08572bd4-5b43-41fe-8dd5-5fbf1d8955ba\",\n" +
        "          \"granularity\": \"DAY\",\n" +
        "          \"id\": 10037067,\n" +
        "          \"logicalType\": \"STRING\",\n" +
        "          \"name\": \"product_id\",\n" +
        "          \"pivot\": [\n" +
        "            \"AGGREGATIONS\"\n" +
        "          ],\n" +
        "          \"ref\": \"productsalesds\",\n" +
        "          \"role\": \"DIMENSION\",\n" +
        "          \"segGranularity\": \"MONTH\",\n" +
        "          \"seq\": 3,\n" +
        "          \"type\": \"STRING\",\n" +
        "          \"useFilter\": false\n" +
        "        },\n" +
        "        \"granularity\": \"DAY\",\n" +
        "        \"name\": \"product_id\",\n" +
        "        \"ref\": \"productsalesds\",\n" +
        "        \"segGranularity\": \"MONTH\",\n" +
        "        \"subRole\": \"DIMENSION\",\n" +
        "        \"subType\": \"STRING\",\n" +
        "        \"type\": \"dimension\"\n" +
        "      },\n" +
        "      {\n" +
        "        \"currentPivot\": \"AGGREGATIONS\",\n" +
        "        \"field\": {\n" +
        "          \"aggrType\": \"NONE\",\n" +
        "          \"alias\": \"name\",\n" +
        "          \"biType\": \"DIMENSION\",\n" +
        "          \"dataSource\": \"productsalesds\",\n" +
        "          \"dsId\": \"ba3257ee-fe53-4a42-885c-b3143eff02ac\",\n" +
        "          \"granularity\": \"DAY\",\n" +
        "          \"id\": 10037070,\n" +
        "          \"logicalType\": \"STRING\",\n" +
        "          \"name\": \"name\",\n" +
        "          \"pivot\": [\n" +
        "            \"AGGREGATIONS\"\n" +
        "          ],\n" +
        "          \"ref\": \"productds\",\n" +
        "          \"role\": \"DIMENSION\",\n" +
        "          \"segGranularity\": \"MONTH\",\n" +
        "          \"seq\": 1,\n" +
        "          \"type\": \"STRING\",\n" +
        "          \"useFilter\": true\n" +
        "        },\n" +
        "        \"granularity\": \"DAY\",\n" +
        "        \"name\": \"name\",\n" +
        "        \"ref\": \"productds\",\n" +
        "        \"segGranularity\": \"MONTH\",\n" +
        "        \"subRole\": \"DIMENSION\",\n" +
        "        \"subType\": \"STRING\",\n" +
        "        \"type\": \"dimension\"\n" +
        "      },\n" +
        "      {\n" +
        "        \"currentPivot\": \"AGGREGATIONS\",\n" +
        "        \"field\": {\n" +
        "          \"aggrType\": \"NONE\",\n" +
        "          \"alias\": \"order_id\",\n" +
        "          \"biType\": \"DIMENSION\",\n" +
        "          \"boardId\": \"c374c599-16dc-49c4-8fe2-38bbc321e9c3\",\n" +
        "          \"dataSource\": \"productsalesds\",\n" +
        "          \"dsId\": \"08572bd4-5b43-41fe-8dd5-5fbf1d8955ba\",\n" +
        "          \"granularity\": \"DAY\",\n" +
        "          \"id\": 10037065,\n" +
        "          \"logicalType\": \"STRING\",\n" +
        "          \"name\": \"order_id\",\n" +
        "          \"pivot\": [\n" +
        "            \"AGGREGATIONS\"\n" +
        "          ],\n" +
        "          \"ref\": \"productsalesds\",\n" +
        "          \"role\": \"DIMENSION\",\n" +
        "          \"segGranularity\": \"MONTH\",\n" +
        "          \"seq\": 1,\n" +
        "          \"type\": \"STRING\",\n" +
        "          \"useFilter\": false\n" +
        "        },\n" +
        "        \"granularity\": \"DAY\",\n" +
        "        \"name\": \"order_id\",\n" +
        "        \"ref\": \"productsalesds\",\n" +
        "        \"segGranularity\": \"MONTH\",\n" +
        "        \"subRole\": \"DIMENSION\",\n" +
        "        \"subType\": \"STRING\",\n" +
        "        \"type\": \"dimension\"\n" +
        "      }\n" +
        "    ],\n" +
        "    \"columns\": [\n" +
        "      {\n" +
        "        \"aggregationType\": \"SUM\",\n" +
        "        \"currentPivot\": \"COLUMNS\",\n" +
        "        \"field\": {\n" +
        "          \"aggrType\": \"NONE\",\n" +
        "          \"alias\": \"amount\",\n" +
        "          \"biType\": \"MEASURE\",\n" +
        "          \"boardId\": \"c374c599-16dc-49c4-8fe2-38bbc321e9c3\",\n" +
        "          \"dataSource\": \"productsalesds\",\n" +
        "          \"dsId\": \"08572bd4-5b43-41fe-8dd5-5fbf1d8955ba\",\n" +
        "          \"granularity\": \"DAY\",\n" +
        "          \"id\": 10037066,\n" +
        "          \"logicalType\": \"INTEGER\",\n" +
        "          \"name\": \"amount\",\n" +
        "          \"pivot\": [\n" +
        "            \"COLUMNS\"\n" +
        "          ],\n" +
        "          \"ref\": \"productsalesds\",\n" +
        "          \"role\": \"MEASURE\",\n" +
        "          \"segGranularity\": \"MONTH\",\n" +
        "          \"seq\": 2,\n" +
        "          \"type\": \"STRING\",\n" +
        "          \"useFilter\": false\n" +
        "        },\n" +
        "        \"format\": {\n" +
        "          \"abbr\": \"NONE\",\n" +
        "          \"customSymbol\": null,\n" +
        "          \"decimal\": 2,\n" +
        "          \"sign\": \"KRW\",\n" +
        "          \"type\": \"number\",\n" +
        "          \"useThousandsSep\": true\n" +
        "        },\n" +
        "        \"granularity\": \"DAY\",\n" +
        "        \"name\": \"amount\",\n" +
        "        \"ref\": \"productsalesds\",\n" +
        "        \"segGranularity\": \"MONTH\",\n" +
        "        \"subRole\": \"MEASURE\",\n" +
        "        \"subType\": \"STRING\",\n" +
        "        \"type\": \"measure\"\n" +
        "      }\n" +
        "    ],\n" +
        "    \"rows\": [\n" +
        "      {\n" +
        "        \"aggregationType\": \"SUM\",\n" +
        "        \"currentPivot\": \"ROWS\",\n" +
        "        \"field\": {\n" +
        "          \"aggrType\": \"NONE\",\n" +
        "          \"alias\": \"sale_count\",\n" +
        "          \"biType\": \"MEASURE\",\n" +
        "          \"boardId\": \"c374c599-16dc-49c4-8fe2-38bbc321e9c3\",\n" +
        "          \"dataSource\": \"productsalesds\",\n" +
        "          \"dsId\": \"08572bd4-5b43-41fe-8dd5-5fbf1d8955ba\",\n" +
        "          \"granularity\": \"DAY\",\n" +
        "          \"id\": 10037068,\n" +
        "          \"logicalType\": \"INTEGER\",\n" +
        "          \"name\": \"sale_count\",\n" +
        "          \"pivot\": [\n" +
        "            \"ROWS\"\n" +
        "          ],\n" +
        "          \"ref\": \"productsalesds\",\n" +
        "          \"role\": \"MEASURE\",\n" +
        "          \"segGranularity\": \"MONTH\",\n" +
        "          \"seq\": 4,\n" +
        "          \"type\": \"STRING\",\n" +
        "          \"useFilter\": false\n" +
        "        },\n" +
        "        \"format\": {\n" +
        "          \"abbr\": \"NONE\",\n" +
        "          \"customSymbol\": null,\n" +
        "          \"decimal\": 2,\n" +
        "          \"sign\": \"KRW\",\n" +
        "          \"type\": \"number\",\n" +
        "          \"useThousandsSep\": true\n" +
        "        },\n" +
        "        \"granularity\": \"DAY\",\n" +
        "        \"name\": \"sale_count\",\n" +
        "        \"ref\": \"productsalesds\",\n" +
        "        \"segGranularity\": \"MONTH\",\n" +
        "        \"subRole\": \"MEASURE\",\n" +
        "        \"subType\": \"STRING\",\n" +
        "        \"type\": \"measure\"\n" +
        "      }\n" +
        "    ]\n" +
        "  },\n" +
        "  \"type\": \"page\"\n" +
        "}";
  }
}