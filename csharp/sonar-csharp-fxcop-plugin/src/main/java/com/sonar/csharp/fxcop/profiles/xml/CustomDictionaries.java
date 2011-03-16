/*
 * Sonar C# Plugin :: FxCop
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

package com.sonar.csharp.fxcop.profiles.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * A CustomDictionaries for FxCop options
 */
@XmlType(name = "CustomDictionaries")
public class CustomDictionaries {

  @XmlAttribute(name = "SearchFxCopDir")
  private String searchFxCopDir = "True";

  @XmlAttribute(name = "SearchUserProfile")
  private String searchUserProfile = "True";

  @XmlAttribute(name = "SearchProjectDir")
  private String searchProjectDir = "True";

  /**
   * 
   * Constructs a @link{CustomDictionaries}.
   */
  public CustomDictionaries() {
  }

  /**
   * Returns the searchFxCopDir.
   * 
   * @return The searchFxCopDir to return.
   */
  public String getSearchFxCopDir() {
    return this.searchFxCopDir;
  }

  /**
   * Sets the searchFxCopDir.
   * 
   * @param searchFxCopDir
   *          The searchFxCopDir to set.
   */
  public void setSearchFxCopDir(String searchFxCopDir) {
    this.searchFxCopDir = searchFxCopDir;
  }

  /**
   * Returns the searchUserProfile.
   * 
   * @return The searchUserProfile to return.
   */
  public String getSearchUserProfile() {
    return this.searchUserProfile;
  }

  /**
   * Sets the searchUserProfile.
   * 
   * @param searchUserProfile
   *          The searchUserProfile to set.
   */
  public void setSearchUserProfile(String searchUserProfile) {
    this.searchUserProfile = searchUserProfile;
  }

  /**
   * Returns the searchProjectDir.
   * 
   * @return The searchProjectDir to return.
   */
  public String getSearchProjectDir() {
    return this.searchProjectDir;
  }

  /**
   * Sets the searchProjectDir.
   * 
   * @param searchProjectDir
   *          The searchProjectDir to set.
   */
  public void setSearchProjectDir(String searchProjectDir) {
    this.searchProjectDir = searchProjectDir;
  }
}