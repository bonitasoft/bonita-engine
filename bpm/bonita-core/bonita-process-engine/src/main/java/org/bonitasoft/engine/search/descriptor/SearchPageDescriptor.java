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
package org.bonitasoft.engine.search.descriptor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.page.PageSearchDescriptor;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
public class SearchPageDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> pageKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> pageAllFields;

    SearchPageDescriptor() {
        final SPageBuilderFactory keyProvider = BuilderFactory.get(SPageBuilderFactory.class);
        pageKeys = new HashMap<>();
        pageKeys.put(PageSearchDescriptor.ID, new FieldDescriptor(SPage.class, keyProvider.getIdKey()));
        pageKeys.put(PageSearchDescriptor.NAME, new FieldDescriptor(SPage.class, keyProvider.getNameKey()));
        pageKeys.put(PageSearchDescriptor.PROVIDED, new FieldDescriptor(SPage.class, keyProvider.getProvidedKey()));
        pageKeys.put(PageSearchDescriptor.HIDDEN, new FieldDescriptor(SPage.class, keyProvider.getHiddenKey()));
        pageKeys.put(PageSearchDescriptor.INSTALLATION_DATE,
                new FieldDescriptor(SPage.class, keyProvider.getInstallationDateKey()));
        pageKeys.put(PageSearchDescriptor.LAST_MODIFICATION_DATE,
                new FieldDescriptor(SPage.class, keyProvider.getLastModificationDateKey()));
        pageKeys.put(PageSearchDescriptor.INSTALLED_BY,
                new FieldDescriptor(SPage.class, keyProvider.getInstalledByKey()));
        pageKeys.put(PageSearchDescriptor.DISPLAY_NAME,
                new FieldDescriptor(SPage.class, keyProvider.getDisplayNameKey()));
        pageKeys.put(PageSearchDescriptor.CONTENT_TYPE,
                new FieldDescriptor(SPage.class, keyProvider.getContentTypeKey()));
        pageKeys.put(PageSearchDescriptor.PROCESS_DEFINITION_ID,
                new FieldDescriptor(SPage.class, keyProvider.getProcessDefinitionIdKey()));

        pageAllFields = new HashMap<>(1);

        final Set<String> pageFields = new HashSet<>(2);
        pageFields.add(keyProvider.getNameKey());
        pageFields.add(keyProvider.getDisplayNameKey());
        pageAllFields.put(SPage.class, pageFields);
    }

    @Override
    protected Serializable convertFilterValue(String filterField, Serializable filterValue) {
        if (PageSearchDescriptor.PROCESS_DEFINITION_ID.equals(filterField) && filterValue == null) {
            return 0;
        }
        return super.convertFilterValue(filterField, filterValue);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return pageKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return pageAllFields;
    }

}
