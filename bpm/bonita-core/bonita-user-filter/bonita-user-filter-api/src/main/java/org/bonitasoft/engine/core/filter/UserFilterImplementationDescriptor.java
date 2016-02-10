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
package org.bonitasoft.engine.core.filter;

import java.io.Serializable;

/**
 * @author Baptiste Mesta
 */
public class UserFilterImplementationDescriptor implements Serializable {

    private static final long serialVersionUID = -1499062187239644206L;

    private String implementationClassName;

    private String id;

    private String version;

    private String definitionId;

    private String definitionVersion;

    private JarDependencies jarDependencies;

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

}
