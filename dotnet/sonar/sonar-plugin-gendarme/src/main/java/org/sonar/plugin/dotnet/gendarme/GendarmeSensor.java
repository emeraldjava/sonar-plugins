/*
 * Maven and Sonar plugin for .Net
 * Copyright (C) 2010 Jose Chillan and Alexandre Victoor
 * mailto: jose.chillan@codehaus.org or alexvictoor@codehaus.org
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

package org.sonar.plugin.dotnet.gendarme;

import static org.sonar.plugin.dotnet.gendarme.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.dotnet.commons.GeneratedCodeFilter;
import org.apache.maven.dotnet.commons.project.SourceFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.maven.MavenPluginHandler;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulesManager;
import org.sonar.api.rules.Violation;
import org.sonar.plugin.dotnet.core.AbstractDotnetSensor;
import org.sonar.plugin.dotnet.core.resource.CLRAssembly;
import org.sonar.plugin.dotnet.core.resource.CSharpFileLocator;
import org.sonar.plugin.dotnet.core.resource.InvalidResourceException;
import org.sonar.plugin.dotnet.gendarme.model.Issue;
import org.sonar.plugin.dotnet.gendarme.stax.GendarmeResultStaxParser;
import org.xml.sax.InputSource;

public class GendarmeSensor extends AbstractDotnetSensor {

  private final static Logger log = LoggerFactory.getLogger(GendarmeSensor.class);


  private final RulesManager rulesManager;
  private final RulesProfile profile;
  private final GendarmePluginHandler pluginHandler;
  private final CSharpFileLocator fileLocator;

  /**
   * Constructs a @link{GendarmeSensor}.
   * 
   * @param rulesManager
   */
  public GendarmeSensor(RulesProfile profile, RulesManager rulesManager,
      GendarmePluginHandler pluginHandler, CSharpFileLocator fileLocator) {
    super();
    this.rulesManager = rulesManager;
    this.profile = profile;
    this.pluginHandler = pluginHandler;
    this.fileLocator = fileLocator;
  }

  /**
   * Launches the project analysis/
   * 
   * @param project
   * @param context
   */
  @Override
  public void analyse(Project project, SensorContext context) {
    final String reportFileName;
    if (GENDARME_REUSE_MODE.equals(getGendarmeMode(project))) {
      reportFileName = project.getConfiguration().getString(GENDARME_REPORT_KEY);
      log.warn("Using reuse report mode for Mono Gendarme");
      log.warn("Mono Gendarme profile settings may not have been taken in account");
    } else {
      reportFileName = GENDARME_REPORT_XML;
    }

    File dir = getReportsDirectory(project);
    File report = new File(dir, reportFileName);

    GendarmeResultStaxParser parser 
      = new GendarmeResultStaxParser();
    try {
      List<Issue> issues = parser.parse(report);
      log.debug("Parsing ended. Start saving measures...");
      for (Issue issue : issues) {
        saveViolations(issue, project, context);
      }

    } catch (InvalidResourceException ex) {
      log.warn("C# file not referenced in the solution", ex);
    }
  }

  /**
   * @Deprecated Since 0.6 (Stax version of the parser)
   * Transforms the report to a usable format.
   * 
   * @param report
   * @param dir
   * @return
   */
  @Deprecated
  private File transformReport(File report, File dir) {
    try {
      ClassLoader contextClassLoader = Thread.currentThread()
        .getContextClassLoader();
      InputStream stream = contextClassLoader
        .getResourceAsStream(GENDARME_TRANSFO_XSL);
      if (stream==null) {
        // happens with sonar2.3 classloader mechanism
        stream = getClass().getClassLoader()
          .getResourceAsStream(GENDARME_TRANSFO_XSL);
      }
      Source xslSource = new SAXSource(new InputSource(stream));
      Templates templates = TransformerFactory.newInstance().newTemplates(
        xslSource);
      Transformer transformer = templates.newTransformer();

      // We open the report to be processed
      Source xmlSource = new StreamSource(report);

      File processedReport = new File(dir, GENDARME_PROCESSED_REPORT_XML);
      processedReport.delete();
      Result result = new StreamResult(new FileOutputStream(processedReport));
      transformer.transform(xmlSource, result);
      return processedReport;
    } catch (Exception exc) {
      log.warn("Error during the processing of the Gendarme report for Sonar",
          exc);
    }
    return null;
  }

  /**
   * @param project
   * @return
   */
  @Override
  public MavenPluginHandler getMavenPluginHandler(Project project) {
    String mode = getGendarmeMode(project);
    final MavenPluginHandler pluginHandlerReturned;
    if (GENDARME_DEFAULT_MODE.equalsIgnoreCase(mode)) {
      pluginHandlerReturned = pluginHandler;
    } else {
      pluginHandlerReturned = null;
    }
    return pluginHandlerReturned;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    String mode = getGendarmeMode(project);
    return super.shouldExecuteOnProject(project) && !GENDARME_SKIP_MODE.equalsIgnoreCase(mode);
  }

  private String getGendarmeMode(Project project) {
    return project.getConfiguration().getString(GENDARME_MODE_KEY, GENDARME_DEFAULT_MODE);
  }

  private void saveViolations(Issue issue, Project project, SensorContext context) {

    String key = issue.getName();
    String source = issue.getSource();
    String message = issue.getProblem() + " " + issue.getSolution();
    String location = issue.getLocation();

    final String filePath;
    final Integer lineNumber;

    if (StringUtils.isEmpty(source)
        || StringUtils.contains(source, "debugging symbols unavailable")) {
      String assemblyName = StringUtils.substringBefore(
          issue.getAssembly(), ",");

      if ("[across all assemblies analyzed]".equals(assemblyName)) {
        // this one will be ignored... Anyway it barely never happen
        return;
      }

      CLRAssembly assembly = CLRAssembly.fromName(project, assemblyName);
      if (StringUtils.contains(key, "Assembly")) {
        // we assume we have a violation at the
        // assembly level
        filePath = assembly.getVisualProject().getDirectory()
        .getAbsolutePath()
          + File.separator
          + "Properties"
          + File.separator
          + "AssemblyInfo.cs";
        lineNumber = null;
      } else {
        if (StringUtils.containsNone(location, " ")) {
          // we will try to find a cs file that match with the class name
          final String className = StringUtils.substringBeforeLast(
              StringUtils.substringAfterLast(location, "."), "/");
          Collection<SourceFile> sourceFiles = assembly.getVisualProject()
            .getSourceFiles();

          SourceFile sourceFile = retrieveCurrentSourcefile(className, sourceFiles);

          if (sourceFile == null) {
            // this one will be ignored
            log.info("ignoring gendarme violation {} {} {}", new Object[] {
              key, source, message });
            return;
          } else {
            filePath = sourceFile.getFile().getAbsolutePath();
            lineNumber = null;
          }
        } else {
          // this one will be ignored
          log.info("ignoring gendarme violation {} {} {}", new Object[] {
            key, source, message });
          return;
        }
      }
    } else {
      DefectLocation defectLocation = DefectLocation.parse(source);
      filePath = defectLocation.getPath();
      lineNumber = defectLocation.getLineNumber();

      // we do not care about violations in generated files
      if (GeneratedCodeFilter.INSTANCE.isGenerated(StringUtils
          .substringAfterLast(filePath, File.separator))) {
        return;
      }
    }

    message = appendMessageLocation(message, location, lineNumber);

    Resource<?> resource = fileLocator.getResource(project, filePath);
    Rule rule = rulesManager.getPluginRule(GendarmePlugin.KEY, key);

    if (rule == null || resource == null) {
      // We skip the rules that were not registered
      log.debug("Unregistered rule {}, file path is : {}", key, filePath);
      return;
    }
    ActiveRule activeRule = profile.getActiveRule(GendarmePlugin.KEY, key);
    Violation violation = createViolation(message, lineNumber, resource, rule, activeRule);

    // We store the violation
    context.saveViolation(violation);

  }

  private Violation createViolation(String message, final Integer lineNumber, Resource<?> resource, Rule rule, ActiveRule activeRule) {
    Violation violation = new Violation(rule, resource);
    violation.setLineId(lineNumber);
    violation.setMessage(message);
    if (activeRule != null) {
      violation.setPriority(activeRule.getPriority());
    }
    return violation;
  }

  private SourceFile retrieveCurrentSourcefile(final String className, Collection<SourceFile> sourceFiles) {
    SourceFile sourceFile = null;
    for (SourceFile currentSourceFile : sourceFiles) {
      if (StringUtils
          .startsWith(currentSourceFile.getName(), className)) {
        sourceFile = currentSourceFile;
        break;
      }
    }
    return sourceFile;
  }

  /**
   * Append a more specific location information to the message
   * 
   * @param message
   * @param location
   * @param lineNumber
   * @return message with a more specific location information
   * 
   */
  private String appendMessageLocation(String message, String location, final Integer lineNumber) {
    if (lineNumber == null
        && StringUtils.contains(location, "::")) {
      String code = StringUtils.substringAfter(location, "::");
      if (!StringUtils.contains(message, code)) {
        message = StringUtils.substringAfter(location, "::") + " " + message;
      }
    }
    return message;
  }

}
