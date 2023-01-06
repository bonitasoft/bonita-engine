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
package org.bonitasoft.engine.api.impl.application.installer.detector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;

import org.bonitasoft.engine.io.FileAndContent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Emmanuel Duchastenier
 */
public class LayoutDetectorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void isCompliant_should_reject_layout_if_contentType_is_not_layout() throws Exception {
        // given:
        final LayoutDetector detector = new LayoutDetector();
        byte[] zip = zip(
                file("page.properties", "contentType=toto"),
                file("resources/index.html", "anything"));

        // when:
        final boolean compliant = detector.isCompliant(new FileAndContent("myLayout.zip", zip));

        // then:
        assertThat(compliant).isFalse();
    }

    @Test
    public void isCompliant_should_reject_layout_if_index_page_not_present() throws Exception {
        // given:
        final LayoutDetector detector = new LayoutDetector();
        byte[] zip = zip(
                file("page.properties", "contentType=layout"),
                file("resources/toto.html", "anything"));

        // when:
        final boolean compliant = detector.isCompliant(new FileAndContent("myLayout.zip", zip));

        // then:
        assertThat(compliant).isFalse();
    }

    @Test
    public void isCompliant_should_accept_valid_layout() throws Exception {
        // given:
        final FileAndContent file = file("layout.zip",
                zip(file("page.properties", "name=custompage_test1\ncontentType=layout"),
                        file("resources/index.html", "someContent")));

        // when:
        final boolean compliant = new LayoutDetector().isCompliant(file);

        // then:
        assertThat(compliant).isTrue();
    }
}
