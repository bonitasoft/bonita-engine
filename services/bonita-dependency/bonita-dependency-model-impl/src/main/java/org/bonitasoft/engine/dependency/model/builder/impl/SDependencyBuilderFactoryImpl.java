/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.dependency.model.builder.impl;

import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilder;
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.impl.SDependencyImpl;

/**
 * @author Charles Souillard
 */
public class SDependencyBuilderFactoryImpl implements SDependencyBuilderFactory {
    @Override
    public SDependencyBuilder createNewInstance(final String name, final String version, final String fileName, final byte[] value) {
        final SDependencyImpl object = new SDependencyImpl(name, version, fileName, value);
        return new SDependencyBuilderImpl(object);
    }

    @Override
    public String getDescriptionKey() {
        return "description";
    }

    @Override
    public String getFileNameKey() {
        return "fileName";
    }

    @Override
    public String getIdKey() {
        return "id";
    }

    @Override
    public String getNameKey() {
        return "name";
    }

    @Override
    public String getValueKey() {
        return "value_";
    }

    @Override
    public String getVersionKey() {
        return "version";
    }

}
