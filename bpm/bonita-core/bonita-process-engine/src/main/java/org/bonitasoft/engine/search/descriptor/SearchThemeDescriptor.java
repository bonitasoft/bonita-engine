/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
