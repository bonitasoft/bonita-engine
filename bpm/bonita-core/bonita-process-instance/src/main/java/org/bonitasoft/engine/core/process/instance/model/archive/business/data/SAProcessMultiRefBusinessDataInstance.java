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

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;

/**
 * @author Emmanuel Duchastenier
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("proc_multi_ref")
public class SAProcessMultiRefBusinessDataInstance extends SARefBusinessDataInstance {

    @Column(name = "orig_proc_inst_id")
    private long processInstanceId;
    @ElementCollection
    @CollectionTable(name = "arch_multi_biz_data", joinColumns = {
            @JoinColumn(name = "id", referencedColumnName = "id"),
            @JoinColumn(name = "tenantid", referencedColumnName = "tenantid") })
    @OrderColumn(name = "idx")
    @Column(name = "data_id")
    private List<Long> dataIds;

    @Override
    public SRefBusinessDataInstance toSRefBusinessDataInstance() {
        SProcessMultiRefBusinessDataInstance refBusinessDataInstance = new SProcessMultiRefBusinessDataInstance();
        refBusinessDataInstance.setId(getSourceObjectId());
        refBusinessDataInstance.setTenantId(tenantId);
        refBusinessDataInstance.setName(getName());
        refBusinessDataInstance.setDataClassName(getDataClassName());
        refBusinessDataInstance.setDataIds(getDataIds());
        refBusinessDataInstance.setProcessInstanceId(processInstanceId);
        return refBusinessDataInstance;
    }
}
