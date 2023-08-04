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
package org.bonitasoft.engine.core.process.comment.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Elias Ricken de Medeiros
 */
@Data
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@IdClass(PersistentObjectId.class)
@DiscriminatorColumn(name = "kind")
@Table(name = "process_comment")
public class SComment implements PersistentObject {

    public static final String ID_KEY = "id";
    public static final String USERID_KEY = "userId";
    public static final String PROCESSINSTANCEID_KEY = "processInstanceId";
    public static final String POSTDATE_KEY = "postDate";
    public static final String CONTENT_KEY = "content";
    public static final String KIND_KEY = "kind";
    @Id
    private long id;
    @Id
    private long tenantId;
    @Column
    private Long userId;
    @Column
    private long processInstanceId;
    @Column
    private long postDate;
    @Column
    private String content;

    public SComment(final long processInstanceId, final String content) {
        super();
        this.processInstanceId = processInstanceId;
        this.content = content;
        this.postDate = System.currentTimeMillis();
    }

}
