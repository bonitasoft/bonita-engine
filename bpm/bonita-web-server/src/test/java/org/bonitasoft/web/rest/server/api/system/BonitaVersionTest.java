/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class BonitaVersionTest {

    @Mock
    private VersionFile file;

    @Mock
    private InputStream stream;

    @Test
    public void should_read_version_stream_to_return_its_content() throws Exception {
        final InputStream stream = IOUtils.toInputStream("1.0.0\n2021.2-u0\nBonitasoft © 2021", StandardCharsets.UTF_8);
        given(file.getStream()).willReturn(stream);

        final BonitaVersion version = new BonitaVersion(file);

        assertThat(version.getVersion()).isEqualTo("1.0.0");
        assertThat(version.getBrandingVersion()).isEqualTo("2021.2");
        assertThat(version.getBrandingVersionWithUpdate()).isEqualTo("2021.2-u0");
        assertThat(version.getCopyright()).isEqualTo("Bonitasoft © 2021");
        IOUtils.closeQuietly(stream);
    }

    @Test
    public void should_read_version_stream_to_return_its_version() throws Exception {
        final InputStream stream = IOUtils.toInputStream("1.0.0");
        given(file.getStream()).willReturn(stream);

        final BonitaVersion version = new BonitaVersion(file);

        assertThat(version.getVersion()).isEqualTo("1.0.0");
        IOUtils.closeQuietly(stream);
    }

    @Test
    public void should_trim_extra_new_line_character() {
        final InputStream stream = IOUtils.toInputStream("1.0.0\n", StandardCharsets.UTF_8);
        given(file.getStream()).willReturn(stream);

        final BonitaVersion version = new BonitaVersion(file);

        assertThat(version.getVersion()).isEqualTo("1.0.0");
        IOUtils.closeQuietly(stream);
    }

    @Test
    public void should_return_an_empty_version_when_file_is_invalid() {
        given(file.getStream()).willReturn(null);

        final BonitaVersion version = new BonitaVersion(file);

        assertThat(version.getVersion()).isEqualTo("");
        assertThat(version.getCopyright()).isEqualTo("");
    }

}
