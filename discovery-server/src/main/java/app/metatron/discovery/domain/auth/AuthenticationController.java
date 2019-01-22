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

package app.metatron.discovery.domain.auth;

import app.metatron.discovery.domain.user.UserRepository;
import app.metatron.discovery.domain.user.group.Group;
import app.metatron.discovery.domain.user.group.GroupMember;
import app.metatron.discovery.domain.user.group.GroupService;
import app.metatron.discovery.domain.user.role.RoleService;
import app.metatron.discovery.domain.workspace.WorkspaceService;
import com.google.common.collect.Maps;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import app.metatron.discovery.common.saml.SAMLAuthenticationInfo;
import app.metatron.discovery.domain.user.CachedUserService;
import app.metatron.discovery.domain.user.User;
import app.metatron.discovery.domain.user.role.Permission;
import app.metatron.discovery.util.AuthUtils;

/**
 * Created by kyungtaak on 2017. 6. 20..
 */
@RestController
@RequestMapping("/api")
public class AuthenticationController {

  private static Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  ApplicationContext context;

  @Autowired
  CachedUserService userService;

  @Autowired
  DefaultTokenServices defaultTokenServices;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleService roleService;

  @Autowired
  GroupService groupService;

  @Autowired
  WorkspaceService workspaceService;

  @RequestMapping(value = "/auth/{domain}/permissions", method = RequestMethod.GET)
  public ResponseEntity<Object> getPermissions(@PathVariable String domain) {

    if(!"SYSTEM".equals(domain.toUpperCase())) {
      throw new IllegalArgumentException("invalid domain name : " + domain);
    }

    List<String> perms = AuthUtils.getPermissions();
    LOGGER.debug("User permissions : " + perms);

    return ResponseEntity.ok(perms);
  }

  @RequestMapping(value = "/auth/{domain}/permissions/check", method = RequestMethod.GET)
  public ResponseEntity<Object> checkPermissions(@PathVariable String domain,
                                          @RequestParam List<String> permissions) {

    if(!"SYSTEM".equals(domain.toUpperCase())) {
      throw new IllegalArgumentException("invalid domain name : " + domain);
    }

    Map<String, Object> result = Maps.newHashMap();
    if(CollectionUtils.isNotEmpty(permissions)) {
      List<String> userPerms = AuthUtils.getPermissions();
      result.put("hasPermission", CollectionUtils.containsAll(userPerms, permissions));
    } else {
      result.put("hasPermission", false);
    }

    return ResponseEntity.ok(result);
  }

