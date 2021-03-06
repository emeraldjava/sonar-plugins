/*
 * Sonar C# Plugin :: Gendarme
 * Copyright (C) 2010 Jose Chillan, Alexandre Victoor and SonarSource
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

package org.sonar.plugins.csharp.gendarme;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.dotnet.tools.commons.utils.FileFinder;
import org.sonar.dotnet.tools.gendarme.GendarmeCommandBuilder;
import org.sonar.dotnet.tools.gendarme.GendarmeException;
import org.sonar.dotnet.tools.gendarme.GendarmeRunner;
import org.sonar.plugins.csharp.api.CSharpConfiguration;
import org.sonar.plugins.csharp.api.CSharpConstants;
import org.sonar.plugins.csharp.api.MicrosoftWindowsEnvironment;
import org.sonar.plugins.csharp.api.sensor.AbstractCilRuleBasedCSharpSensor;
import org.sonar.plugins.csharp.gendarme.profiles.GendarmeProfileExporter;
import org.sonar.plugins.csharp.gendarme.results.GendarmeResultParser;

/**
 * Collects the Gendarme reporting into sonar.
 */
@DependsUpon(CSharpConstants.CSHARP_CORE_EXECUTED)
public class GendarmeSensor extends AbstractCilRuleBasedCSharpSensor {

  private static final Logger LOG = LoggerFactory.getLogger(GendarmeSensor.class);

  private ProjectFileSystem fileSystem;
  private RulesProfile rulesProfile;
  private GendarmeProfileExporter profileExporter;
  private GendarmeResultParser gendarmeResultParser;
  private CSharpConfiguration configuration;
  private String executionMode;

  /**
   * Constructs a {@link GendarmeSensor}.
   * 
   * @param fileSystem
   * @param ruleFinder
   * @param gendarmeRunner
   * @param profileExporter
   * @param rulesProfile
   */
  public GendarmeSensor(ProjectFileSystem fileSystem, RulesProfile rulesProfile, GendarmeProfileExporter profileExporter,
      GendarmeResultParser gendarmeResultParser, CSharpConfiguration configuration, MicrosoftWindowsEnvironment microsoftWindowsEnvironment) {
    super(microsoftWindowsEnvironment, configuration, "Gendarme");
    this.fileSystem = fileSystem;
    this.rulesProfile = rulesProfile;
    this.profileExporter = profileExporter;
    this.gendarmeResultParser = gendarmeResultParser;
    this.configuration = configuration;
    this.executionMode = configuration.getString(GendarmeConstants.MODE, "");
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    boolean skipMode = GendarmeConstants.MODE_SKIP.equalsIgnoreCase(executionMode);
    boolean reuseMode = GendarmeConstants.MODE_REUSE_REPORT.equalsIgnoreCase(executionMode);
    if (skipMode) {
      LOG.info("Gendarme plugin won't execute as it is set to 'skip' mode.");
    }
    return reuseMode || (super.shouldExecuteOnProject(project) && !skipMode);
  }

  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    if (rulesProfile.getActiveRulesByRepository(GendarmeConstants.REPOSITORY_KEY).isEmpty()) {
      LOG.warn("/!\\ SKIP Gendarme analysis: no rule defined for Gendarme in the \"{}\" profil.", rulesProfile.getName());
      return;
    }

    gendarmeResultParser.setEncoding(fileSystem.getSourceCharset());
    final File reportFile;
    File sonarDir = fileSystem.getSonarWorkingDirectory();
    if (GendarmeConstants.MODE_REUSE_REPORT.equalsIgnoreCase(executionMode)) {
      String reportPath = configuration.getString(GendarmeConstants.REPORTS_PATH_KEY, GendarmeConstants.GENDARME_REPORT_XML);
      reportFile = FileFinder.browse(sonarDir, reportPath);
    } else {
      // prepare config file for Gendarme
      File gendarmeConfigFile = generateConfigurationFile();
      // run Gendarme
      try {
        File tempDir = new File(getMicrosoftWindowsEnvironment().getCurrentSolution().getSolutionDir(), getMicrosoftWindowsEnvironment()
            .getWorkingDirectory());
        GendarmeRunner runner = GendarmeRunner.create(
            configuration.getString(GendarmeConstants.INSTALL_DIR_KEY, GendarmeConstants.INSTALL_DIR_DEFVALUE), tempDir.getAbsolutePath());

        launchGendarme(project, runner, gendarmeConfigFile);
      } catch (GendarmeException e) {
        throw new SonarException("Gendarme execution failed.", e);
      }
      reportFile = new File(sonarDir, GendarmeConstants.GENDARME_REPORT_XML);
    }

    // and analyse results
    analyseResults(reportFile);
  }

  protected File generateConfigurationFile() {
    File configFile = new File(fileSystem.getSonarWorkingDirectory(), GendarmeConstants.GENDARME_RULES_FILE);
    FileWriter writer = null;
    try {
      writer = new FileWriter(configFile);
      profileExporter.exportProfile(rulesProfile, writer);
      writer.flush();
    } catch (IOException e) {
      throw new SonarException("Error while generating the Gendarme configuration file by exporting the Sonar rules.", e);
    } finally {
      IOUtils.closeQuietly(writer);
    }
    return configFile;
  }

  protected void launchGendarme(Project project, GendarmeRunner runner, File gendarmeConfigFile) throws GendarmeException {
    GendarmeCommandBuilder builder = runner.createCommandBuilder(getVSSolution(), getVSProject(project));
    builder.setConfigFile(gendarmeConfigFile);
    builder.setReportFile(new File(fileSystem.getSonarWorkingDirectory(), GendarmeConstants.GENDARME_REPORT_XML));
    builder.setConfidence(configuration
        .getString(GendarmeConstants.GENDARME_CONFIDENCE_KEY, GendarmeConstants.GENDARME_CONFIDENCE_DEFVALUE));
    builder.setSeverity("all");
    builder.setSilverlightFolder(getMicrosoftWindowsEnvironment().getSilverlightDirectory());
    builder.setBuildConfigurations(configuration.getString(CSharpConstants.BUILD_CONFIGURATIONS_KEY,
        CSharpConstants.BUILD_CONFIGURATIONS_DEFVALUE));

    String[] assemblies = configuration.getStringArray("sonar.gendarme.assemblies");
    if (assemblies == null || assemblies.length == 0) {
      assemblies = configuration.getStringArray(CSharpConstants.ASSEMBLIES_TO_SCAN_KEY);
    } else {
      LOG.warn("Using deprecated key 'sonar.gendarme.assemblies', you should use instead " + CSharpConstants.ASSEMBLIES_TO_SCAN_KEY);
    }

    builder.setAssembliesToScan(assemblies);

    runner.execute(builder, configuration.getInt(GendarmeConstants.TIMEOUT_MINUTES_KEY, GendarmeConstants.TIMEOUT_MINUTES_DEFVALUE));
  }

  private void analyseResults(File reportFile) {
    if (reportFile.exists()) {
      LOG.debug("Gendarme report found at location {}", reportFile);
      gendarmeResultParser.parse(reportFile);
    } else {
      LOG.warn("No Gendarme report found for path {}", reportFile);
    }
  }

}