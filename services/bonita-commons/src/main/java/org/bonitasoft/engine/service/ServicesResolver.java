/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.engine.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.text.WordUtils;

/**
 * @author Baptiste Mesta
 */
public class ServicesResolver {

    private ServicesLookup servicesLookup;

    public ServicesResolver(ServicesLookup servicesLookup) {
        this.servicesLookup = servicesLookup;
    }

    public void injectServices(Long tenantId, Object target) throws InvocationTargetException, IllegalAccessException {

        final Method[] methods = target.getClass().getMethods();
        for (final Method method : methods) {
            if (method.getAnnotation(InjectedService.class) != null) {
                String serviceName = WordUtils.uncapitalize(method.getName().substring(3));
                final Object lookup = servicesLookup.lookupOnTenant(tenantId, serviceName);
                method.invoke(target, lookup);
            }
        }
    }

}
