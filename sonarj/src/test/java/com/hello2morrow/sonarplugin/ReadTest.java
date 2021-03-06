/*
 * Sonar SonarJ Plugin
 * Copyright (C) 2009, 2010 hello2morrow GmbH
 * mailto: info AT hello2morrow DOT com
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

package com.hello2morrow.sonarplugin;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;

import com.hello2morrow.sonarplugin.xsd.ReportContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;

public class ReadTest extends TestCase
{
    private Mockery context = new Mockery();
    
    public void testAnalyse()
    {
        ReportContext report = SonarJSensor.readSonarjReport("src/test/resources/sonarj-report.xml", "");
        
        assertNotNull(report);

        final Configuration config = context.mock(Configuration.class);
        
        context.checking(new Expectations() {{
            oneOf(config).getDouble(SonarJSensor.COST_PER_INDEX_POINT, 12.0); will(returnValue(12.0));
            }});

        SonarJSensor sensor = new SonarJSensor(config, null, null);

        final SensorContext sensorContext = context.mock(SensorContext.class);
        
        context.checking(new Expectations() {{
            atLeast(1).of(sensorContext).saveMeasure(with(any(Measure.class)));
            allowing(sensorContext).getResource(with(any(Resource.class))); will(returnValue(null)); 
            allowing(sensorContext).getMeasure(with(any(Metric.class))); will(returnValue(null)); 
            }});

    
        final IProject project = context.mock(IProject.class);

        context.checking(new Expectations() {{
            allowing(project).getArtifactId(); will(returnValue("simple-model"));
            allowing(project).getName(); will(returnValue("sonar-plugin-api"));
            allowing(project).getGroupId(); will(returnValue("sonar"));
            }});
        
        sensor.analyse(project, sensorContext, report);
        
        context.assertIsSatisfied();
    }
}
