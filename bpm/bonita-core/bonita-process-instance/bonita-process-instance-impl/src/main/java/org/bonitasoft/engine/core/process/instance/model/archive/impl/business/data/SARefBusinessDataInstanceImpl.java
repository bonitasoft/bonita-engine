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
package org.bonitasoft.engine.core.process.instance.model.archive.impl.business.data;

import org.bonitasoft.engine.core.process.instance.model.archive.business.data.SARefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.SAPersistenceObjectImpl;

/**
 * @author Emmanuel Duchastenier
 */
public abstract class SARefBusinessDataInstanceImpl extends SAPersistenceObjectImpl implements SARefBusinessDataInstance {

    private static final long serialVersionUID = 6616497495062704471L;

    private String name;

    private String dataClassName;

    public SARefBusinessDataInstanceImpl() {
        super();
    }

    @Override
    public String getDiscriminator() {
        return SARefBusinessDataInstanceImpl.class.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDataClassName() {
        return dataClassName;
    }

    public void setDataClassName(final String dataClassName) {
        this.dataClassName = dataClassName;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SRefBusinessDataInstance.class;
    }

}
