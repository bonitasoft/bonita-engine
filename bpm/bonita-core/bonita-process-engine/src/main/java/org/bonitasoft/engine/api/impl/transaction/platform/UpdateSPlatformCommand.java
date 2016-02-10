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
package org.bonitasoft.engine.api.impl.transaction.platform;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.command.CommandUpdater;
import org.bonitasoft.engine.command.CommandUpdater.CommandField;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.platform.command.PlatformCommandService;
import org.bonitasoft.engine.platform.command.model.SPlatformCommand;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandUpdateBuilder;
import org.bonitasoft.engine.platform.command.model.SPlatformCommandUpdateBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Zhang Bole
 */
public class UpdateSPlatformCommand implements TransactionContent {

    private final PlatformCommandService platformCommandService;

    private final String name;

    private final CommandUpdater updateDescriptor;

    public UpdateSPlatformCommand(final PlatformCommandService platformCommandService,
            final String name, final CommandUpdater updateDescriptor) {
        this.platformCommandService = platformCommandService;
        this.name = name;
        this.updateDescriptor = updateDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        final EntityUpdateDescriptor changeDescriptor = getCommandUpdateDescriptor();
        final SPlatformCommand sPlatformCommand = platformCommandService.getPlatformCommand(name);
        platformCommandService.update(sPlatformCommand, changeDescriptor);
    }

    private EntityUpdateDescriptor getCommandUpdateDescriptor() {
        final SPlatformCommandUpdateBuilder platformCommandUpdateBuilder = BuilderFactory.get(SPlatformCommandUpdateBuilderFactory.class).createNewInstance();
        final Map<CommandField, Serializable> fields = updateDescriptor.getFields();
        for (final Entry<CommandField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case NAME:
                    platformCommandUpdateBuilder.updateName((String) field.getValue());
                    break;
                case DESCRIPTION:
                    platformCommandUpdateBuilder.updateDescription((String) field.getValue());
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        return platformCommandUpdateBuilder.done();
    }

}
