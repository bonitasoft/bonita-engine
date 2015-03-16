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
package org.bonitasoft.engine.command.api.record;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;

/**
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class SelectDescriptorBuilder {

    public static SelectOneDescriptor<SCommand> getCommandByName(final String commandName) {
        final Map<String, Object> parameters = Collections.singletonMap("name", (Object) commandName);
        return new SelectOneDescriptor<SCommand>("getCommandByName", parameters, SCommand.class);
    }

    public static SelectListDescriptor<SCommand> getCommands(final String field, final OrderByType order, final int fromIndex, final int numberOfElements) {
        final Map<String, Object> parameters = Collections.emptyMap();
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, SCommand.class, field, order);
        return new SelectListDescriptor<SCommand>("getCommands", parameters, SCommand.class, queryOptions);
    }

    public static SelectListDescriptor<SCommand> getUserCommands(final String field, final OrderByType order, final int fromIndex, final int numberOfElements) {
        final Map<String, Object> parameters = Collections.singletonMap("system", (Object) false);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, SCommand.class, field, order);
        return new SelectListDescriptor<SCommand>("getUserCommands", parameters, SCommand.class, queryOptions);
    }

    public static SelectByIdDescriptor<SCommand> getCommandById(final long commandId) {
        return new SelectByIdDescriptor<SCommand>("getCommandById", SCommand.class, commandId);
    }
}
