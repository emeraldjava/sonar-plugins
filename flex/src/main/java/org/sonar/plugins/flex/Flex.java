/*
 * Sonar Flex Plugin
 * Copyright (C) 2010 SonarSource
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

package org.sonar.plugins.flex;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.AbstractLanguage;

public class Flex extends AbstractLanguage {
  private Configuration configuration;
  public static final String KEY = "flex";
  public static final String DEFAULT_PACKAGE_NAME = "[default]";
  static final String[] SUFFIXES = {"as"};
  public static Flex INSTANCE;

  public Flex(Configuration configuration) {
    super(KEY, "Flex");
    this.configuration = configuration;

    // See SONAR-1461
    INSTANCE = this;
  }

  public String[] getFileSuffixes() {
    String[] suffixes = configuration.getStringArray(FlexPlugin.FILE_SUFFIXES_KEY);
    if (suffixes == null || suffixes.length == 0) {
      suffixes = StringUtils.split(FlexPlugin.FILE_SUFFIXES_DEFVALUE, ",");
    }
    return suffixes;
  }

}
