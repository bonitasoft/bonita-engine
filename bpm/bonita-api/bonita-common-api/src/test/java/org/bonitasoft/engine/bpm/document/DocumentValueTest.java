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
package org.bonitasoft.engine.bpm.document;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Celine Souchet
 * @version 6.4.2
 * @since 6.4.2
 */
public class DocumentValueTest {

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#DocumentValue(byte[], java.lang.String, java.lang.String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void cant_construct_DocumentValue_with_content_and_mimeType_without_file_name() {
        new DocumentValue("content".getBytes(), "mimeType", null);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#DocumentValue(byte[], java.lang.String, java.lang.String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void cant_construct_DocumentValue_with_content_and_mimeType_with_empty_file_name() {
        new DocumentValue("content".getBytes(), "mimeType", "");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#DocumentValue(byte[], java.lang.String, java.lang.String)}.
     */
    @Test
    public final void should_be_able_to_construct_DocumentValue_without_content_but_with_mimeType_and_file_name() {
        new DocumentValue(null, "mimeType", "filename");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#DocumentValue(byte[], java.lang.String, java.lang.String)}.
     */
    @Test
    public final void should_be_able_to_construct_DocumentValue_with_empty_content_with_mimeType_and_file_name() {
        new DocumentValue("".getBytes(), "mimeType", "filename");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#DocumentValue(byte[], java.lang.String, java.lang.String)}.
     */
    @Test
    public final void can_construct_DocumentValue_with_content_and_mimeType_and_file_name() {
        new DocumentValue("tyjiy".getBytes(), "mimeType", "filename");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#DocumentValue(long, byte[], java.lang.String, java.lang.String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void cant_construct_DocumentValue_with_documentId_and_content_and_mimeType_without_file_name() {
        new DocumentValue(1, "content".getBytes(), "mimeType", null);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#DocumentValue(long, byte[], java.lang.String, java.lang.String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void cant_construct_DocumentValue_with_documentId_and_content_and_mimeType_with_empty_file_name() {
        new DocumentValue(1, "content".getBytes(), "mimeType", "");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#DocumentValue(long, byte[], java.lang.String, java.lang.String)}.
     */
    @Test
    public final void should_be_able_to_construct_DocumentValue_without_content_with_documentId_and_mimeType_and_file_name() {
        new DocumentValue(1, null, "mimeType", "filename");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#DocumentValue(long, byte[], java.lang.String, java.lang.String)}.
     */
    @Test
    public final void should_be_able_to_construct_DocumentValue_with_empty_content_with_documentId_and_mimeType_and_file_name() {
        new DocumentValue(1, "".getBytes(), "mimeType", "filename");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#DocumentValue(long, byte[], java.lang.String, java.lang.String)}.
     */
    @Test
    public final void can_construct_DocumentValue_with_content_and_documentId_and_mimeType_and_file_name() {
        new DocumentValue(1, "plop".getBytes(), "mimeType", "filename");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#setContent(byte[])}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void setContent_should_throw_exception_if_filename_is_empty_and_content_not() {
        // Given
        final DocumentValue documentValue = new DocumentValue(2);

        // When
        documentValue.setContent("plop".getBytes());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#setContent(byte[])}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void setContent_should_throw_exception_if_filename_is_null_and_content_not() {
        // Given
        final DocumentValue documentValue = new DocumentValue(2);

        // When
        documentValue.setContent("plop".getBytes());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#setContent(byte[])}.
     */
    @Test
    public final void setContent_should_set_content_with_filename_and_content() {
        // Given
        final DocumentValue documentValue = new DocumentValue("yujyt".getBytes(), "mimeType", "filename");
        final byte[] content = "plop".getBytes();

        // When
        documentValue.setContent(content);

        // Then
        assertEquals(content, documentValue.getContent());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#setFileName(java.lang.String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void setFileName_should_throw_exception_if_filename_is_empty_and_content_not() {
        // Given
        final DocumentValue documentValue = new DocumentValue("yujyt".getBytes(), "mimeType", "filename");

        // When
        documentValue.setFileName("");
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#setFileName(java.lang.String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void setFileName_should_throw_exception_if_filename_is_null_and_content_not() {
        // Given
        final DocumentValue documentValue = new DocumentValue("yujyt".getBytes(), "mimeType", "filename");

        // When
        documentValue.setFileName(null);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.document.DocumentValue#setFileName(java.lang.String)}.
     */
    @Test
    public final void setFileName_should_set_content_with_filename_and_content() {
        // Given
        final DocumentValue documentValue = new DocumentValue("yujyt".getBytes(), "mimeType", "filename");
        final String fileName = "new";

        // When
        documentValue.setFileName(fileName);

        // Then
        assertEquals(fileName, documentValue.getFileName());
    }

}
