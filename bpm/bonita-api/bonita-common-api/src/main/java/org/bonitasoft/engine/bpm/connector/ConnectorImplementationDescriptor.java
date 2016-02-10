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
package org.bonitasoft.engine.bpm.connector;

import java.io.Serializable;
import java.util.List;

/**
 * The fields on which a search can be made for the connector implementation.
 * 
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public class ConnectorImplementationDescriptor implements Serializable {

    private static final long serialVersionUID = -3988746732940581935L;

    /**
     * The name of the field corresponding to the class name of the implementation of the connector
     */
    public static final String IMPLEMENTATION_CLASS_NAME = "implementationClassName";

    /**
     * The name of the field corresponding to the identifier of the connector
     */
    public static final String ID = "id";

    /**
     * The name of the field corresponding to the version of the connector
     */
    public static final String VERSIOIN = "version";

    /**
     * The name of the field corresponding to the identifier of the definition of the connector
     */
    public static final String DEFINITION_ID = "definitionId";

    /**
     * The name of the field corresponding to the version of the definition of the connector
     */
    public static final String DEFINITION_VERSION = "definitionVersion";

    private String implementationClassName;

    private String id;

    private String version;

    private String definitionId;

    private String definitionVersion;

    private List<String> jarDependencies;

    /**
     * The default constructor
     */
    public ConnectorImplementationDescriptor() {
        super();
    }

    /**
     * @param implementationClassName
     *            The implementation of the connector
     * @param id
     *            The identifier of the connector
     * @param version
     *            The version of the connector
     * @param definitionId
     *            The identifier of the definition of the connector
     * @param definitionVersion
     *            The version of the definition of the connector
     * @param jarDependencies
     *            The dependencies of the connector (path of the JAR files)
     */
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

    /**
     * @return The implementation of the connector
     */
    public String getImplementationClassName() {
        return implementationClassName;
    }

    /**
     * @return
     *         The identifier of the connector
     */
    public String getId() {
        return id;
    }

    /**
     * @return The version of the connector
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return
     *         The identifier of the definition of the connector
     */
    public String getDefinitionId() {
        return definitionId;
    }

    /**
     * @return
     *         The version of the definition of the connector
     */
    public String getDefinitionVersion() {
        return definitionVersion;
    }

    /**
     * @return The list of the dependencies of the connector (path of the JAR files)
     */
    public List<String> getJarDependencies() {
        return jarDependencies;
    }

    @Override
    public String toString() {
        return "ConnectorImplementation [implementationClassName=" + implementationClassName + ", id=" + id + ", version=" + version + ", definitionId="
                + definitionId + ", definitionVersion=" + definitionVersion + ", jarDependencies=" + jarDependencies + "]";
    }
}
