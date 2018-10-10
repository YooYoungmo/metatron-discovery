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

package app.metatron.discovery.domain.workbook.widget;

import app.metatron.discovery.common.GlobalObjectMapper;
import app.metatron.discovery.domain.datasource.DataSource;
import app.metatron.discovery.domain.workbook.DashBoard;
import app.metatron.discovery.domain.workbook.configurations.WidgetConfiguration;
import app.metatron.discovery.domain.workbook.configurations.widget.PageWidgetConfiguration;
import app.metatron.discovery.util.PolarisUtils;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by kyungtaak on 2017. 7. 18..
 */
@Entity
@JsonTypeName("page")
@DiscriminatorValue("page")
public class PageWidget extends Widget {

  /**
   * Page Image Url
   */
  @Column(name = "page_image_Url")
  private String imageUrl;

  /**
   * Notebook Models related to widget
   */
//  @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
//  @JoinTable(name = "notebook_model_widget",
//      joinColumns = @JoinColumn(name = "wg_id", referencedColumnName = "id"),
//      inverseJoinColumns = @JoinColumn(name = "nbm_id", referencedColumnName = "id"))
//  @JsonBackReference
//  protected Set<NotebookModel> models;

  /**
   * Default Constructor
   */
  public PageWidget() {
    // Empty Constructor
  }

  @Override
  public Widget copyOf(DashBoard parent, boolean addPrefix) {
    PageWidget pageWidget = new PageWidget();
    pageWidget.setName(addPrefix ? PolarisUtils.COPY_OF_PREFIX + name : name);
    pageWidget.setDescription(description);
    pageWidget.setConfiguration(configuration);
    pageWidget.setImageUrl(imageUrl);

    if(parent == null) {
      pageWidget.setDashBoard(dashBoard);
    } else {
      pageWidget.setDashBoard(parent);
    }

    return pageWidget;
  }

  @Override
  public WidgetConfiguration convertConfiguration() {
    return GlobalObjectMapper.readValue(this.configuration, PageWidgetConfiguration.class);
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

//  public Set<NotebookModel> getModels() {
//    return models;
//  }
//
//  public void setModels(Set<NotebookModel> models) {
//    this.models = models;
//  }
//
//  public boolean hasModels() {
//    return CollectionUtils.isNotEmpty(this.models);
//  }
//
//  @JsonIgnore
//  public List<NotebookModelSummary> getModelSummarys() {
//    return this.models.stream()
//            .map((model) -> NotebookModelSummary.valueOf(model)).collect(Collectors.toList());
//  }


  @Override
  public void changeDataSource(DataSource fromDataSource, DataSource toDataSource) {
    Map<String, Object> widgetConfiguration = this.getConfigurationToMap();
    if(MapUtils.isNotEmpty(widgetConfiguration)) {
      Map<String, Object> dataSource = (Map<String, Object>)widgetConfiguration.get("dataSource");
      if(MapUtils.isNotEmpty(dataSource) && StringUtils.equals((String)dataSource.get("id"), fromDataSource.getId())) {
        dataSource.put("id", toDataSource.getId());
        dataSource.put("engineName", toDataSource.getEngineName());
        dataSource.put("name", toDataSource.getEngineName());
      }

      List<Map<String, Object>> joins = (List<Map<String, Object>>)dataSource.get("joins");
      Optional.ofNullable(joins).orElse(Collections.emptyList()).forEach(join -> {
        if(StringUtils.equals((String)join.get("id"), fromDataSource.getId())) {
          join.put("id", toDataSource.getId());
          join.put("engineName", toDataSource.getEngineName());
          join.put("name", toDataSource.getEngineName());
        }

        Map<String, Object> joinOfJoin = (Map<String, Object>)join.get("join");
        if(MapUtils.isNotEmpty(joinOfJoin)) {
          if(StringUtils.equals((String)joinOfJoin.get("id"), fromDataSource.getId())) {
            joinOfJoin.put("id", toDataSource.getId());
            joinOfJoin.put("engineName", toDataSource.getEngineName());
            joinOfJoin.put("name", toDataSource.getEngineName());
          }
        }
      });

      List<Map<String, Object>> fields = (List<Map<String, Object>>)widgetConfiguration.get("fields");
      Optional.ofNullable(fields).orElse(Collections.emptyList()).forEach(field -> {
        // TODO "expr": "SUMOF( \"productds.price\"  )", 고려 필요..
        if(StringUtils.equals((String)field.get("dataSource"), fromDataSource.getEngineName())) {
          field.put("dataSource", toDataSource.getEngineName());
        }
      });

      List<Map<String, Object>> filters = (List<Map<String, Object>>)widgetConfiguration.get("filters");
      Optional.ofNullable(filters).orElse(Collections.emptyList()).forEach(filter -> {
        if(StringUtils.equals((String)filter.get("dataSource"), fromDataSource.getEngineName())) {
          filter.put("dataSource", toDataSource.getEngineName());
        }
        if(StringUtils.equals((String)filter.get("ref"), fromDataSource.getEngineName())) {
          filter.put("ref", toDataSource.getEngineName());
        }
      });

      Map<String, Object> pivot = (Map<String, Object>) widgetConfiguration.get("pivot");
      if(MapUtils.isNotEmpty(pivot)) {
        changePivotElementInConfiguration((List<Map<String, Object>>)pivot.get("aggregations"), fromDataSource, toDataSource);
        changePivotElementInConfiguration((List<Map<String, Object>>)pivot.get("columns"), fromDataSource, toDataSource);
        changePivotElementInConfiguration((List<Map<String, Object>>)pivot.get("rows"), fromDataSource, toDataSource);
      }

      this.setConfigurationObject(widgetConfiguration);
    }
  }

  private void changePivotElementInConfiguration(List<Map<String, Object>> pivotElements, DataSource fromDataSource, DataSource toDataSource) {
    Optional.ofNullable(pivotElements).orElse(Collections.emptyList()).forEach(element -> {
      if(StringUtils.equals((String)element.get("ref"), fromDataSource.getEngineName())) {
        element.put("ref", toDataSource.getEngineName());
      }

      Map<String, Object> field = (Map<String, Object>) element.get("field");
      if(MapUtils.isNotEmpty(field)) {
        if(StringUtils.equals((String)field.get("dsId"), fromDataSource.getId())) {
          field.put("dsId", toDataSource.getId());
        }

        if(StringUtils.equals((String)field.get("dataSource"), fromDataSource.getEngineName())) {
          field.put("dataSource", toDataSource.getEngineName());
        }

        if(StringUtils.equals((String)field.get("ref"), fromDataSource.getEngineName())) {
          field.put("ref", toDataSource.getEngineName());
        }
      }
    });
  }
}
