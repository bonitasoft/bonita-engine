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
import org.bonitasoft.engine.business.application.ApplicationPageSearchDescriptor;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SearchApplicationPageDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> keys;

    private final Map<Class<? extends PersistentObject>, Set<String>> allFields;

    SearchApplicationPageDescriptor() {
        final SApplicationPageBuilderFactory keyProvider = BuilderFactory.get(SApplicationPageBuilderFactory.class);
        keys = new HashMap<String, FieldDescriptor>(4);
        keys.put(ApplicationPageSearchDescriptor.ID, new FieldDescriptor(SApplicationPage.class, keyProvider.getIdKey()));
        keys.put(ApplicationPageSearchDescriptor.TOKEN, new FieldDescriptor(SApplicationPage.class, keyProvider.getTokenKey()));
        keys.put(ApplicationPageSearchDescriptor.APPLICATION_ID, new FieldDescriptor(SApplicationPage.class, keyProvider.getApplicationIdKey()));
        keys.put(ApplicationPageSearchDescriptor.PAGE_ID, new FieldDescriptor(SApplicationPage.class, keyProvider.getPageIdKey()));

        allFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);

        final Set<String> pageFields = new HashSet<String>(1);
        pageFields.add(keyProvider.getTokenKey());
        allFields.put(SApplicationPage.class, pageFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return keys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return allFields;
    }

}
