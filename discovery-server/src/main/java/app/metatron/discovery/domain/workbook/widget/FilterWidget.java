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

import app.metatron.discovery.domain.datasource.DataSource;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import app.metatron.discovery.common.GlobalObjectMapper;
import app.metatron.discovery.domain.workbook.DashBoard;
import app.metatron.discovery.domain.workbook.configurations.WidgetConfiguration;
import app.metatron.discovery.domain.workbook.configurations.widget.FilterWidgetConfiguration;
import app.metatron.discovery.util.PolarisUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by kyungtaak on 2017. 7. 18..
 */
@Entity
@JsonTypeName("filter")
@DiscriminatorValue("filter")
public class FilterWidget extends Widget {

  /**
   * Default Constructor
   */
  public FilterWidget() {
    // Empty Constructor
  }

  @Override
  public Widget copyOf(DashBoard parent, boolean addPrefix) {
    FilterWidget filterWidget = new FilterWidget();
    filterWidget.setName(addPrefix ? PolarisUtils.COPY_OF_PREFIX + name : name);
    filterWidget.setDescription(description);
    filterWidget.setConfiguration(configuration);

    if(parent == null) {
      filterWidget.setDashBoard(dashBoard);
    } else {
      filterWidget.setDashBoard(parent);
    }

    return filterWidget;
  }

  @Override
  public WidgetConfiguration convertConfiguration() {
    return GlobalObjectMapper.readValue(this.configuration, FilterWidgetConfiguration.class);
  }

  @Override
  public void changeDataSource(DataSource fromDataSource, DataSource toDataSource) {
    Map<String, Object> widgetConfiguration = this.getConfigurationToMap();

    if(MapUtils.isNotEmpty(widgetConfiguration)) {
      Map<String, Object> filter = (Map<String, Object>)widgetConfiguration.get("filter");
      if(MapUtils.isNotEmpty(filter)) {
        if(StringUtils.equals((String)filter.get("dataSource"), fromDataSource.getEngineName())) {
          filter.put("dataSource", toDataSource.getEngineName());
        }

        if(StringUtils.equals((String)filter.get("ref"), fromDataSource.getEngineName())) {
          filter.put("ref", toDataSource.getEngineName());
        }

        this.setConfigurationFromMap(widgetConfiguration);
      }
    }
  }
}
