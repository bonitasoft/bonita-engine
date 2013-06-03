/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

/**
 * @author Feng Hui
 * @author Yanyan Liu
 */
public class SConnectorImplementationDescriptor implements Serializable, Comparable<SConnectorImplementationDescriptor> {

    private static final long serialVersionUID = 1262691234201780432L;

    public static final String IMPLEMENTATION_CLASS_NAME = "implementationClassName";

    public static final String ID = "id";

    public static final String VERSION = "version";

    public static final String DEFINITION_ID = "definitionId";

    public static final String DEFINITION_VERSION = "definitionVersion";

    private String implementationClassName;

    private String id;

    private String version;

    private String definitionId;

    private String definitionVersion;

    private JarDependencies jarDependencies;

    public static String comparedFiled;

    public SConnectorImplementationDescriptor() {
        super();
    }

    public SConnectorImplementationDescriptor(final String implementationClassName, final String id, final String version, final String definitionId,
            final String definitionVersion, final JarDependencies jarDependencies) {
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

    public JarDependencies getJarDependencies() {
        return jarDependencies;
    }

    @Override
    public String toString() {
        return "ConnectorImplementation [implementationClassName=" + implementationClassName + ", id=" + id + ", version=" + version + ", definitionId="
                + definitionId + ", definitionVersion=" + definitionVersion + ", jarDependencies=" + jarDependencies + "]";
    }

    /*
     * default compare by id
     */
    @Override
    public int compareTo(final SConnectorImplementationDescriptor connectorImplementation) {
        if (comparedFiled != null) {
            if (comparedFiled.equals(SConnectorImplementationDescriptor.IMPLEMENTATION_CLASS_NAME)) {
                return implementationClassName.compareTo(connectorImplementation.getImplementationClassName());
            } else if (comparedFiled.equals(SConnectorImplementationDescriptor.VERSION)) {
                return version.compareTo(connectorImplementation.getVersion());
            } else if (comparedFiled.equals(SConnectorImplementationDescriptor.DEFINITION_ID)) {
                return definitionId.compareTo(connectorImplementation.getDefinitionId());
            } else if (comparedFiled.equals(SConnectorImplementationDescriptor.DEFINITION_VERSION)) {
                return definitionVersion.compareTo(connectorImplementation.getDefinitionVersion());
            }
        }
        return id.compareTo(connectorImplementation.getId());
    }

}
