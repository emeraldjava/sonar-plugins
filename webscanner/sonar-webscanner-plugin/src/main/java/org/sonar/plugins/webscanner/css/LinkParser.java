/*
 * Sonar Webscanner Plugin
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

package org.sonar.plugins.webscanner.css;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.xerces.impl.Constants;
import org.sonar.api.utils.SonarException;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse XML files using Sax parser to find links to stylesheets
 *
 * @author Matthijs Galesloot
 * @since 1.0
 */
public final class LinkParser {

  private static class QuietErrorHandler implements ErrorHandler  {

    public void error(SAXParseException arg0) throws SAXException {
      // nothing to do, keep quiet
    }

    public void fatalError(SAXParseException arg0) throws SAXException {
      // nothing to do, keep quiet
    }

    public void warning(SAXParseException arg0) throws SAXException {
      // nothing to do, keep quiet
    }
  }

  private static final class StylesheetHandler extends DefaultHandler {

    private static final String HREF = "href";
    private static final String TYPE = "type";
    private static final String TEXT_CSS = "text/css";
    private static final String LINK = "link";
    private final List<String> stylesheets = new ArrayList<String>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) {
      if (LINK.equals(qName) && StringUtils.equals(TEXT_CSS, attrs.getValue(TYPE))) {
        String href = attrs.getValue(HREF);
        if (href != null) {
          stylesheets.add(href);
        }
      }
    }
  }

  private static final SAXParserFactory SAX_FACTORY;

  /**
   * Build the SAXParserFactory.
   */
  static {

    SAX_FACTORY = SAXParserFactory.newInstance();

    try {
      SAX_FACTORY.setValidating(false);
      SAX_FACTORY.setNamespaceAware(false);
      SAX_FACTORY.setFeature("http://xml.org/sax/features/validation", false);
      SAX_FACTORY.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      SAX_FACTORY.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      SAX_FACTORY.setFeature("http://xml.org/sax/features/external-general-entities", false);
      SAX_FACTORY.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    } catch (SAXException e) {
      throw new SonarException(e);
    } catch (ParserConfigurationException e) {
      throw new SonarException(e);
    }
  }

  private void parse(InputStream input, DefaultHandler handler) {
    try {
      SAXParser parser = SAX_FACTORY.newSAXParser();
      XMLReader reader = parser.getXMLReader();
      reader.setFeature(Constants.XERCES_FEATURE_PREFIX + "continue-after-fatal-error", true);
      reader.setErrorHandler(new QuietErrorHandler());
      reader.setContentHandler(handler);
      reader.parse(new InputSource(input));
    } catch (IOException e) {
      throw new SonarException(e);
    } catch (SAXException e) {
      throw new SonarException(e);
    } catch (ParserConfigurationException e) {
      throw new SonarException(e);
    }
  }

  /**
   * Parse HTML file to get a list of referenced stylesheets.
   */
  public List<String> parseStylesheets(InputStream input) {

    StylesheetHandler handler = new StylesheetHandler();
    parse(input, handler);

    return handler.stylesheets;
  }
}