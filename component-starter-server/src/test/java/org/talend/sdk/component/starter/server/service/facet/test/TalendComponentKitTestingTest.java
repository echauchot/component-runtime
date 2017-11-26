/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.talend.sdk.component.starter.server.service.facet.test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.talend.sdk.component.starter.server.test.Resources.resourceFileToString;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.meecrowave.junit.MonoMeecrowave;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.talend.sdk.component.starter.server.service.domain.Build;
import org.talend.sdk.component.starter.server.service.domain.ProjectRequest;
import org.talend.sdk.component.starter.server.service.facet.FacetGenerator;
import org.talend.sdk.component.starter.server.service.facet.testing.TalendComponentKitTesting;

@RunWith(MonoMeecrowave.Runner.class)
public class TalendComponentKitTestingTest {

    @Inject
    private TalendComponentKitTesting generator;

    private Build build = new Build("test", "src/main/java", "src/test/java", "src/main/resources",
            "src/test/resources", "src/main/webapp", "pom.xml", "some pom", "target");

    @Test
    public void testSourceWithoutConf() {
        final Set<ProjectRequest.SourceConfiguration> sources =
                singleton(new ProjectRequest.SourceConfiguration("mycomp", "", false,
                        new ProjectRequest.DataStructure(emptySet()),
                        new ProjectRequest.StructureConfiguration(null, true)));

        String testFile = generator
                .create("foo.bar", build, emptyList(), sources, emptyList())
                .map(i -> new String(i.getContent(), StandardCharsets.UTF_8))
                .findFirst()
                .orElse(null);

        assertEquals(
                resourceFileToString("generated/TalendComponentKitTesting/testSourceWithoutConf/MycompSourceTest.java"),
                testFile);

    }

    @Test
    public void testSourceWithConf() {
        final Set<ProjectRequest.SourceConfiguration> sources =
                singleton(new ProjectRequest.SourceConfiguration("mycomp", "", false, complexConfig(),
                        new ProjectRequest.StructureConfiguration(null, true)));

        String testFile = generator
                .create("foo.bar", build, emptyList(), sources, emptyList())
                .map(i -> new String(i.getContent(), StandardCharsets.UTF_8))
                .findFirst()
                .orElse(null);

        assertEquals(
                resourceFileToString("generated/TalendComponentKitTesting/testSourceWithConf/MycompSourceTest.java"),
                testFile);
    }

    @Test
    public void testSourceWithNonGenericOutput() {
        final Set<ProjectRequest.SourceConfiguration> sources =
                singleton(new ProjectRequest.SourceConfiguration("mycomp", "", false, complexConfig(),
                        new ProjectRequest.StructureConfiguration(complexConfig(), false)));

        String testFile = generator
                .create("foo.bar", build, emptyList(), sources, emptyList())
                .map(i -> new String(i.getContent(), StandardCharsets.UTF_8))
                .findFirst()
                .orElse(null);

        assertEquals(
                resourceFileToString(
                        "generated/TalendComponentKitTesting/testSourceWithNonGenericOutput/MycompSourceTest.java"),
                testFile);
    }

    @Test
    public void testProcessorWithoutConf() {
        final Set<ProjectRequest.ProcessorConfiguration> processors = singleton(
                new ProjectRequest.ProcessorConfiguration("mycomp", "", new ProjectRequest.DataStructure(emptySet()),
                        singletonMap("__default__", new ProjectRequest.StructureConfiguration(null, true)),
                        singletonMap("__default__", new ProjectRequest.StructureConfiguration(null, true))));

        String testFile = generator
                .create("foo.bar", build, emptyList(), emptyList(), processors)
                .map(i -> new String(i.getContent(), StandardCharsets.UTF_8))
                .findFirst()
                .orElse(null);

        assertEquals(
                resourceFileToString(
                        "generated/TalendComponentKitTesting/testProcessorWithoutConf/MycompProcessorTest.java"),
                testFile);

    }

    @Test
    public void testProcessorWithConf() {
        final Set<ProjectRequest.ProcessorConfiguration> processors =
                singleton(new ProjectRequest.ProcessorConfiguration("mycomp", "", complexConfig(),
                        singletonMap("__default__", new ProjectRequest.StructureConfiguration(null, true)),
                        singletonMap("__default__", new ProjectRequest.StructureConfiguration(null, true))));

        Map<String, String> files = generator.create("foo.bar", build, emptyList(), emptyList(), processors).collect(
                toMap(FacetGenerator.InMemoryFile::getPath, i -> new String(i.getContent(), StandardCharsets.UTF_8)));

        String testFile = generator
                .create("foo.bar", build, emptyList(), emptyList(), processors)
                .map(i -> new String(i.getContent(), StandardCharsets.UTF_8))
                .findFirst()
                .orElse(null);

        assertEquals(
                resourceFileToString(
                        "generated/TalendComponentKitTesting/testProcessorWithConf/MycompProcessorTest.java"),
                testFile);
    }

    @Test
    public void testProcessorWithNonGenericOutput() {
        final Set<ProjectRequest.ProcessorConfiguration> processors =
                singleton(new ProjectRequest.ProcessorConfiguration("mycomp", "", complexConfig(),
                        new HashMap<String, ProjectRequest.StructureConfiguration>() {

                            {
                                put("__default__", new ProjectRequest.StructureConfiguration(complexConfig(), false));
                                put("input2", new ProjectRequest.StructureConfiguration(complexConfig(), false));
                                put("input3", new ProjectRequest.StructureConfiguration(complexConfig(), false));
                            }
                        }, new HashMap<String, ProjectRequest.StructureConfiguration>() {

                            {
                                put("__default__", new ProjectRequest.StructureConfiguration(complexConfig(), false));
                                put("reject", new ProjectRequest.StructureConfiguration(complexConfig(), false));
                            }
                        }));

        String testFile = generator
                .create("foo.bar", build, emptyList(), emptyList(), processors)
                .map(i -> new String(i.getContent(), StandardCharsets.UTF_8))
                .findFirst()
                .orElse(null);

        assertEquals(resourceFileToString(
                "generated/TalendComponentKitTesting/testProcessorWithNonGenericOutput/MycompProcessorTest.java"),
                testFile);
    }

    private ProjectRequest.DataStructure complexConfig() {
        return new ProjectRequest.DataStructure(asList(new ProjectRequest.Entry("host", "string", null),
                new ProjectRequest.Entry("port", "string", null),
                new ProjectRequest.Entry("credential", "object",
                        new ProjectRequest.DataStructure(asList(new ProjectRequest.Entry("username", "string", null),
                                new ProjectRequest.Entry("password", "string", null))))));
    }

}
