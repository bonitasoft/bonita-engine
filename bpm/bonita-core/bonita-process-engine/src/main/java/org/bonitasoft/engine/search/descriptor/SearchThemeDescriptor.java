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

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.theme.ThemeSearchDescriptor;
import org.bonitasoft.engine.theme.builder.SThemeBuilderFactory;
import org.bonitasoft.engine.theme.model.STheme;

/**
 * @author Celine Souchet
 */
public class SearchThemeDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> themeAllFields;

    public SearchThemeDescriptor() {
        searchEntityKeys = new HashMap<String, FieldDescriptor>(1);
        searchEntityKeys.put(ThemeSearchDescriptor.ID, new FieldDescriptor(STheme.class, SThemeBuilderFactory.ID));
        searchEntityKeys.put(ThemeSearchDescriptor.IS_DEFAULT, new FieldDescriptor(STheme.class, SThemeBuilderFactory.IS_DEFAULT));
        searchEntityKeys.put(ThemeSearchDescriptor.TYPE, new FieldDescriptor(STheme.class, SThemeBuilderFactory.TYPE));

        themeAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> fields = new HashSet<String>(3);
        fields.add(SThemeBuilderFactory.TYPE);
        themeAllFields.put(STheme.class, fields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return searchEntityKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return themeAllFields;
    }

}
