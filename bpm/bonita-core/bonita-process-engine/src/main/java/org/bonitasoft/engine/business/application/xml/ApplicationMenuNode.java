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
package org.bonitasoft.engine.business.application.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author Elias Ricken de Medeiros
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationMenuNode {

    @XmlElement(required = true)
    private String displayName;

    @XmlAttribute
    private String applicationPage;

    @XmlElementWrapper(name = "applicationMenus")
    @XmlElement(name = "applicationMenu")
    private List<ApplicationMenuNode> applicationMenus;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getApplicationPage() {
        return applicationPage;
    }

    public void setApplicationPage(String applicationPage) {
        this.applicationPage = applicationPage;
    }

    public List<ApplicationMenuNode> getApplicationMenus() {
        return applicationMenus == null ? Collections.<ApplicationMenuNode> emptyList() : Collections.unmodifiableList(applicationMenus);
    }

    public void addApplicationMenu(ApplicationMenuNode applicationMenu) {
        if (applicationMenus == null) {
            applicationMenus = new ArrayList<ApplicationMenuNode>();
        }
        applicationMenus.add(applicationMenu);
    }
}
