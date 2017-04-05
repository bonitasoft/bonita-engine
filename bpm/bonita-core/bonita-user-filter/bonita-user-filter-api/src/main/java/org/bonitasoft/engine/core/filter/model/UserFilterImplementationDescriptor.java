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
package org.bonitasoft.engine.core.filter.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Baptiste Mesta
 */
@XmlRootElement(name = "connectorImplementation")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class UserFilterImplementationDescriptor implements Serializable {

    private static final long serialVersionUID = -1499062187239644206L;
    @XmlElement(name = "implementationClassname", required = true)
    private String implementationClassName;
    @XmlElement(name = "implementationId", required = true)
    private String id;
    @XmlElement(name = "implementationVersion", required = true)
    private String version;
    @XmlElement(required = true)
    private String definitionId;
    @XmlElement
    private String definitionVersion;
    @XmlElement
    private JarDependencies jarDependencies;
    //can be in xsd
    @XmlElement
    private Boolean hasSources;
    //can be in xsd
    @XmlElement
    private String description;

    public UserFilterImplementationDescriptor(final String implementationClassName, final String id, final String version, final String definitionId,
                                              final String definitionVersion, final JarDependencies jarDependencies) {
        super();
        this.implementationClassName = implementationClassName;
        this.id = id;
        this.version = version;
        this.definitionId = definitionId;
        this.definitionVersion = definitionVersion;
        this.jarDependencies = jarDependencies;
    }

    public UserFilterImplementationDescriptor() {
    }

    public String getImplementationClassName() {
        return implementationClassName;
    }

    public void setImplementationClassName(final String implementationClassName) {
        this.implementationClassName = implementationClassName;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(final String definitionId) {
        this.definitionId = definitionId;
    }

    public String getDefinitionVersion() {
        return definitionVersion;
    }

    public void setDefinitionVersion(final String definitionVersion) {
        this.definitionVersion = definitionVersion;
    }

    public JarDependencies getJarDependencies() {
        return jarDependencies;
    }

    public void setJarDependencies(final JarDependencies jarDependencies) {
        this.jarDependencies = jarDependencies;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserFilterImplementationDescriptor{");
        sb.append("implementationClassName='").append(implementationClassName).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", definitionId='").append(definitionId).append('\'');
        sb.append(", definitionVersion='").append(definitionVersion).append('\'');
        sb.append(", jarDependencies=").append(jarDependencies);
        sb.append('}');
        return sb.toString();
    }
}
