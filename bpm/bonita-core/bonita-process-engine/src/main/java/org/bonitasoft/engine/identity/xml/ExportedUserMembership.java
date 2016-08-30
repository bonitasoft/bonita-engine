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

package org.bonitasoft.engine.identity.xml;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Baptiste Mesta
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ExportedUserMembership {

    @XmlElement
    private String userName;
    @XmlElement
    private String roleName;
    @XmlElement
    private String groupName;
    @XmlElement
    private String groupParentPath;
    @XmlElement
    private String assignedBy;
    @XmlElement
    private Long assignedDate;

    public ExportedUserMembership() {
    }

    public ExportedUserMembership(String userName, String roleName, String groupName, String groupParentPath, String assignedBy, Long assignedDate) {
        this.userName = userName;
        this.roleName = roleName;
        this.groupName = groupName;
        this.groupParentPath = groupParentPath;
        this.assignedBy = assignedBy;
        this.assignedDate = assignedDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupParentPath() {
        return groupParentPath;
    }

    public void setGroupParentPath(String groupParentPath) {
        this.groupParentPath = groupParentPath;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Long getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(Long assignedDate) {
        this.assignedDate = assignedDate;
    }

    @Override
    public String toString() {
        return "ExportedUserMembership{" +
                "userName='" + userName + '\'' +
                ", roleName='" + roleName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", groupParentPath='" + groupParentPath + '\'' +
                ", assignedBy='" + assignedBy + '\'' +
                ", assignedDate=" + assignedDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportedUserMembership that = (ExportedUserMembership) o;
        return Objects.equals(userName, that.userName) &&
                Objects.equals(roleName, that.roleName) &&
                Objects.equals(groupName, that.groupName) &&
                Objects.equals(groupParentPath, that.groupParentPath) &&
                Objects.equals(assignedBy, that.assignedBy) &&
                Objects.equals(assignedDate, that.assignedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, roleName, groupName, groupParentPath, assignedBy, assignedDate);
    }
}
