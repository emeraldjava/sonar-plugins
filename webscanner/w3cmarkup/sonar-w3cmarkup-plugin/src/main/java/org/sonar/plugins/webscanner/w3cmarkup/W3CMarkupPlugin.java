/*
 * Copyright (C) 2010 Matthijs Galesloot
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

package org.sonar.plugins.webscanner.w3cmarkup;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;

/**
 * @author Matthijs Galesloot
 */
@Properties({
  @Property(key = "sonar.web.fileExtensions",
    name = "File extensions",
    description = "List of file extensions that will be scanned.",
    defaultValue="html",
    global = true, project = true)})
public final class W3CMarkupPlugin implements Plugin {

  private static final String KEY = "sonar-w3cmarkup-plugin";

  public static String getKEY() {
    return KEY;
  }

  public String getDescription() {
    return getName() + " validates HTML with help of the W3C Markup Validator";
  }

  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> list = new ArrayList<Class<? extends Extension>>();

    // W3C markup rules
    list.add(MarkupRuleRepository.class);
    list.add(MarkupProfileExporter.class);
    list.add(MarkupProfileImporter.class);
    list.add(DefaultMarkupProfile.class);
    list.add(HtmlMetrics.class);
    list.add(HtmlWidget.class);
    list.add(HtmlSensor.class);

    return list;
  }

  public String getKey() {
    return KEY;
  }

  public String getName() {
    return "W3C Markup";
  }

  @Override
  public String toString() {
    return getKey();
  }
}