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

import java.util.Map;

import org.bonitasoft.engine.identity.ExportedCustomUserInfoValue;
import org.bonitasoft.engine.identity.ExportedUserBuilder;
import org.bonitasoft.engine.identity.ExportedUserBuilderFactory;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Yanyan Liu
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class UserBinding extends ElementBinding {

    private ExportedUserBuilder userBuilder;

    private boolean containsEnabled = false;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        final String userName = attributes.get(OrganizationMappingConstants.USER_NAME);
        userBuilder = ExportedUserBuilderFactory.createNewInstance(userName, null);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (OrganizationMappingConstants.PASSWORD.equals(name)) {
            userBuilder.setPassword(value);
            final String encrypted = attributes.get(OrganizationMappingConstants.PASSWORD_ENCRYPTED);
            userBuilder.setPasswordEncrypted(Boolean.parseBoolean(encrypted));
        } else if (OrganizationMappingConstants.FIRST_NAME.equals(name)) {
            userBuilder.setFirstName(value);
        } else if (OrganizationMappingConstants.LAST_NAME.equals(name)) {
            userBuilder.setLastName(value);
        } else if (OrganizationMappingConstants.ICON_NAME.equals(name)) {
            userBuilder.setIconName(value);
        } else if (OrganizationMappingConstants.ICON_PATH.equals(name)) {
            userBuilder.setIconPath(value);
        } else if (OrganizationMappingConstants.TITLE.equals(name)) {
            userBuilder.setTitle(value);
        } else if (OrganizationMappingConstants.JOB_TITLE.equals(name)) {
            userBuilder.setJobTitle(value);
        } else if (OrganizationMappingConstants.MANAGER.equals(name)) {
            userBuilder.setManagerUserName(value);
        } else if (OrganizationMappingConstants.ENABLED.equals(name)) {
            containsEnabled = true;
            userBuilder.setEnabled(Boolean.parseBoolean(value));
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (OrganizationMappingConstants.PERSONAL_DATA.equals(name)) {
            final XMLContactDataMapping personalData = (XMLContactDataMapping) value;
            if (personalData != null) {
                userBuilder.setPersonalData(personalData.getContactData());
            }
        } else if (OrganizationMappingConstants.PROFESSIONAL_DATA.equals(name)) {
            final XMLContactDataMapping professionalData = (XMLContactDataMapping) value;
            if (professionalData != null) {
                userBuilder.setProfessionalData(professionalData.getContactData());
            }
        } else if (OrganizationMappingConstants.CUSTOM_USER_INFO_VALUE.equals(name)) {
            final ExportedCustomUserInfoValue customUserInfoValue = (ExportedCustomUserInfoValue) value;
            if (customUserInfoValue != null) {
                userBuilder.addCustomUserInfoValue(customUserInfoValue);
            }
        }
    }

    @Override
    public Object getObject() {
        if (!containsEnabled) {
            userBuilder.setEnabled(true);
        }
        return userBuilder.done();
    }

    @Override
    public String getElementTag() {
        return OrganizationMappingConstants.USER;
    }

}
