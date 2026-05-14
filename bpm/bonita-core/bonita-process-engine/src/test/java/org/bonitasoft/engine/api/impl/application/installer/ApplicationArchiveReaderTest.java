/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.impl.application.installer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.bonitasoft.engine.io.FileOperations.asInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.bonitasoft.engine.api.impl.application.installer.detector.*;
import org.junit.Test;

/**
 * @author Baptiste Mesta.
 */
public class ApplicationArchiveReaderTest {

    ArtifactTypeDetector artifactTypeDetector = new ArtifactTypeDetector(new BdmDetector(),
            new LivingApplicationDetector(), new OrganizationDetector(), new CustomPageDetector(),
            new ProcessDetector(), new ThemeDetector(), new PageAndFormDetector(), new LayoutDetector(),
            new IconDetector());
    private final ApplicationArchiveReader applicationArchiveReader = new ApplicationArchiveReader(
            artifactTypeDetector);

    @Test
    public void should_read_application_archive_with_a_live_application() throws Exception {
        byte[] zip = zip(
                file("apps/MyApp.xml",
                        "<applications xmlns=\"http://documentation.bonitasoft.com/application-xml-schema/1.1\"></applications>"));

        ApplicationArchive applicationArchive = applicationArchiveReader.read(asInputStream(zip));

        assertThat(applicationArchive.getApplications().size()).isEqualTo(1);
        assertThat(applicationArchive.getApplications().get(0).getName()).isEqualTo("MyApp.xml");
        assertThat(new String(Files.readAllBytes(applicationArchive.getApplications().get(0).toPath()))).contains(
                "<applications xmlns=\"http://documentation.bonitasoft.com/application-xml-schema/1.1\"></applications>");

    }

    @Test
    public void should_read_application_archive_with_pages() throws IOException {
        byte[] zip = zip(
                file("pages/myCustomPage1.zip",
                        zip(file("page.properties", "name=custompage_test1\ncontentType=page"),
                                file("resources/index.html", "someContent"))),
                file("pages/myCustomPage2.zip",
                        zip(file("page.properties", "name=custompage_test2\ncontentType=page"),
                                file("resources/Index.groovy", "someContent"))),
                file("pages/myCustomPage3.zip", zip(file("page.properties", "name=custompage_test3\ncontentType=page"))) //ignored, no index
        );

        ApplicationArchive applicationArchive = applicationArchiveReader.read(zip);

        assertThat(applicationArchive.getPages()).hasSize(2);
        List<java.io.File> pageList = applicationArchive.getPages().stream().sorted().collect(Collectors.toList());
        File firstPage = pageList.get(0);
        File secondPage = pageList.get(1);

        assertThat(firstPage.getName()).contains("myCustomPage1.zip");
        assertThat(Files.readAllBytes(firstPage.toPath()))
                .containsExactly(zip(file("page.properties", "name=custompage_test1\ncontentType=page"),
                        file("resources/index.html", "someContent")));
        assertThat(secondPage.getName()).contains("myCustomPage2.zip");
        assertThat(Files.readAllBytes(secondPage.toPath()))
                .containsExactly(zip(file("page.properties", "name=custompage_test2\ncontentType=page"),
                        file("resources/Index.groovy", "someContent")));
    }

    @Test
    public void should_read_application_archive_with_layout() throws IOException {
        byte[] layout = zip(file("page.properties", "name=custompage_test1\ncontentType=layout"),
                file("resources/index.html", "someContent"));
        byte[] zip = zip(
                file("layout.zip", layout));

        ApplicationArchive applicationArchive = applicationArchiveReader.read(asInputStream(zip));

        List<java.io.File> layouts = applicationArchive.getLayouts();
        assertThat(layouts.size()).isEqualTo(1);
        assertThat(layouts.get(0).getName()).contains("layout.zip");
        assertThat(Files.readAllBytes(layouts.get(0).toPath())).containsExactly(layout);
    }

    @Test
    public void should_read_application_archive_with_theme() throws IOException {
        byte[] themeContent = zip(file("page.properties", "name=custompage_test1\ncontentType=theme"),
                file("resources/theme.css", "someContent"));
        byte[] zip = zip(
                file("theme.zip", themeContent));

        ApplicationArchive applicationArchive = applicationArchiveReader.read(asInputStream(zip));

        List<java.io.File> themes = applicationArchive.getThemes();
        assertThat(themes.size()).isEqualTo(1);
        assertThat(themes.get(0).getName()).contains("theme.zip");
        assertThat(Files.readAllBytes(themes.get(0).toPath())).containsExactly(themeContent);

    }

    @Test
    public void should_read_application_archive_with_apiExtension() throws IOException {
        byte[] apiExtensionContent = zip(file("page.properties", "name=custompage_test1\ncontentType=apiExtension"));
        byte[] zip = zip(
                file("apiExtension.zip", apiExtensionContent));

        ApplicationArchive applicationArchive = applicationArchiveReader.read(asInputStream(zip));

        List<java.io.File> restAPIExtensions = applicationArchive.getRestAPIExtensions();
        assertThat(restAPIExtensions.size()).isEqualTo(1);
        assertThat(restAPIExtensions.get(0).getName()).contains("apiExtension.zip");
        assertThat(Files.readAllBytes(restAPIExtensions.get(0).toPath())).containsExactly(apiExtensionContent);
    }

    @Test
    public void should_read_application_archive_with_process() throws IOException {
        byte[] barContent = zip(file("process-design.xml",
                "<process xmlns=\"http://www.bonitasoft.org/ns/process/client/\"></process>"));
        byte[] zip = zip(
                file("myprocess.bar", barContent));

        ApplicationArchive applicationArchive = applicationArchiveReader.read(asInputStream(zip));

        List<java.io.File> processes = applicationArchive.getProcesses();
        assertThat(processes.size()).isEqualTo(1);
        assertThat(processes.get(0).getName()).contains("myprocess.bar");
        assertThat(Files.readAllBytes(processes.get(0).toPath())).containsExactly(barContent);
    }

}
