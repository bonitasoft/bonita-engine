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
package org.bonitasoft.engine.core.category.model;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Matthieu Chaffotte
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "processcategorymapping")
@IdClass(PersistentObjectId.class)
@Cacheable(false)
public class SProcessCategoryMapping implements PersistentObject {

    @Id
    private long tenantId;
    @Id
    private long id;
    @Column
    private long categoryId;
    @Column
    private long processId;

    public SProcessCategoryMapping(final long categoryId, final long processId) {
        this.categoryId = categoryId;
        this.processId = processId;
    }

}
