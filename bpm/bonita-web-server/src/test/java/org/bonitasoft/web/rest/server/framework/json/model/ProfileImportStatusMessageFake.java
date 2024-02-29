/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.framework.json.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabio Lombardi
 */
public class ProfileImportStatusMessageFake {

    private List<String> errors;

    private String profileName;

    private String status;

    public ProfileImportStatusMessageFake(String profileName, String status) {
        errors = new ArrayList<>();
        this.profileName = profileName;
        this.status = status;
    }

    public void addError(String errorMessage) {
        errors.add(errorMessage);
    }

    public void addErrors(List<String> errorMessages) {
        errors.addAll(errorMessages);
    }

    public void setProfileName(String name) {
        this.profileName = name;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getProfileName() {
        return profileName;
    }
}
