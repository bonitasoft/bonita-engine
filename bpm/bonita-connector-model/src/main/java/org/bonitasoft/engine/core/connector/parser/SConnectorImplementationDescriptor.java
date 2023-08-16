/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.connector.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Feng Hui
 * @author Yanyan Liu
 */
@XmlRootElement(name = "connectorImplementation")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class SConnectorImplementationDescriptor implements Serializable {

    private static final long serialVersionUID = 1262691234201780432L;
    public static final String IMPLEMENTATION_CLASS_NAME = "implementationClassName";
    public static final String IMPLEMENTATION_ID = "id";
    public static final String IMPLEMENTATION_VERSION = "version";
    public static final String DEFINITION_ID = "definitionId";
    public static final String DEFINITION_VERSION = "definitionVersion";

    @XmlElement(name = "implementationId", required = true)
    private String id;
    @XmlElement(name = "implementationVersion", required = true)
    private String version;
    @XmlElement(required = true)
    private String definitionId;
    @XmlElement
    private String definitionVersion;
    @XmlElement(name = "implementationClassname", required = true)
    private String implementationClassName;
    // not usefull but must  be in the xsd (studio related
    @XmlElement
    private Boolean hasSources;
    @XmlElementWrapper(name = "jarDependencies")
    @XmlElement(type = String.class, name = "jarDependency")
    private List<String> jarDependencies = new ArrayList<>();
    //can be in xsd
    @XmlElement
    private String description;

    public SConnectorImplementationDescriptor() {
        super();
    }

    public SConnectorImplementationDescriptor(final String implementationClassName, final String id,
            final String version, final String definitionId,
            final String definitionVersion, final List<String> jarDependencies) {
        super();
        this.implementationClassName = implementationClassName;
        this.id = id;
        this.version = version;
        this.definitionId = definitionId;
        this.definitionVersion = definitionVersion;
        if (jarDependencies != null) {
            this.jarDependencies = jarDependencies;
        }
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
        return new StringJoiner(", ", "ConnectorImplementation [", "]")
                .add("implementationClassName='" + implementationClassName + "'")
                .add("id='" + id + "'")
                .add("version='" + version + "'")
                .add("definitionId='" + definitionId + "'")
                .add("definitionVersion='" + definitionVersion + "'")
                .add("jarDependencies=" + jarDependencies)
                .toString();
    }
}
