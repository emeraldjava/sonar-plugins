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

package org.sonar.plugins.sigmm.axis;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.plugins.sigmm.MMRank;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CombinedAxisTest {
  @Test
  public void testGetRank() {
    int[] bLimits = {40, 30, 20, 10, 0};
    MMAxis axis1 = new SimpleAxis(bLimits, MMRank.ascSortedRanks(), CoreMetrics.ACCESSORS, getContext(14.0));
    MMAxis axis2 = new SimpleAxis(bLimits, MMRank.ascSortedRanks(), CoreMetrics.ACCESSORS, getContext(50.0));
    MMAxis axis3 = new SimpleAxis(bLimits, MMRank.ascSortedRanks(), CoreMetrics.ACCESSORS, getContext(0.0));
    assertEquals(new CombinedAxis(axis1).getRank(), MMRank.PLUS);
    assertEquals(new CombinedAxis(axis1, axis2).getRank(), MMRank.ZERO);
    assertEquals(new CombinedAxis(axis1, axis2, axis2).getRank(), MMRank.MINUS);
  }

  private DecoratorContext getContext(double value) {
    DecoratorContext context = mock(DecoratorContext.class);
    when(context.getMeasure(CoreMetrics.ACCESSORS)).
      thenReturn(new Measure(CoreMetrics.ACCESSORS, value));
    return context;
  }

}
