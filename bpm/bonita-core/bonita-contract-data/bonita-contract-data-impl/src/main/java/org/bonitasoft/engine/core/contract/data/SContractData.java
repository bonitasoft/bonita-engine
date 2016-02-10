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
package org.bonitasoft.engine.core.contract.data;

import java.io.Serializable;

import org.bonitasoft.engine.data.instance.model.impl.XStreamFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Matthieu Chaffotte
 */
public abstract class SContractData extends PersistentObjectId implements PersistentObject {

    private static final long serialVersionUID = 4666337073276985147L;

    private String name;

    String value;

    public long getScopeId() {
        return scopeId;
    }

    public void setScopeId(long scopeId) {
        this.scopeId = scopeId;
    }

    private long scopeId;

    public SContractData() {
        super();
    }

    public SContractData(final String name, final Serializable value, long scopeId) {
        super();
        this.name = name;
        this.value = convert(value);
        this.scopeId = scopeId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Serializable getValue() {
        return revert(value);
    }

    public void setValue(final Serializable value) {
        this.value = convert(value);
    }


    private String convert(final Serializable value) {
        return XStreamFactory.getXStream().toXML(value);
    }

    private Serializable revert(final String value) {
        if (value != null) {
            return (Serializable) XStreamFactory.getXStream().fromXML(value);
        }
        return null;
    }

    @Override
    public String getDiscriminator() {
        return SContractData.class.getName();
    }

}
