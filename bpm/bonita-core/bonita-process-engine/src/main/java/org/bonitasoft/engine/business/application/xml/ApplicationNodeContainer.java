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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Elias Ricken de Medeiros
 */
@XmlRootElement(name = "applications")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationNodeContainer {

    @XmlElement(name = "application")
    private List<ApplicationNode> applications;

    public ApplicationNodeContainer() {
        this.applications = new ArrayList<ApplicationNode>();
    }

    public List<ApplicationNode> getApplications() {
        return applications;
    }

    public void addApplication(ApplicationNode application) {
        applications.add(application);
    }
}
