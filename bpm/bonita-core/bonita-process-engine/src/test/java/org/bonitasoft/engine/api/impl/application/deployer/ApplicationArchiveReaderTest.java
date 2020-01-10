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
package org.bonitasoft.engine.api.impl.application.deployer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.bonitasoft.engine.io.FileOperations.asInputStream;

import java.io.IOException;

import org.assertj.core.groups.Tuple;
import org.junit.Test;

/**
 * @author Baptiste Mesta.
 */
public class ApplicationArchiveReaderTest {

    private ApplicationArchiveReader applicationArchiveReader = new ApplicationArchiveReader();

    @Test
    public void should_read_application_archive_with_a_live_application() throws Exception {
        byte[] zip = zip(
                file("apps/MyApp.xml",
                        "<applications xmlns=\"http://documentation.bonitasoft.com/application-xml-schema/1.0\"></applications>"));

        ApplicationArchive applicationArchive = applicationArchiveReader.read(asInputStream(zip));

        assertThat(applicationArchive.getApplications()).hasOnlyOneElementSatisfying(a -> {
            assertThat(a.getFileName()).isEqualTo("MyApp.xml");
            assertThat(new String(a.getContent())).contains(
                    "<applications xmlns=\"http://documentation.bonitasoft.com/application-xml-schema/1.0\"></applications>");
        });
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

        assertThat(applicationArchive.getPages()).hasSize(2)
                .extracting("fileName", "content")
                .containsExactly(
                        new Tuple("myCustomPage1.zip",
                                zip(file("page.properties", "name=custompage_test1\ncontentType=page"),
                                        file("resources/index.html", "someContent"))),
                        new Tuple("myCustomPage2.zip",
                                zip(file("page.properties", "name=custompage_test2\ncontentType=page"),
                                        file("resources/Index.groovy", "someContent"))));
    }

    @Test
    public void should_read_application_archive_with_layout() throws IOException {
        byte[] layout = zip(file("page.properties", "name=custompage_test1\ncontentType=layout"),
                file("resources/index.html", "someContent"));
        byte[] zip = zip(
                file("layout.zip", layout));

        ApplicationArchive applicationArchive = applicationArchiveReader.read(asInputStream(zip));

        assertThat(applicationArchive.getLayouts())
                .containsExactly(file("layout.zip", layout));
    }

    @Test
    public void should_read_application_archive_with_theme() throws IOException {
        byte[] themeContent = zip(file("page.properties", "name=custompage_test1\ncontentType=theme"),
                file("resources/theme.css", "someContent"));
        byte[] zip = zip(
                file("theme.zip", themeContent));

        ApplicationArchive applicationArchive = applicationArchiveReader.read(asInputStream(zip));

        assertThat(applicationArchive.getThemes())
                .containsExactly(file("theme.zip", themeContent));
    }

    @Test
    public void should_read_application_archive_with_apiExtension() throws IOException {
        byte[] apiExtensionContent = zip(file("page.properties", "name=custompage_test1\ncontentType=apiExtension"));
        byte[] zip = zip(
                file("apiExtension.zip", apiExtensionContent));

        ApplicationArchive applicationArchive = applicationArchiveReader.read(asInputStream(zip));

        assertThat(applicationArchive.getRestAPIExtensions())
                .containsExactly(file("apiExtension.zip", apiExtensionContent));
    }

    @Test
    public void should_read_application_archive_with_process() throws IOException {
        byte[] barContent = zip(file("process-design.xml",
                "<process xmlns=\"http://www.bonitasoft.org/ns/process/client/\"></process>"));
        byte[] zip = zip(
                file("myprocess.bar", barContent));

        ApplicationArchive applicationArchive = applicationArchiveReader.read(asInputStream(zip));

        assertThat(applicationArchive.getProcesses())
                .containsExactly(file("myprocess.bar", barContent));
    }
}
