/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource
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

package org.sonar.plugins.emma;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class EmmaIT {

  private static Sonar sonar;
  private static final String PROJECT_STRUTS = "org.apache.struts:struts-parent";
  private static final String MODULE_CORE = "org.apache.struts:struts-core";
  private static final String MODULE_EL = "org.apache.struts:struts-el";
  private static final String FILE_ACTION = "org.apache.struts:struts-core:org.apache.struts.action.Action";
  private static final String PACKAGE_ACTION = "org.apache.struts:struts-core:org.apache.struts.action";

  @BeforeClass
  public static void buildServer() {
    sonar = Sonar.create("http://localhost:9000");
  }

  @Test
  public void strutsIsAnalyzed() {
    assertThat(sonar.find(new ResourceQuery(PROJECT_STRUTS)).getName(), is("Struts"));
    assertThat(sonar.find(new ResourceQuery(PROJECT_STRUTS)).getVersion(), is("1.3.9"));
    assertThat(sonar.find(new ResourceQuery(MODULE_CORE)).getName(), is("Struts Core"));
    assertThat(sonar.find(new ResourceQuery(MODULE_EL)).getName(), is("Struts EL"));
    assertThat(sonar.find(new ResourceQuery(PACKAGE_ACTION)).getName(), is("org.apache.struts.action"));
  }

  @Test
  public void projectsMetrics() {
    assertThat(getProjectMeasure("coverage").getValue(), is(14.8));
    assertThat(getProjectMeasure("line_coverage").getValue(), is(14.8));
    assertThat("lines_to_cover", getProjectMeasure("lines_to_cover").getValue(), anyOf(
      is(26124.0), // java 1.5.0_22
      is(26126.0)  // java 1.6.0_20
      ));
    assertThat("uncovered_lines", getProjectMeasure("uncovered_lines").getValue(), anyOf(
      is(22264.0), // java 1.5_0.22
      is(22266.0)  // java 1.6.0_20
      ));
    assertThat(getProjectMeasure("tests").getValue(), is(323.0));
    assertThat(getProjectMeasure("test_success_density").getValue(), is(100.0));
  }

  @Test
  public void CoremoduleMetrics() {
    assertThat(getCoreModuleMeasure("coverage").getValue(), is(38.1));
    assertThat(getCoreModuleMeasure("line_coverage").getValue(), is(38.1));
    assertThat("lines_to_cover", getCoreModuleMeasure("lines_to_cover").getValue(), anyOf(
      is(7447.0), // java 1.5.0_22
      is(7448.0) // java 1.6.0_20
      ));
    assertThat("uncovered_lines", getCoreModuleMeasure("uncovered_lines").getValue(), anyOf(
      is(4606.0), // java 1.5.0_22
      is(4607.0) // java 1.6.0_20
      ));
    assertThat(getCoreModuleMeasure("tests").getValue(), is(195.0));
    assertThat(getCoreModuleMeasure("test_success_density").getValue(), is(100.0));
  }

  @Test
  public void ElModuleMetrics() {
    assertThat(getElModuleMeasure("coverage").getValue(), is(0.0));
    assertThat(getElModuleMeasure("line_coverage").getValue(), is(0.0));
    assertThat(getElModuleMeasure("lines_to_cover").getValue(), is(8064.0));
    assertThat(getElModuleMeasure("uncovered_lines").getValue(), is(8064.0));
    assertThat(getElModuleMeasure("tests").getValue(), is(0.0));
    assertNull(getElModuleMeasure("test_success_density"));
  }

  @Test
  public void packagesMetrics() {
    assertThat(getPackageMeasure("coverage").getValue(), is(32.1));
    assertThat(getPackageMeasure("line_coverage").getValue(), is(32.1));
    assertThat(getPackageMeasure("lines_to_cover").getValue(), is(1569.0));
    assertThat(getPackageMeasure("uncovered_lines").getValue(), is(1065.0));
    assertThat(getPackageMeasure("tests").getValue(), is(105.0));
    assertThat(getPackageMeasure("test_success_density").getValue(), is(100.0));
  }

  @Test
  public void filesMetrics() {
    assertThat(getFileMeasure("coverage").getValue(), is(0.0));
    assertThat(getFileMeasure("line_coverage").getValue(), is(0.0));
    assertThat(getFileMeasure("lines_to_cover").getValue(), is(78.0));
    assertThat(getFileMeasure("uncovered_lines").getValue(), is(78.0));
    assertNull(getFileMeasure("tests"));
    assertNull(getFileMeasure("test_success_density"));
  }

  private Measure getFileMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(FILE_ACTION, metricKey));
    Measure measure = resource!=null ? resource.getMeasure(metricKey) : null;

    return measure;
  }

  private Measure getPackageMeasure(String metricKey) {
    return sonar.find(ResourceQuery.createForMetrics(PACKAGE_ACTION, metricKey)).getMeasure(metricKey);
  }

  private Measure getCoreModuleMeasure(String metricKey) {
    return sonar.find(ResourceQuery.createForMetrics(MODULE_CORE, metricKey)).getMeasure(metricKey);
  }

  private Measure getElModuleMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(MODULE_EL, metricKey));
    Measure measure = resource!=null ? resource.getMeasure(metricKey) : null;

    return measure;
  }

  private Measure getProjectMeasure(String metricKey) {
    return sonar.find(ResourceQuery.createForMetrics(PROJECT_STRUTS, metricKey)).getMeasure(metricKey);
  }
}
