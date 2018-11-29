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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.command.api.impl.CommandDeployment;

/**
 * @author Matthieu Chaffotte
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SCommandImpl extends CommandDeployment implements SCommand {

    private long tenantId;
    private long id;
    private boolean system;

    public SCommandImpl(final String name, final String description, final String implementation) {
        super(name, description, implementation);
    }

    public SCommandImpl(final SCommand command) {
        super(command.getName(), command.getDescription(), command.getImplementation());
        this.id = command.getId();
        this.system = command.getSystem();
    }

    @Override
    public boolean getSystem() {
        return system;
    }

}
