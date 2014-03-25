/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.connector.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class JarDependenciesBinding extends ElementBinding {

    private final List<String> dependencies = new ArrayList<String>();

    @SuppressWarnings("unused")
    @Override
    public void setAttributes(final Map<String, String> attributes) {
    }

    @SuppressWarnings("unused")
    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (XMLDescriptor.JAR_DEPENDENCY.equals(name)) {
            dependencies.add(value);
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void setChildObject(final String name, final Object value) {
    }

    @Override
    public Object getObject() {
        return new JarDependencies(dependencies);
    }

    @Override
    public String getElementTag() {
        return XMLDescriptor.JAR_DEPENDENCIES;
    }

}
