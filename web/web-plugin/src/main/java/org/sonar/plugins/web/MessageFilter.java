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

package org.sonar.plugins.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.web.markupvalidation.MarkupMessage;

public class MessageFilter {

  private static final Logger LOG = LoggerFactory.getLogger(HtmlSensor.class);

  private final Pattern[] patterns;

  MessageFilter(Object property) {

    final List<String> excludeViolations = new ArrayList<String>();

    if (property != null) {
      if (property instanceof String) {
        excludeViolations.add((String) property);
      } else if (Collection.class.isAssignableFrom(property.getClass())) {
        excludeViolations.addAll((Collection<String>) property);
      }
    }

    patterns = new Pattern[excludeViolations.size()];
    for (int i = 0; i < excludeViolations.size(); i++) {
      LOG.info("Pattern:" + excludeViolations.get(i));
      patterns[i] = Pattern.compile(excludeViolations.get(i));
    }
  }

  public boolean accept(MarkupMessage message) {
    String text = String.format("%03d:%s",message.getMessageId(), message.getMessage());
    for (Pattern pattern : patterns) {
      if (pattern.matcher(text).lookingAt()) {
        LOG.debug("Ignore: " + text);
        return false;
      }
    }
    return true;
  }
}