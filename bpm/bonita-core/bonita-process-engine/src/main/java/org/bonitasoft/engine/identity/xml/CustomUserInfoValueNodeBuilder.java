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
package org.bonitasoft.engine.identity.xml;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.identity.ExportedCustomUserInfoValue;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.XMLNode;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class CustomUserInfoValueNodeBuilder {

    static final List<Class<? extends ElementBinding>> BINDINGS = new ArrayList<Class<? extends ElementBinding>>();

    static {
        BINDINGS.add(CustomUserInfoValueBinding.class);
    }
    
    
    private CustomUserInfoValueNodeBuilder() {
    }
    
    public static XMLNode buildNode(List<ExportedCustomUserInfoValue> userInfoValues) {
        XMLNode node = new XMLNode(OrganizationMappingConstants.CUSTOM_USER_INFO_VALUES);
        for (ExportedCustomUserInfoValue userInfoValue : userInfoValues) {
            node.addChild(buildCustomUserInfoValueNode(userInfoValue));
        }
        return node;
    }

    private static XMLNode buildCustomUserInfoValueNode(ExportedCustomUserInfoValue userInfoValue) {
        XMLNode node = new XMLNode(OrganizationMappingConstants.CUSTOM_USER_INFO_VALUE);
        node.addChild(buildNodeWithContent(OrganizationMappingConstants.NAME, userInfoValue.getName()));
        node.addChild(buildNodeWithContent(OrganizationMappingConstants.VALUE, userInfoValue.getValue()));
        return node;
    }

    private static XMLNode buildNodeWithContent(String nodeName, String nodeValue) {
        XMLNode node = new XMLNode(nodeName);
        node.setContent(nodeValue);
        return node;
    }
    
}
