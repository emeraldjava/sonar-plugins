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

package com.sonar.csharp.fxcop.profiles;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.test.TestUtils;
import org.xml.sax.SAXException;

public class FxCopProfileExporterTest {

  @Test
  public void testSimpleFxCopRulesToExport() throws IOException, SAXException {
    RulesProfile profile = RulesProfile.create("Sonar C# Way", "cs");
    profile.activateRule(Rule.create("fxcop", "AssembliesShouldHaveValidStrongNames", "Assemblies should have valid strong names")
        .setConfigKey("AssembliesShouldHaveValidStrongNames@$(FxCopDir)\\Rules\\DesignRules.dll"), null);
    profile.activateRule(
        Rule.create("fxcop", "UsePropertiesWhereAppropriate", "Use properties where appropriate").setConfigKey(
            "UsePropertiesWhereAppropriate@$(FxCopDir)\\Rules\\DesignRules.dll"), null);
    profile.activateRule(
        Rule.create("fxcop", "AvoidDuplicateAccelerators", "Avoid duplicate accelerators").setConfigKey(
            "AvoidDuplicateAccelerators@$(FxCopDir)\\Rules\\GlobalizationRules.dll"), null);

    StringWriter writer = new StringWriter();
    new FxCopProfileExporter().exportProfile(profile, writer);

    TestUtils.assertSimilarXml(TestUtils.getResourceContent("/ProfileExporter/SimpleRules.FxCop.exported"), writer.toString());
  }

}