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

import {Injectable, Injector} from '@angular/core';
import {AbstractService} from '../../../../common/service/abstract.service';
import {HttpHeaders} from '@angular/common/http';
import {CookieConstant} from '../../../../common/constant/cookie.constant';

@Injectable()
export class DataEncryptionDecryptionService extends AbstractService {
  constructor(protected injector: Injector) {
    super(injector);
  }

  public doIdentityVerification() {
    return this.post(this.API_URL + `idcube/imsi/identity-verification`, null);
  }

  public checkIdentityVerificationByAuthenticationNumber(identityVerificationId: string, receivedAuthNumber: string) {
    return this.get(this.API_URL + `idcube/imsi/identity-verification?identityVerificationId=${identityVerificationId}&receivedAuthenticationNumber=${receivedAuthNumber}`);
  }

  public encryptOrDecrypt(data: any) {
    return this.post(this.API_URL + `idcube/imsi/encryption-or-decryption`, data);
  }

  public downloadFile(params: any) {

    let headers = new HttpHeaders({
                                    'Accept': 'application/csv',
                                    'Content-Type': 'application/json',
                                    'Authorization': this.cookieService.get(CookieConstant.KEY.LOGIN_TOKEN_TYPE) + ' ' + this.cookieService.get(CookieConstant.KEY.LOGIN_TOKEN)
                                  });



    // return this.postBinary(this.API_URL + `idcube/imsi/encryption-or-decryption/download`, params);

    return this.http.post(this.API_URL + `idcube/imsi/encryption-or-decryption/download`, params,
                              {headers: headers, responseType: 'blob'}).toPromise();
  }

  public getMaxResultSize() {
    return this.get(this.API_URL + `idcube/imsi/max-result-size`);
  }
}