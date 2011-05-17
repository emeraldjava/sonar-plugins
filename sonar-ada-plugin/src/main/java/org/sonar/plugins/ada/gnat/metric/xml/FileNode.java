/*
 * Ada Sonar Plugin
 * Copyright (C) 2010 Akram Ben Aissi
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

/**
 * 
 */
package org.sonar.plugins.ada.gnat.metric.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * @author Akram Ben Aissi
 * 
 */
@XStreamAlias("file")
public class FileNode {

  @XStreamAsAttribute
  private String name;

  @XStreamImplicit
  private List<UnitNode> units;

  private CouplingNode coupling;

  @XStreamImplicit
  @XStreamConverter(MetricNodeConverter.class)
  private List<MetricNode> metrics;

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the metrics
   */
  public List<MetricNode> getMetrics() {
    return metrics;
  }

  /**
   * @return the coupling
   */
  public CouplingNode getCoupling() {
    return coupling;
  }

  /**
   * @return the units
   */
  public List<UnitNode> getUnits() {
    return units;
  }

}
