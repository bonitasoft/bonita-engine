/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.connector;

import java.io.Serializable;
import java.util.List;

/**
 * @author Yanyan Liu
 */
public class ConnectorImplementationDescriptor implements Serializable {

    private static final long serialVersionUID = -3988746732940581935L;
    
    public static final String IMPLEMENTATION_CLASS_NAME = "implementationClassName";

    public static final String ID = "id";

    public static final String VERSIOIN = "version";

    public static final String DEFINITION_ID = "definitionId";

    public static final String DEFINITION_VERSION = "definitionVersion";

    private String implementationClassName;

    private String id;

    private String version;

    private String definitionId;

    private String definitionVersion;

    private List<String> jarDependencies;

    public ConnectorImplementationDescriptor() {
        super();
    }

    public ConnectorImplementationDescriptor(final String implementationClassName, final String id, final String version, final String definitionId,
            final String definitionVersion, final List<String> jarDependencies) {
        super();
        this.implementationClassName = implementationClassName;
        this.id = id;
        this.version = version;
        this.definitionId = definitionId;
        this.definitionVersion = definitionVersion;
        this.jarDependencies = jarDependencies;
    }

    public String getImplementationClassName() {
        return implementationClassName;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public String getDefinitionVersion() {
        return definitionVersion;
    }

    public List<String> getJarDependencies() {
        return jarDependencies;
    }

    @Override
    public String toString() {
        return "ConnectorImplementation [implementationClassName=" + implementationClassName + ", id=" + id + ", version=" + version + ", definitionId="
                + definitionId + ", definitionVersion=" + definitionVersion + ", jarDependencies=" + jarDependencies + "]";
    }
}
