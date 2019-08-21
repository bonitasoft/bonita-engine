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
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.bonitasoft.engine.io.FileAndContentUtils.*;
import static org.bonitasoft.engine.io.FileOperations.asInputStream;
import static org.bonitasoft.engine.api.impl.application.deployer.validator.ArtifactValidatorFactory.artifactValidator;

import java.io.InputStream;

import org.bonitasoft.engine.api.impl.application.deployer.model.Application;
import org.junit.After;
import org.junit.Test;

/**
 * @author Baptiste Mesta.
 */
public class ApplicationArchiveReaderTest {

    private ApplicationArchiveReader applicationArchiveReader = new ApplicationArchiveReader(artifactValidator());
    private ApplicationArchive applicationArchive;

    @After
    public void after() throws Exception {
        if (applicationArchive != null) {
            applicationArchive.close();
        }
    }

    @Test
    public void should_throw_exception_when_inputstream_is_not_a_zip() {
        InputStream byteArrayInputStream = asInputStream(new byte[] { 1, 2, 3 });

        Throwable thrown = catchThrowable(() -> applicationArchiveReader.read(byteArrayInputStream));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Application archive is empty or is not a valid file");
    }

    @Test
    public void should_generate_descriptor_when_archive_do_not_contains_descriptor() throws Exception {
        byte[] zip = zip(file("MyApp.xml", "<applications></applications>"));
        InputStream byteArrayInputStream = asInputStream(zip);

        applicationArchive = applicationArchiveReader.read(byteArrayInputStream);

        assertThat(applicationArchive.getDeploymentDescriptor()).isNotNull();
    }

    @Test
    public void should_read_application_from_zip() throws Exception {
        byte[] zip = zip(file("deploy.json", "{}"), file("apps/MyApp.xml", "<applications></applications>"));

        applicationArchive = applicationArchiveReader.read(asInputStream(zip));

        assertThat(applicationArchive.getDeploymentDescriptor()).isNotNull();
    }

    @Test
    public void should_read_application_with_applicationFile_as_stream() throws Exception {
        String deployJsonContent = "{\n" +
                " \"name\":\"LeaveRequest\",\n" +
                " \"version\":\"1.0.0-SNAPSHOT\",\n" +
                " \"description\":\"Description of foo is bar\",\n" +
                " \"targetVersion\":\"7.2.0\",\n" +
                " \"applications\":[\n" +
                "  {\n" +
                "   \"file\":\"apps/MyApp.xml\",\n" +
                "   \"policy\":\"FAIL_ON_DUPLICATES\"\n" +
                "  }\n" +
                " ],\n" +
                " \"modelVersion\":\"0.1\"\n" +
                "}";
        byte[] zip = zip(file("deploy.json", deployJsonContent),
                directory("apps/"),
                file("apps/MyApp.xml",
                        "<applications xmlns=\"http://documentation.bonitasoft.com/application-xml-schema/1.0\"></applications>"));

        applicationArchive = applicationArchiveReader.read(asInputStream(zip));

        Application application = applicationArchive.getDeploymentDescriptor().getApplications().get(0);
        assertThat(applicationArchive.getFile(application.getFile())).hasContent(
                "<applications xmlns=\"http://documentation.bonitasoft.com/application-xml-schema/1.0\"></applications>");
    }

    @Test
    public void should_read_application_with_applicationFile_as_file() throws Exception {
        String deployJsonContent = "{\n" +
                " \"name\":\"LeaveRequest\",\n" +
                " \"version\":\"1.0.0-SNAPSHOT\",\n" +
                " \"description\":\"Description of foo is bar\",\n" +
                " \"targetVersion\":\"7.2.0\",\n" +
                " \"applications\":[\n" +
                "  {\n" +
                "   \"file\":\"apps/MyApp.xml\",\n" +
                "   \"policy\":\"FAIL_ON_DUPLICATES\"\n" +
                "  }\n" +
                " ],\n" +
                " \"modelVersion\":\"0.1\"\n" +
                "}";
        byte[] zip = zip(file("deploy.json", deployJsonContent),
                file("apps/MyApp.xml",
                        "<applications xmlns=\"http://documentation.bonitasoft.com/application-xml-schema/1.0\"></applications>"));

        applicationArchive = applicationArchiveReader.read(zip);

        Application application = applicationArchive.getDeploymentDescriptor().getApplications().get(0);
        assertThat(applicationArchive.getFile(application.getFile())).hasContent(
                "<applications xmlns=\"http://documentation.bonitasoft.com/application-xml-schema/1.0\"></applications>");
    }

}
