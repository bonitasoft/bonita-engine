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
package org.bonitasoft.engine.core.process.instance.model.archive.business.data;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Emmanuel Duchastenier
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "arch_ref_biz_data_inst")
@IdClass(PersistentObjectId.class)
@DiscriminatorColumn(name = "kind")
public abstract class SARefBusinessDataInstance implements ArchivedPersistentObject {

    @Id
    protected long id;
    @Id
    protected long tenantId;
    private String name;
    @Column(name = "data_classname")
    private String dataClassName;

    @Override
    public long getSourceObjectId() {
        // WARNING
        // There is no column sourceObjectId, return the id of the column
        // This is an error. we should have this column
        return getId();
    }

    @Override
    public long getArchiveDate() {
        //There is no archiveDate column
        return 0;
    }

    public void setSourceObjectId(long id) {
        //Nothing to do, not persisted
    }

    public void setArchiveDate(long id) {
        //Nothing to do, not persisted
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SRefBusinessDataInstance.class;
    }

    public abstract SRefBusinessDataInstance toSRefBusinessDataInstance();

}
