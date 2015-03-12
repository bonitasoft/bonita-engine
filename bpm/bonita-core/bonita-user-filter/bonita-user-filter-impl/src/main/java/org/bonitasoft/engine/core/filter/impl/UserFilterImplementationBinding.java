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
package org.bonitasoft.engine.core.filter.impl;

import java.util.Map;

import org.bonitasoft.engine.core.filter.JarDependencies;
import org.bonitasoft.engine.core.filter.UserFilterImplementationDescriptor;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Yanyan Liu
 */
public class UserFilterImplementationBinding extends ElementBinding {

    private String implementationId;

    private String implementationVersion;

    private String definitionId;

    private String definitionVersion;

    private String implementationClassname;

    private JarDependencies jarDependencies;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        // useful when id and other fields are as attributes
        implementationId = attributes.get(XMLDescriptor.IMPLEMENTATION_ID);
        implementationVersion = attributes.get(XMLDescriptor.IMPLEMENTATION_VERSION);
        definitionId = attributes.get(XMLDescriptor.DEFINITION_ID);
        definitionVersion = attributes.get(XMLDescriptor.DEFINITION_VERSION);
        implementationClassname = attributes.get(XMLDescriptor.IMPLEMENTATION_CLASSNAME);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (XMLDescriptor.IMPLEMENTATION_ID.equals(name)) {
            implementationId = value;
        }
        if (XMLDescriptor.IMPLEMENTATION_VERSION.equals(name)) {
            implementationVersion = value;
        }
        if (XMLDescriptor.DEFINITION_ID.equals(name)) {
            definitionId = value;
        }
        if (XMLDescriptor.DEFINITION_VERSION.equals(name)) {
            definitionVersion = value;
        }
        if (XMLDescriptor.IMPLEMENTATION_CLASSNAME.equals(name)) {
            implementationClassname = value;
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLDescriptor.JAR_DEPENDENCIES.equals(name)) {
            jarDependencies = (JarDependencies) value;
        }
    }

    @Override
    public Object getObject() {
        return new UserFilterImplementationDescriptor(implementationClassname, implementationId, implementationVersion, definitionId, definitionVersion,
                jarDependencies);
    }

    @Override
    public String getElementTag() {
        return XMLDescriptor.USER_FILTER_IMPLEMENTATION;
    }

}
