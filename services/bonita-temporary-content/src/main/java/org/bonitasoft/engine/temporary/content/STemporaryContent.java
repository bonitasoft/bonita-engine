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

import java.sql.Blob;
import java.util.UUID;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.engine.persistence.PlatformPersistentObject;

/**
 * @author Haroun EL ALAMI
 */
@Data
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "temporary_content")
@Cacheable(false)
public class STemporaryContent implements PlatformPersistentObject {

    @Id
    private long id;

    /**
     * Date when the temporary content was added
     */
    @Column
    private long creationDate;

    /**
     * Unique identifier sent to the client after upload is complete to retrieve the file
     */
    @Column(name = "key_")
    private String key;

    /**
     * Original uploaded file name
     */
    @Column
    private String fileName;

    /**
     * The mimeType of the file
     */
    @Column
    private String mimeType;

    /**
     * Content of the file uploaded (accessed with Streams)
     */
    @Lob
    private Blob content;

    public STemporaryContent(final String fileName, Blob content, String mimeType) {
        super();
        String fileExtension = FilenameUtils.getExtension(fileName);
        String keyUUID = UUID.randomUUID().toString();
        this.key = StringUtils.isNotBlank(fileExtension) ? keyUUID + "." + fileExtension : keyUUID;
        this.fileName = fileName;
        this.content = content;
        this.creationDate = System.currentTimeMillis();
        this.mimeType = mimeType;
    }

    @Override
    public void setTenantId(long id) {
        //no tenant id
    }
}
