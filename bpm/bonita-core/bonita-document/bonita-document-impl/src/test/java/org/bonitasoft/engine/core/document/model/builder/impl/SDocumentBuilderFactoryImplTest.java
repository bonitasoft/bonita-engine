/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.document.model.builder.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @version 6.4.2
 * @since 6.4.2
 */
@RunWith(MockitoJUnitRunner.class)
public class SDocumentBuilderFactoryImplTest {

    @InjectMocks
    private SDocumentBuilderFactoryImpl sDocumentBuilderFactoryImpl;

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.document.model.builder.impl.SDocumentBuilderFactoryImpl#createNewProcessDocument(java.lang.String, java.lang.String, long, byte[])}
     * .
     */
    @Test(expected = IllegalArgumentException.class)
    public final void createNewProcessDocument_should_throw_exception_if_no_file_name() {
        // Given
        final String fileName = null;
        final String mimeType = "mimeType";
        final long authorId = 3;
        final byte[] content = "content".getBytes();

        // When
        sDocumentBuilderFactoryImpl.createNewProcessDocument(fileName, mimeType, authorId, content);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#DocumentValue(byte[], java.lang.String, java.lang.String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void createNewProcessDocument_should_throw_exception_if_empty_file_name() {
        // Given
        final String fileName = "";
        final String mimeType = "mimeType";
        final long authorId = 3;
        final byte[] content = "content".getBytes();

        // When
        sDocumentBuilderFactoryImpl.createNewProcessDocument(fileName, mimeType, authorId, content);
    }

    @Test
    public final void createNewProcessDocument_should_be_valid_if_no_content_but_with_filename() {
        // Given
        final String fileName = "filename";
        final String mimeType = "mimeType";
        final long authorId = 3;
        final byte[] content = null;

        // When
        final SDocumentBuilder newProcessDocument = sDocumentBuilderFactoryImpl.createNewProcessDocument(fileName, mimeType, authorId, content);

        // then
        assertThat(newProcessDocument).isNotNull();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#DocumentValue(byte[], java.lang.String, java.lang.String)}.
     */
    @Test
    public final void createNewProcessDocument_should_be_valid_if_empty_content_but_with_filename() {
        // Given
        final String fileName = "filename";
        final String mimeType = "mimeType";
        final long authorId = 3;
        final byte[] content = "".getBytes();

        // When
        final SDocumentBuilder newProcessDocument = sDocumentBuilderFactoryImpl.createNewProcessDocument(fileName, mimeType, authorId, content);

        // then
        assertThat(newProcessDocument).isNotNull();
    }

}
