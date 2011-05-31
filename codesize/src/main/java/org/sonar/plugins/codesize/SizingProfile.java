/*
 * Codesize
 * Copyright (C) 2010 Matthijs Galesloot
 * dev@sonar.codehaus.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonar.plugins.codesize;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.utils.SonarException;

public final class SizingProfile implements BatchExtension {

  private static final String EXCLUDES = "includes";

  private static final String INCLUDES = "includes";

  private static final Logger LOG = LoggerFactory.getLogger(SizingProfile.class);
  private static String getConfigurationFromFile() {
    InputStream inputStream = null;
    try {
      inputStream = SizingProfile.class.getResourceAsStream("/metrics.xml");

      return IOUtils.toString(inputStream, "UTF-8");
    } catch (IOException e) {
      throw new SonarException("Configuration file not found: metrics.xml", e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  private final List<SizingMetric> sizingMetrics = new ArrayList<SizingMetric>();

  public SizingProfile(Configuration configuration) {

    String codeSizeProfile = configuration.getString(CodesizeConstants.SONAR_CODESIZE_PROFILE);
    if (codeSizeProfile == null) {
      codeSizeProfile = getConfigurationFromFile();
    }

    LOG.debug("Load codesize profile: " + codeSizeProfile);
    parse(codeSizeProfile);
  }

  public List<SizingMetric> getSizingMetrics() {
    return sizingMetrics;
  }

  public void parse(String codeSizeProfile) {
    String[] lines = StringUtils.split(codeSizeProfile, "\n");
    SizingMetric metric = null;
    for (String line : lines) {
      if ( !StringUtils.isBlank(line)) {
        String[] kv = StringUtils.stripAll(line.split("[:=]"));
        if (kv.length > 1) {
          if (INCLUDES.contains(kv[0])) {
            metric.addIncludes(kv[1]);
          } else if (EXCLUDES.contains(kv[0])) {
            metric.addExcludes(kv[1]);
          }
        } else {
          metric = new SizingMetric();
          metric.setName(line.trim());
          getSizingMetrics().add(metric);
        }
      }
    }
  }
}