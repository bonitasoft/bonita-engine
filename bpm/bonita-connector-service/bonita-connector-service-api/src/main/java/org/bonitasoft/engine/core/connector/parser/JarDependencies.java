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
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JarDependencies{");
        sb.append("dependencies=").append(dependencies);
        sb.append('}');
        return sb.toString();
    }
}
