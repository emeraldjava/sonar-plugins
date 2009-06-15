/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.jira;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.IssueManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.api.maven.MavenCollector;
import org.sonar.plugins.api.maven.MavenPluginHandler;
import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.api.maven.model.MavenPom;
import org.sonar.plugins.api.measures.PropertiesBuilder;
import org.sonar.plugins.jira.client.JiraSoapService;
import org.sonar.plugins.jira.client.JiraSoapServiceServiceLocator;
import org.sonar.plugins.jira.client.RemoteAuthenticationException;
import org.sonar.plugins.jira.client.RemoteComponent;
import org.sonar.plugins.jira.client.RemoteException;
import org.sonar.plugins.jira.client.RemoteFilter;
import org.sonar.plugins.jira.client.RemoteIssue;
import org.sonar.plugins.jira.client.RemotePermissionException;
import org.sonar.plugins.jira.client.RemoteVersion;

public class JiraMavenCollector implements MavenCollector {

  private static final Logger LOG = LoggerFactory.getLogger(JiraMavenCollector.class);
  
  private JiraEntitiesLabels labels;
  private String filterDescr;
  
  private int MAX_ISSUES_OUTSIDE_FILTER_SEARCH = 2500;

  public Class<? extends MavenPluginHandler> dependsOnMavenPlugin(MavenPom pom) {
    return null;
  }

  public boolean shouldCollectOn(MavenPom pom) {
    IssueManagement manag = pom.getMavenProject().getIssueManagement();
    if (manag != null) {
      return manag.getSystem().equalsIgnoreCase("jira") && isValidJiraURL(manag.getUrl()) && pom.isRoot();
    }
    return false;
  }
  
  private String[] getUsernameAndPass(MavenPom pom, String jiraManagmentURL) throws MalformedURLException {
    URL jiraServer = new URL(jiraManagmentURL);
    String serversUsersPass = (String)pom.getProperty(JiraPlugin.JIRA_USER_AND_PASS);
    if (!StringUtils.isEmpty(serversUsersPass)) {
      String[] keys = serversUsersPass.split(",");
      for (String serverUserPass : keys) {
        String[] serverUserPassDate = serverUserPass.split(";");
        String jiraDomain = serverUserPassDate[0];
        if (jiraDomain.equals(jiraServer.getHost())) {
          return new String[]{serverUserPassDate[1], serverUserPassDate[2]};
        }
      }
    }
    LOG.warn("Unable to find a user name and password for server domain {}, go to the sonar settings page to define it", jiraServer.getHost());
    return null;
  }
  
  private RemoteIssue[] getRemoteIssues(MavenPom pom, String jiraManagmentURL) throws MalformedURLException, ServiceException, RemoteException, java.rmi.RemoteException {
    RemoteIssue[] issues = new RemoteIssue[0];
    JiraSoapServiceServiceLocator locator = new JiraSoapServiceServiceLocator();
    JiraSoapService srv = locator.getJirasoapserviceV2(getJiraServiceUrl(jiraManagmentURL));
    String[] userNameAndPass = getUsernameAndPass(pom, jiraManagmentURL);
    if (userNameAndPass != null) {
      String userName = userNameAndPass[0];
      String token = srv.login(userName, userNameAndPass[1]);
      String jiraFilterName = "sonar-jira-plugin-" + getJiraProjectName(jiraManagmentURL);
      RemoteFilter filter = findRemoteFilter(jiraFilterName, srv, token);
      if (filter == null) {
        //LOG.warn("Unable to find remote filter {} for given user {}. " +
        //    "Please setup a filter with this given name and your criteria choices", jiraFilterName, userName);
        issues = srv.getIssuesFromTextSearchWithProject(token, new String[]{getJiraProjectName(jiraManagmentURL)}, "", MAX_ISSUES_OUTSIDE_FILTER_SEARCH);
      } else {
        long issuesCount = srv.getIssueCountForFilter(token, filter.getId());
        issues = srv.getIssuesFromFilterWithLimit(token, filter.getId(), 0, (int)issuesCount);
      }
      
      labels = new JiraEntitiesLabels(srv.getIssueTypes(token), srv.getStatuses(token), srv.getResolutions(token), srv.getPriorities(token));
      srv.logout(token);
    }
    return issues;
  }

  private RemoteFilter findRemoteFilter(String jiraFilterName, JiraSoapService srv, String token) throws java.rmi.RemoteException,
      RemotePermissionException, RemoteAuthenticationException, RemoteException {
    RemoteFilter filter = null;
    RemoteFilter[] filters = srv.getFavouriteFilters(token);
    for (RemoteFilter remoteFilter : filters) {
      if (remoteFilter.getName().equalsIgnoreCase(jiraFilterName)) {
        filter = remoteFilter;
        filterDescr = filter.getDescription();
        break;
      }
    }
    return filter;
  }

