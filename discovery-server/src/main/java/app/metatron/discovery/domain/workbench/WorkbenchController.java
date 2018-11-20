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

package app.metatron.discovery.domain.workbench;

import app.metatron.discovery.common.exception.GlobalErrorCodes;
import app.metatron.discovery.common.exception.MetatronException;
import app.metatron.discovery.common.exception.ResourceNotFoundException;
import app.metatron.discovery.domain.datasource.connection.DataConnection;
import app.metatron.discovery.domain.datasource.connection.jdbc.HiveConnection;
import app.metatron.discovery.domain.workbench.dto.ImportFile;
import app.metatron.discovery.domain.workbench.hive.WorkbenchHiveService;
import app.metatron.discovery.domain.workbench.util.WorkbenchDataSource;
import app.metatron.discovery.domain.workbench.util.WorkbenchDataSourceUtils;
import app.metatron.discovery.domain.workspace.Workspace;
import app.metatron.discovery.util.HibernateUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RepositoryRestController
public class WorkbenchController {

  private static Logger LOGGER = LoggerFactory.getLogger(WorkbenchController.class);

  @Autowired
  WorkbenchRepository workbenchRepository;

  @Autowired
  PagedResourcesAssembler pagedResourcesAssembler;

  @Autowired
  WorkbenchService workbenchService;

  @Autowired
  WorkbenchHiveService workbenchHiveService;

  @RequestMapping(value = "/workbenchs/{id}/navigation", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ResponseEntity<?> listNavigation(@PathVariable("id") String id,
                                          @RequestParam(required=false) String namePattern,
                                          Pageable pageable,
                                          PersistentEntityResourceAssembler resourceAssembler) {

    Workbench workbench = workbenchRepository.findOne(id);
    if(workbench == null){
      throw new ResourceNotFoundException("Workbench(" + id + ")");
    }
    Workspace workspace = workbench.getWorkspace();
    if(workspace == null){
      throw new ResourceNotFoundException("Workspace");
    }
    Page<Workbench> workbenches = workbenchRepository.findByWorkspaceIdAndNameIgnoreCaseContaining(workspace.getId(),
            StringUtils.defaultString(namePattern, ""), pageable);
    return ResponseEntity.ok(this.pagedResourcesAssembler.toResource(workbenches, resourceAssembler));
  }

  @RequestMapping(value = "/workbenchs/connection", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ResponseEntity<?> currentConnection() {
    Map<String, WorkbenchDataSource> dataSourceMap = WorkbenchDataSourceUtils.getCurrentConnection();
    return ResponseEntity.ok(
            dataSourceMap.entrySet().stream()
                    .map(e -> e.getValue().toString())
                    .toArray());
  }

  @RequestMapping(value = "/workbenchdatasource", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ResponseEntity<?> workbenchdatasource() {
    Map<String, WorkbenchDataSource> dataSourceMap = WorkbenchDataSourceUtils.getCurrentConnection();
    return ResponseEntity.ok(
            dataSourceMap.entrySet().stream()
                    .map(e -> e.getValue().toString())
                    .toArray());
  }

  @RequestMapping(value = "/workbenchs/{id}/import", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> importFileToPersonalDatabase(@PathVariable("id") String id,
                                                        @RequestBody ImportFile importFile) throws IOException {

    Workbench workbench = workbenchRepository.findOne(id);

    DataConnection dataConnection = HibernateUtils.unproxy(workbench.getDataConnection());

    if((dataConnection instanceof HiveConnection) == false ||
        ((HiveConnection)dataConnection).isSupportSaveAsHive() == false) {
      throw new MetatronException(GlobalErrorCodes.BAD_REQUEST_CODE, "Only Hive is allowed.");
    }

    workbenchHiveService.importFileToPersonalDatabase((HiveConnection)dataConnection, importFile);

    return ResponseEntity.noContent().build();
  }
}
