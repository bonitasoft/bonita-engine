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
package org.bonitasoft.engine.theme.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;

/**
 * @author Celine Souchet
 */
public class SelectDescriptorBuilder {

    private SelectDescriptorBuilder() {
        // For Sonar
    }

    public static <T extends PersistentObject> SelectByIdDescriptor<T> getElementById(final Class<T> clazz, final String elementName, final long id) {
        return new SelectByIdDescriptor<T>("get" + elementName + "ById", clazz, id);
    }

    public static SelectOneDescriptor<Long> getNumberOfElement(final String elementName, final Class<? extends PersistentObject> clazz) {
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectOneDescriptor<Long>("getNumberOf" + elementName, parameters, clazz, Long.class);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElements(final Class<T> clazz, final String elementName, final int fromIndex,
            final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements);
        return getElements(clazz, elementName, queryOptions);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElements(final Class<T> clazz, final String elementName, final String field,
            final OrderByType order, final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, clazz, field, order);
        return getElements(clazz, elementName, queryOptions);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElements(final Class<T> clazz, final String elementName,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectListDescriptor<T>("get" + elementName + "s", parameters, clazz, queryOptions);
    }

    public static SelectOneDescriptor<STheme> getTheme(SThemeType type, boolean isDefault) {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(1);
        inputParameters.put("type", type);
        inputParameters.put("isDefault", isDefault);
        return new SelectOneDescriptor<STheme>("getTheme", inputParameters, STheme.class);
    }

    public static SelectOneDescriptor<STheme> getLastModifiedTheme(SThemeType type) {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(1);
        inputParameters.put("type", type);
        return new SelectOneDescriptor<STheme>("getLastModifiedTheme", inputParameters, STheme.class);
    }
}