  public void collect(MavenPom pom, ProjectContext context) {
    try {
      String jiraManagmentURL = pom.getMavenProject().getIssueManagement().getUrl();
      String targetComponent = (String)pom.getProperty(JiraPlugin.JIRA_COMPONENT_FILTER);
      String currentVersion = getVersion(pom);
      Map<String,Integer> issuesByPriority = new HashMap<String, Integer>();
      Map<String,Integer> issuesByStatus = new HashMap<String, Integer>();
      Map<String,Integer> issuesByType = new HashMap<String, Integer>();
      Map<String,Integer> issuesByResolution = new HashMap<String, Integer>();
      Map<String,Integer> issuesByVersion = new HashMap<String, Integer>();
      RemoteIssue[] remoteIssues = getRemoteIssues(pom, jiraManagmentURL);
      int openBugs = 0;
      for (RemoteIssue remoteIssue : remoteIssues) {
        
        if (filterTargetComponent(targetComponent, remoteIssue.getComponents())) {
          boolean versionMatch = remoteIssue.getAffectsVersions().length == 0;
          for (RemoteVersion remoteVersion : remoteIssue.getAffectsVersions()) {
            if (currentVersion.equals(remoteVersion)) {
              versionMatch = true;
            }
            incrementCountmap(remoteVersion.getName(), issuesByVersion);
          }
          if (!versionMatch) continue;
          incrementCountmap(labels.getPriorityLabel(remoteIssue.getPriority()), issuesByPriority);
          incrementCountmap(labels.getStatusLabel(remoteIssue.getStatus()), issuesByStatus);
          incrementCountmap(labels.getTypeLabel(remoteIssue.getType()), issuesByType);
          incrementCountmap(labels.getResolutionLabel(remoteIssue.getResolution()), issuesByResolution);
          // bug is issue type 1 and open is status type 1
          if (remoteIssue.getType().equals("1") && remoteIssue.getStatus().equals("1")) {
            openBugs++;
          }
        }
      }

      context.addMeasure(new PropertiesBuilder(JiraMetrics.ISSUES_VERSION, issuesByVersion).build());
      context.addMeasure(new PropertiesBuilder(JiraMetrics.ISSUES_PRIORITIES, issuesByPriority).build());
      context.addMeasure(new PropertiesBuilder(JiraMetrics.ISSUES_STATUS, issuesByStatus).build());
      context.addMeasure(new PropertiesBuilder(JiraMetrics.ISSUES_TYPE, issuesByType).build());
      context.addMeasure(new PropertiesBuilder(JiraMetrics.ISSUES_RESOLUTION, issuesByResolution).build());
      context.addMeasure(JiraMetrics.ISSUES_COUNT, new Double(remoteIssues.length));
      context.addMeasure(JiraMetrics.OPEN_BUGS_COUNT, new Double(openBugs));
      
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
  
  private String getVersion(MavenPom pom) {
    String version = pom.getMavenProject().getVersion();
    return version.toLowerCase().replace("-SNAPSHOT", "");
  }
  
  private boolean filterTargetComponent(String targetComponent, RemoteComponent[] comps) {
    if (targetComponent == null) {
      return true;
    }
    for (RemoteComponent remoteComponent : comps) {
      if (remoteComponent.getName().equals(targetComponent)) {
        return true;
      }
    }
    return false;
  }
  
  private void incrementCountmap(String key, Map<String,Integer> counterMap) {
    Integer val = counterMap.get(key);
    if (val == null) {
      val = 0;
    }
    val++;
    counterMap.put(key, val);
  }
  
  private URL getJiraServiceUrl(String jiraManagmentURL) throws MalformedURLException {
    int fromIndex = "https://".length();
    return new URL(jiraManagmentURL.substring(0, jiraManagmentURL.indexOf("/", fromIndex)) + "/rpc/soap/jirasoapservice-v2");
  }
  
  private boolean isValidJiraURL(String jiraProjectURL) {
    if (StringUtils.isNotEmpty(jiraProjectURL)) {
      try {
        URL server = new URL(jiraProjectURL);
        String path = server.getPath().toLowerCase();
        boolean valid = path.startsWith("/browse/") && !path.equals("/browse/");
        if (!valid) {
          LOG.warn("Invalid jira {} url, should be like http://myjiraserver.com/browse/PROJECT-NAME", jiraProjectURL);
        }
        return path.startsWith("/browse/") && !path.equals("/browse/");
      } catch (MalformedURLException e) {
        LOG.error("Unable to parse jira server url", e);
      }
    }
    return false;
  }
  
  private String getJiraProjectName(String jiraManagmentURL) {
    return jiraManagmentURL.toLowerCase().substring(jiraManagmentURL.lastIndexOf("/") + 1);
  }

}
