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

@XmlType(name = "ProjectFile")
public class ProjectFile {

  @XmlAttribute(name = "Compress")
  private String compress = "True";
  @XmlAttribute(name = "DefaultTargetCheck")
  private String defaultTargetCheck = "True";
  @XmlAttribute(name = "DefaultRuleCheck")
  private String defaultRuleCheck = "True";
  @XmlAttribute(name = "SaveByRuleGroup")
  private String saveByRuleGroup = "";
  @XmlAttribute(name = "Deterministic")
  private String deterministic = "True";

  /**
   * Constructs a @link{ProjectFile}.
   */
  public ProjectFile() {
  }

  /**
   * Returns the compress.
   * 
   * @return The compress to return.
   */
  public String getCompress() {
    return this.compress;
  }

  /**
   * Sets the compress.
   * 
   * @param compress
   *          The compress to set.
   */
  public void setCompress(String compress) {
    this.compress = compress;
  }

  /**
   * Returns the defaultTargetCheck.
   * 
   * @return The defaultTargetCheck to return.
   */
  public String getDefaultTargetCheck() {
    return this.defaultTargetCheck;
  }

  /**
   * Sets the defaultTargetCheck.
   * 
   * @param defaultTargetCheck
   *          The defaultTargetCheck to set.
   */
  public void setDefaultTargetCheck(String defaultTargetCheck) {
    this.defaultTargetCheck = defaultTargetCheck;
  }

  /**
   * Returns the defaultRuleCheck.
   * 
   * @return The defaultRuleCheck to return.
   */
  public String getDefaultRuleCheck() {
    return this.defaultRuleCheck;
  }

  /**
   * Sets the defaultRuleCheck.
   * 
   * @param defaultRuleCheck
   *          The defaultRuleCheck to set.
   */
  public void setDefaultRuleCheck(String defaultRuleCheck) {
    this.defaultRuleCheck = defaultRuleCheck;
  }

  /**
   * Returns the saveByRuleGroup.
   * 
   * @return The saveByRuleGroup to return.
   */
  public String getSaveByRuleGroup() {
    return this.saveByRuleGroup;
  }

  /**
   * Sets the saveByRuleGroup.
   * 
   * @param saveByRuleGroup
   *          The saveByRuleGroup to set.
   */
  public void setSaveByRuleGroup(String saveByRuleGroup) {
    this.saveByRuleGroup = saveByRuleGroup;
  }

  /**
   * Returns the deterministic.
   * 
   * @return The deterministic to return.
   */
  public String getDeterministic() {
    return this.deterministic;
  }

  /**
   * Sets the deterministic.
   * 
   * @param deterministic
   *          The deterministic to set.
   */
  public void setDeterministic(String deterministic) {
    this.deterministic = deterministic;
  }

}