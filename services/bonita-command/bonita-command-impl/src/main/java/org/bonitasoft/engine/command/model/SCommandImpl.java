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
package org.bonitasoft.engine.command.model;

import org.bonitasoft.engine.command.api.impl.CommandDeployment;

/**
 * @author Matthieu Chaffotte
 */
public class SCommandImpl extends CommandDeployment implements SCommand {

    private static final long serialVersionUID = 4257969847115435401L;

    private long tenantId;

    private long id;

    private boolean system;

    public SCommandImpl() {
    }

    public SCommandImpl(final String name, final String description, final String implementation) {
        super(name, description, implementation);
    }

    public SCommandImpl(final SCommand command) {
        super(command.getName(), command.getDescription(), command.getImplementation());
        this.id = command.getId();
        this.system = command.getSystem();
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    @Override
    public String getDiscriminator() {
        return SCommandImpl.class.getName();
    }

    public long getTenantId() {
        return this.tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SCommandImpl))
            return false;
        if (!super.equals(o))
            return false;

        final SCommandImpl sCommand = (SCommandImpl) o;

        if (id != sCommand.id)
            return false;
        if (system != sCommand.system)
            return false;
        if (tenantId != sCommand.tenantId)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (tenantId ^ (tenantId >>> 32));
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (system ? 1 : 0);
        return result;
    }

    @Override
    public boolean getSystem() {
        return system;
    }

}
