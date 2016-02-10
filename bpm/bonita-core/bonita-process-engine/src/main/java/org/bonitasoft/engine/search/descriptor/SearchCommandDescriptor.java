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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.command.CommandSearchDescriptor;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class SearchCommandDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> commandKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> commandAllFields;

    SearchCommandDescriptor() {
        final SCommandBuilderFactory fact = BuilderFactory.get(SCommandBuilderFactory.class);
        commandKeys = new HashMap<String, FieldDescriptor>(5);
        commandKeys.put(CommandSearchDescriptor.ID, new FieldDescriptor(SCommand.class, fact.getIdKey()));
        commandKeys.put(CommandSearchDescriptor.NAME, new FieldDescriptor(SCommand.class, fact.getNameKey()));
        commandKeys.put(CommandSearchDescriptor.DESCRIPTION, new FieldDescriptor(SCommand.class, fact.getDescriptionKey()));
        commandKeys.put(CommandSearchDescriptor.IMPLEMENTATION, new FieldDescriptor(SCommand.class, fact.getImplementationClassKey()));
        commandKeys.put(CommandSearchDescriptor.SYSTEM, new FieldDescriptor(SCommand.class, fact.getSystemKey()));

        commandAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> commandFields = new HashSet<String>(5);
        commandFields.add(fact.getNameKey());
        commandFields.add(fact.getDescriptionKey());
        commandFields.add(fact.getImplementationClassKey());
        commandAllFields.put(SCommand.class, commandFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return commandKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return commandAllFields;
    }

}
