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

import {Component, ElementRef, Injector, OnDestroy, OnInit} from '@angular/core';
import {AbstractComponent} from '../../../common/component/abstract.component';
import {WorkspaceService} from "../../../workspace/service/workspace.service";
import {WorkbookService} from "../../service/workbook.service";
import {Alert} from "../../../common/util/alert.util";

@Component({
  selector: 'app-change-datasource',
  templateUrl: './change-datasource.component.html',
})
export class ChangeDatasourceComponent extends AbstractComponent implements OnInit, OnDestroy {

  // 모달 flag
  public isShow = false;

  public datasourcesInWorkbook : any = [];
  public availableDatasources : any = [];

  public defaultIndex : number = -1;

  private changeFromDataSourceId: string;
  private changeToDataSourceId: string;

  private workbookId: string;


  // 생성자
  constructor(private workspaceService: WorkspaceService,
              private workbookService: WorkbookService,
              protected elementRef: ElementRef,
              protected injector: Injector) {
    super(elementRef, injector);
  }

  /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
   | Override Method
   |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

  // Init
  public ngOnInit() {
    // Init
    super.ngOnInit();
  }

  // Destory
  public ngOnDestroy() {

    // Destory
    super.ngOnDestroy();
  }

  public init(datasources: any[], workspaceId: string, workbookId: string) {
    this.datasourcesInWorkbook = datasources.map(datasource => {
      return {
        label: datasource.name,
        value: datasource.id
      }
    });

    this.workspaceService.getDataSources(workspaceId, {page: 0, size: 100, sort: 'name,asc'}).then((response) => {
      // TODO 상태가 enable이고 connType 이 Engine 인것만...
      this.availableDatasources = response._embedded.datasources.map(datasource => {
        return {
          label: datasource.name,
          value: datasource.id
        }
      });

    }).catch((error) => {
      console.log(error);
    });

    this.workbookId = workbookId;
    this.isShow = true;
  }

  // 닫기
  public close() {
    this.isShow = false;
  }

  public onChangeDataSourceInWorkbook(selectedDataSource: any) {
    this.changeFromDataSourceId = selectedDataSource.value;
  }

  public onChangeAvailableDatasource(selectedDataSource: any) {
    this.changeToDataSourceId = selectedDataSource.value;
  }

  public changeDataSource() {
    // TODO validation
    // 선택한 데이터 소스가 같은 경우 오류...
    this.loadingShow();

    // workbook service 호출 하여 데이터 소스 변경 ...
    this.workbookService.changeDataSource(this.workbookId, this.changeFromDataSourceId, this.changeToDataSourceId)
      .then((response) => {
        this.loadingHide();
        // Alert.success(`'${result.name}’ ` + this.translateService.instant('msg.book.alert.create.workbook.success'));
        Alert.success('데이터 소스 변경 성공');

        this.close();
      }).catch((error) => {
        this.loadingHide();
        console.log(error);
        if(error.code) {
          switch (error.code) {
            case "DSI0001":
              // Alert.error(this.translateService.instant('msg.book.alert.create.workbook.fail'));
              Alert.error("데이터소스가 사용 가능한 상태가 아닙니다.");
              break;
            case "DSI0002":
              Alert.error("데이터소스 타입이 엔진이어야 합니다.");
              break;
            case "DSI0003":
              Alert.error("데이터소스 GRANULARITY가 일치하지 않습니다.");
              break;
            case "DSI0004":
              Alert.error("데이터 소스 필드가 호환 되지 않습니다.");
              break;
            default:
              this.commonExceptionHandler(error);
          }
        } else {
          this.commonExceptionHandler(error);
        }
    });

  }
}
