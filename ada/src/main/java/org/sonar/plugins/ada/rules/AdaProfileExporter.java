/*
 * Ada Sonar Plugin
 * Copyright (C) 2010 Akram Ben Aissi
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.ada.rules;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.RuleParam;
import org.sonar.api.utils.SonarException;

import org.sonar.plugins.ada.Ada;
import org.sonar.plugins.ada.AdaPlugin;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class AdaProfileExporter extends ProfileExporter {

  private Configuration conf;

  public AdaProfileExporter(Configuration configuration) {
    super(AdaRuleRepository.REPOSITORY_KEY, AdaRuleRepository.REPOSITORY_NAME);
    this.conf = configuration;
    setSupportedLanguages(Ada.LANGUAGE_KEY);
    setMimeType("application/xml");
  }

  /**
   * for unit tests
   */
  AdaProfileExporter() {
    this(new BaseConfiguration());
  }

  public void exportProfile(RulesProfile profile, Writer writer) {
    try {
      ListMultimap<String, ActiveRule> activeRulesByConfigKey = arrangeByConfigKey(profile.getActiveRulesByRepository(AdaPlugin.KEY));
      generateXML(writer, activeRulesByConfigKey);

    } catch (IOException e) {
      throw new SonarException("Fail to export the profile " + profile, e);
    }

  }

  private void generateXML(Writer writer, ListMultimap<String, ActiveRule> activeRulesByConfigKey) throws IOException {
    appendXmlHeader(writer);
    appendCustomFilters(writer);
    appendCheckerModules(writer, activeRulesByConfigKey);
    appendTreeWalker(writer, activeRulesByConfigKey);
    appendXmlFooter(writer);
  }

  private void appendXmlHeader(Writer writer) throws IOException {
    writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    writer.append("<!DOCTYPE module PUBLIC \"-//Puppy Crawl//DTD Check Configuration ");
    writer.append("1.2//EN\" \"http://www.puppycrawl.com/dtds/configuration_1_2.dtd\">");
    writer.append("<!-- Generated by Sonar -->" + "<module name=\"Checker\">");
  }

  private void appendCustomFilters(Writer writer) throws IOException {
    String filtersXML = conf.getString(AdaPlugin.FILTERS_KEY, AdaPlugin.FILTERS_DEFAULT_VALUE);
    if (StringUtils.isNotBlank(filtersXML)) {
      writer.append(filtersXML);
    }
  }

  private void appendCheckerModules(Writer writer, ListMultimap<String, ActiveRule> activeRulesByConfigKey) throws IOException {
    for (String configKey : activeRulesByConfigKey.keySet()) {
      if ( !isInTreeWalker(configKey)) {
        List<ActiveRule> activeRules = activeRulesByConfigKey.get(configKey);
        for (ActiveRule activeRule : activeRules) {
          appendModule(writer, activeRule, activeRules.size() > 1);
        }
      }
    }
  }

  private void appendTreeWalker(Writer writer, ListMultimap<String, ActiveRule> activeRulesByConfigKey) throws IOException {
    writer.append("<module name=\"TreeWalker\">");
    writer.append("<module name=\"FileContentsHolder\"/> ");
    for (String configKey : activeRulesByConfigKey.keySet()) {
      if (isInTreeWalker(configKey)) {
        List<ActiveRule> activeRules = activeRulesByConfigKey.get(configKey);
        for (ActiveRule activeRule : activeRules) {
          appendModule(writer, activeRule, activeRules.size() > 1);
        }
      }
    }
    writer.append("</module>");
  }

  private void appendXmlFooter(Writer writer) throws IOException {
    writer.append("</module>");
  }

  static boolean isInTreeWalker(String configKey) {
    return StringUtils.startsWithIgnoreCase(configKey, "Checker/TreeWalker/");
  }

  private static ListMultimap<String, ActiveRule> arrangeByConfigKey(List<ActiveRule> activeRules) {
    ListMultimap<String, ActiveRule> result = ArrayListMultimap.create();
    if (activeRules != null) {
      for (ActiveRule activeRule : activeRules) {
        result.put(activeRule.getConfigKey(), activeRule);
      }
    }
    return result;
  }

  private void appendModule(Writer writer, ActiveRule activeRule, boolean manyInstances) throws IOException {
    String moduleName = StringUtils.substringAfterLast(activeRule.getConfigKey(), "/");
    writer.append("<module name=\"");
    StringEscapeUtils.escapeXml(writer, moduleName);
    writer.append("\">");
    if (manyInstances) {
      appendModuleProperty(writer, "id", activeRule.getRuleKey());
    }
    appendModuleProperty(writer, "severity", AdaRulePriorityMapper.to(activeRule.getPriority()));
    appendRuleParameters(writer, activeRule);
    writer.append("</module>");
  }

  private void appendRuleParameters(Writer writer, ActiveRule activeRule) throws IOException {
    for (RuleParam ruleParam : activeRule.getRule().getParams()) {
      String value = activeRule.getParameter(ruleParam.getKey());
      if (StringUtils.isNotBlank(value)) {
        appendModuleProperty(writer, ruleParam.getKey(), value);
      }
    }
  }

  private void appendModuleProperty(Writer writer, String propertyKey, String propertyValue) throws IOException {
    if (StringUtils.isNotBlank(propertyValue)) {
      writer.append("<property name=\"");
      StringEscapeUtils.escapeXml(writer, propertyKey);
      writer.append("\" value=\"");
      StringEscapeUtils.escapeXml(writer, propertyValue);
      writer.append("\"/>");
    }
  }

}