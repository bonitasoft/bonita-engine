/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.temporary.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.SQLException;

import javax.inject.Inject;

import org.bonitasoft.engine.test.persistence.repository.TemporaryContentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Haroun EL ALAMI
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class TemporaryContentQueryTest {

    @Inject
    private TemporaryContentRepository repository;

    @Test
    public void shouldStoreStream() throws IOException, SQLException {
        // given
        Path tempFilePath = Files.createTempFile("tempFile", ".txt");
        Files.writeString(tempFilePath, "test");

        String originalFileName = "originalFileName";
        FileInputStream fis = new FileInputStream(tempFilePath.toFile());
        Blob data = repository.getSession().getLobHelper().createBlob(fis, fis.available());
        String mimeType = "text/plain";

        // save
        STemporaryContent temporaryContent = new STemporaryContent(originalFileName, data, mimeType);

        //when
        STemporaryContent savedTemporaryContent = repository.add(temporaryContent);

        InputStream uploadedFileStream = savedTemporaryContent.getContent().getBinaryStream();
        assertThat(savedTemporaryContent.getFileName()).isEqualTo(temporaryContent.getFileName());
        assertThat(savedTemporaryContent.getMimeType()).isEqualTo(temporaryContent.getMimeType());
        final byte[] contentByteArray = uploadedFileStream.readAllBytes();
        assertThat(tempFilePath.toFile().length()).isEqualTo(contentByteArray.length);
        assertArrayEquals(contentByteArray, "test".getBytes());
    }

    @Test
    public void shouldCleanOutDatedTemporaryContent() {
        // given
        String originalFileName = "originalFileName";
        String mimeType = "text/plain";

        // save 3 files
        STemporaryContent temporaryContent1 = new STemporaryContent(originalFileName, null, mimeType);
        temporaryContent1.setCreationDate(1000000);
        temporaryContent1 = repository.add(temporaryContent1);

        STemporaryContent temporaryContent2 = new STemporaryContent(originalFileName, null, mimeType);
        temporaryContent2.setCreationDate(2000000);
        repository.add(temporaryContent2);

        STemporaryContent temporaryContent3 = new STemporaryContent(originalFileName, null, mimeType);
        temporaryContent3.setCreationDate(3000000);
        temporaryContent3 = repository.add(temporaryContent3);

        //when cleanOutDatedTemporaryContent
        int deletedRows = repository.cleanOutDatedTemporaryContent(temporaryContent1.getCreationDate() + 1);
        //then
        assertThat(deletedRows).isEqualTo(1);
        //when cleanOutDatedTemporaryContent
        deletedRows = repository.cleanOutDatedTemporaryContent(temporaryContent3.getCreationDate() + 1);
        //then
        assertThat(deletedRows).isEqualTo(2);
    }
}