  @RequestMapping(value = "/auth/autologin", method = RequestMethod.GET)
  public void goTohome(@RequestParam String username, @RequestParam String password,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      authenticateUser(username, password, request);
    } catch (Exception e) {
      e.printStackTrace();
    }
    response.sendRedirect("/");
  }

  private void authenticateUser(String username, String password, HttpServletRequest request) {
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
    HttpSession session = request.getSession();
    authToken.setDetails(new WebAuthenticationDetails(request));
    Authentication authentication = authenticationManager.authenticate(authToken);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // creates context for that session.
    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

    //set necessary details in session
    session.setAttribute("username", username);
    session.setAttribute("authorities", authentication.getAuthorities());
  }

  @RequestMapping(value = "/sso", method = RequestMethod.POST)
  public ResponseEntity<?> setSSO(@RequestParam("token") String token, @RequestParam("type") String type,
                             @RequestParam("refreshToken") String refreshToken, @RequestParam("userId") String userId,
                             @RequestParam("forwardUrl") String forwardUrl, HttpServletResponse response) {

    Cookie cookie = new Cookie("LOGIN_TOKEN", token);
    cookie.setPath("/");
    cookie.setMaxAge(60*60*24) ;
    response.addCookie(cookie);

    cookie = new Cookie("LOGIN_TOKEN_TYPE", type);
    cookie.setPath("/");
    cookie.setMaxAge(60*60*24) ;
    response.addCookie(cookie);

    cookie = new Cookie("REFRESH_LOGIN_TOKEN", refreshToken);
    cookie.setPath("/");
    cookie.setMaxAge(60*60*24);
    response.addCookie(cookie);

    cookie = new Cookie("LOGIN_USER_ID", userId);
    cookie.setPath("/");
    cookie.setMaxAge(60*60*24) ;
    response.addCookie(cookie);

    User user = userService.findUser(userId);
    Set<Permission> perms =  user.getPermissions();
    List<String> arrPerm = new ArrayList<>();
    if( null != perms ) {
        perms.stream().forEach( item -> {
          if(Permission.DomainType.SYSTEM == item.getDomain()) {
              arrPerm.add( item.getName().toUpperCase() );
          }
        });
    }
    cookie = new Cookie( "PERMISSION", String.join( "==", arrPerm ) );
    cookie.setPath("/");
    cookie.setMaxAge(60*60*24) ;
    response.addCookie(cookie);

    response.setHeader("P3P","CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"");

    if(StringUtils.isNotEmpty(forwardUrl)){
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.LOCATION, forwardUrl);
      return new ResponseEntity<String>(headers, HttpStatus.FOUND);
    }

    return ResponseEntity.ok().build();
  }

  @RequestMapping(value = "/saml/auth", method = RequestMethod.GET)
  public ResponseEntity<?> getAuthentication() throws MessageEncodingException{
    //현재 Auth 정보
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = securityContext.getAuthentication();
    if(authentication.getCredentials() instanceof SAMLCredential){
      SAMLAuthenticationInfo samlAuthentication = new SAMLAuthenticationInfo(authentication);
      return ResponseEntity.ok(samlAuthentication);
    } else {
      return ResponseEntity.ok(AuthUtils.getAuthentication());
    }
  }

  @RequestMapping(value = "/saml/idp", method = RequestMethod.GET)
  public ResponseEntity<?> getIdp() {
    MetadataManager metadataManager = context.getBean(MetadataManager.class);
    if(metadataManager != null){
      //IDP List 반환
      Set<String> idps = metadataManager.getIDPEntityNames();
      return ResponseEntity.ok(idps);
    }

    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/oauth/proxy-user", method = RequestMethod.GET)
  public ResponseEntity<OAuth2AccessToken> authProxyUser(@RequestParam String username) {
    // user 관리 권한이 있는 사용자에게만 허용
    if(AuthUtils.getPermissions().contains("PERM_SYSTEM_MANAGE_USER")) {

      User user = userRepository.findByUsername(username);
      if(user == null) {
        // 미 가입된 사용자
        user = new User(username, UUID.randomUUID().toString());
        user.setFullName(username);
        user.setStatus(User.Status.ACTIVATED);

        // 기본 그룹에 포함
        Group defaultGroup = groupService.getDefaultGroup();
        if (defaultGroup == null) {
          LOGGER.warn("Default group not found.");
        } else {
          defaultGroup.addGroupMember(new GroupMember(user.getUsername(), user.getFullName()));
        }

        // 워크스페이스 생성(등록된 워크스페이스가 없을 경우 생성)
        workspaceService.createWorkspaceByUserCreation(user, false);
        userRepository.save(user);
      }
      user.setRoleService(roleService);

      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, "", user.getAuthorities());
      HashMap<String, String> authorizationParameters = new HashMap<String, String>();
      authorizationParameters.put("scope", "read");
      authorizationParameters.put("username", "user");
      authorizationParameters.put("client_id", "client_id");
      authorizationParameters.put("grant", "password");

      Set<String> responseType = new HashSet<String>();
      responseType.add("password");

      Set<String> scopes = new HashSet<String>();
      scopes.add("read");
      scopes.add("write");

      OAuth2Request authorizationRequest = new OAuth2Request(
          authorizationParameters, "polaris_trusted",
          user.getAuthorities(), true, scopes, null, "",
          responseType, null);

      OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(authorizationRequest, authToken);
      oAuth2Authentication.setAuthenticated(true);

      OAuth2AccessToken oAuth2AccessToken = defaultTokenServices.createAccessToken(oAuth2Authentication);

      return ResponseEntity.ok(oAuth2AccessToken);

    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }
}
