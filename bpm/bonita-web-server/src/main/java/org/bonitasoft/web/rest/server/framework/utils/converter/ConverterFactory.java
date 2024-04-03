/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.framework.utils.converter;

import java.util.Date;

import org.bonitasoft.web.rest.server.framework.utils.converter.typed.BooleanConverter;
import org.bonitasoft.web.rest.server.framework.utils.converter.typed.DateConverter;
import org.bonitasoft.web.rest.server.framework.utils.converter.typed.DoubleConverter;
import org.bonitasoft.web.rest.server.framework.utils.converter.typed.IntegerConverter;
import org.bonitasoft.web.rest.server.framework.utils.converter.typed.LongConverter;
import org.bonitasoft.web.rest.server.framework.utils.converter.typed.StringConverter;

/**
 * @author Colin PUY
 */
public class ConverterFactory {

    public Converter<?> createConverter(String className) {
        if (isClass(String.class, className)) {
            return new StringConverter();
        } else if (isClass(Date.class, className)) {
            return new DateConverter();
        } else if (isClass(Double.class, className)) {
            return new DoubleConverter();
        } else if (isClass(Long.class, className)) {
            return new LongConverter();
        } else if (isClass(Boolean.class, className)) {
            return new BooleanConverter();
        } else if (isClass(Integer.class, className)) {
            return new IntegerConverter();
        }
        throw new UnsupportedOperationException("Canno't create converter for class name : " + className);
    }

    private boolean isClass(Class<?> classe, String className) {
        return classe.getName().equals(className);
    }
}
