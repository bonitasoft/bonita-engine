/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yanyan Liu
 */
public class JarDependencies implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5414181096694335680L;

    private List<String> dependencies;

    /**
     * Default constructor.
     * 
     * @param dependencies
     */
    public JarDependencies(final List<String> dependencies) {
        super();
        this.dependencies = dependencies;
    }

    public void addDependency(final String dependency) {
        if (dependencies == null) {
            dependencies = new ArrayList<String>();
        }
        dependencies.add(dependency);
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}
