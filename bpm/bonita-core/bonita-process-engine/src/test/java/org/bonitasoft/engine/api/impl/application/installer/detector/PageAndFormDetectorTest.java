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
public class PageAndFormDetectorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void isCompliant_should_reject_page_if_contentType_is_not_layout() throws Exception {
        // given:
        final PageAndFormDetector detector = new PageAndFormDetector();
        byte[] zip = zip(
                file("page.properties", "contentType=toto"),
                file("resources/index.html", "anything"));

        // when:
        final boolean compliant = detector.isCompliant(new FileAndContent("myPage.zip", zip));

        // then:
        assertThat(compliant).isFalse();
    }

    @Test
    public void isCompliant_should_reject_page_if_index_page_not_present() throws Exception {
        // given:
        final PageAndFormDetector detector = new PageAndFormDetector();
        byte[] zip = zip(file("page.properties", "contentType=layout"));

        // when:
        final boolean compliant = detector.isCompliant(new FileAndContent("myPage.zip", zip));

        // then:
        assertThat(compliant).isFalse();
    }

    @Test
    public void isCompliant_should_accept_valid_page() throws Exception {
        // given:
        byte[] zip = zip(
                file("page.properties", "contentType=page"),
                file("resources/index.html", "some HTML content"));

        // when:
        final boolean compliant = new PageAndFormDetector().isCompliant(new FileAndContent("myPage.zip", zip));

        // then:
        assertThat(compliant).isTrue();
    }

    @Test
    public void isCompliant_should_accept_valid_form() throws Exception {
        // given:
        byte[] zip = zip(
                file("page.properties", "contentType=form"),
                file("resources/Index.groovy", "some Groovy content"));

        // when:
        final boolean compliant = new PageAndFormDetector().isCompliant(new FileAndContent("myGroovyForm.zip", zip));

        // then:
        assertThat(compliant).isTrue();
    }

}
