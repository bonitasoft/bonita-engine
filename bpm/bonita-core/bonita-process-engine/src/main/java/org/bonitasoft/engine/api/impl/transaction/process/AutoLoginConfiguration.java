/**
 * Copyright (C) 2016 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.process;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Laurent Leseigneur
 */
public class AutoLoginConfiguration {

    @JsonProperty(value = "processname")
    private String processName;

    @JsonProperty(value = "processversion")
    private String processVersion;

    @JsonProperty(value = "username")
    private String userName;

    @JsonProperty(value = "password")
    private String password;

    public AutoLoginConfiguration() {
    }

    public AutoLoginConfiguration(String processName, String processVersion, String userName, String password) {
        this.processName = processName;
        this.processVersion = processVersion;
        this.userName = userName;
        this.password = password;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
