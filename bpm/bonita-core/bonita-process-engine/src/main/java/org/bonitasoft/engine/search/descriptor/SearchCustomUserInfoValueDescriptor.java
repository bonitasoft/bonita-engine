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
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.identity.CustomUserInfoValueSearchDescriptor;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Vincent Elcrin
 */
public class SearchCustomUserInfoValueDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchableKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> allFields;

    public SearchCustomUserInfoValueDescriptor() {
        final SCustomUserInfoValueBuilderFactory keyprovider = BuilderFactory.get(SCustomUserInfoValueBuilderFactory.class);

        searchableKeys = new HashMap<String, FieldDescriptor>(3);
        searchableKeys.put(CustomUserInfoValueSearchDescriptor.DEFINITION_ID, new FieldDescriptor(SCustomUserInfoValue.class, keyprovider.getDefinitionIdKey()));
        searchableKeys.put(CustomUserInfoValueSearchDescriptor.USER_ID, new FieldDescriptor(SCustomUserInfoValue.class, keyprovider.getUserIdKey()));
        searchableKeys.put(CustomUserInfoValueSearchDescriptor.VALUE, new FieldDescriptor(SCustomUserInfoValue.class, keyprovider.getValueKey()));

        allFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> fields = new HashSet<String>(2);
        fields.add(keyprovider.getDefinitionIdKey());
        fields.add(keyprovider.getUserIdKey());
        fields.add(keyprovider.getValueKey());

        allFields.put(SConnectorInstance.class, fields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return searchableKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return allFields;
    }

}
