/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.data;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public class DataSourceImplementationProxy implements InvocationHandler, Serializable {

    private static final long serialVersionUID = 1L;

    private final Object dataSourceImplementation;

    public DataSourceImplementationProxy(final Object dataSourceImplementation) {
        super();
        this.dataSourceImplementation = dataSourceImplementation;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        try {
            return method.invoke(dataSourceImplementation, args);
        } catch (final InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
