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
package org.bonitasoft.engine.identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class ExportedUserImpl implements ExportedUser {

    private String firstName;

    private String lastName;

    private boolean passwordEncrypted;

    private String password;

    private String userName;

    private String iconName;

    private String iconPath;

    private long managerUserId;

    private String title;

    private String jobTitle;

    private long createdBy;

    private String managerUserName;

    private boolean enabled;

    // personal info
    private String personalEmail;

    private String personalPhoneNumber;

    private String personalMobileNumber;

    private String personalFaxNumber;

    private String personalBuilding;

    private String personalRoom;

    private String personalAddress;

    private String personalZipCode;

    private String personalCity;

    private String personalState;

    private String personalCountry;

    private String personalWebsite;

    // professional info
    private String professionalEmail;

    private String professionalPhoneNumber;

    private String professionalMobileNumber;

    private String professionalFaxNumber;

    private String professionalBuilding;

    private String professionalRoom;

    private String professionalAddress;

    private String professionalZipCode;

    private String professionalCity;

    private String professionalState;

    private String professionalCountry;

    private String professionalWebsite;

    private long id;

    private List<ExportedCustomUserInfoValue> customUserInfoValues = new ArrayList<ExportedCustomUserInfoValue>();

    public ExportedUserImpl() {
    }

    public ExportedUserImpl(final ExportedUser user) {
        firstName = user.getFirstName();
        lastName = user.getLastName();
        passwordEncrypted = user.isPasswordEncrypted();
        password = user.getPassword();
        userName = user.getUserName();
        jobTitle = user.getJobTitle();
        managerUserId = user.getManagerUserId();
        iconName = user.getIconName();
        iconPath = user.getIconPath();
        createdBy = user.getCreatedBy();
        title = user.getTitle();
        enabled = user.isEnabled();
        personalEmail = user.getPersonalEmail();
        personalPhoneNumber = user.getPersonalPhoneNumber();
        personalMobileNumber = user.getPersonalMobileNumber();
        personalFaxNumber = user.getPersonalFaxNumber();
        personalBuilding = user.getPersonalBuilding();
        personalRoom = user.getPersonalRoom();
        personalAddress = user.getPersonalAddress();
        personalZipCode = user.getPersonalZipCode();
        personalCity = user.getPersonalCity();
        personalState = user.getPersonalState();
        personalCountry = user.getPersonalCountry();
        personalWebsite = user.getPersonalWebsite();
        professionalEmail = user.getProfessionalEmail();
        professionalPhoneNumber = user.getProfessionalEmail();
        professionalMobileNumber = user.getProfessionalMobileNumber();
        professionalFaxNumber = user.getProfessionalFaxNumber();
        professionalBuilding = user.getProfessionalBuilding();
        professionalRoom = user.getProfessionalRoom();
        professionalAddress = user.getProfessionalAddress();
        professionalZipCode = user.getProfessionalZipCode();
        professionalCity = user.getProfessionalCity();
        professionalState = user.getProfessionalState();
        professionalCountry = user.getProfessionalCountry();
        professionalWebsite = user.getProfessionalWebsite();
        customUserInfoValues = user.getCustomUserInfoValues();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean isPasswordEncrypted() {
        return passwordEncrypted;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getJobTitle() {
        return jobTitle;
    }

    @Override
    public String getManagerUserName() {
        return managerUserName;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getPersonalEmail() {
        return personalEmail;
    }

    @Override
    public String getPersonalPhoneNumber() {
        return personalPhoneNumber;
    }

    @Override
    public String getPersonalMobileNumber() {
        return personalMobileNumber;
    }

    @Override
    public String getPersonalFaxNumber() {
        return personalFaxNumber;
    }

    @Override
    public String getPersonalBuilding() {
        return personalBuilding;
    }

    @Override
    public String getPersonalRoom() {
        return personalRoom;
    }

    @Override
    public String getPersonalAddress() {
        return personalAddress;
    }

    @Override
    public String getPersonalZipCode() {
        return personalZipCode;
    }

    @Override
    public String getPersonalCity() {
        return personalCity;
    }

    @Override
    public String getPersonalState() {
        return personalState;
    }

    @Override
    public String getPersonalCountry() {
        return personalCountry;
    }

    @Override
    public String getPersonalWebsite() {
        return personalWebsite;
    }

    @Override
    public String getProfessionalEmail() {
        return professionalEmail;
    }

    @Override
    public String getProfessionalPhoneNumber() {
        return professionalPhoneNumber;
    }

    @Override
    public String getProfessionalMobileNumber() {
        return professionalMobileNumber;
    }

    @Override
    public String getProfessionalFaxNumber() {
        return professionalFaxNumber;
    }

    @Override
    public String getProfessionalBuilding() {
        return professionalBuilding;
    }

    @Override
    public String getProfessionalRoom() {
        return professionalRoom;
    }

    @Override
    public String getProfessionalAddress() {
        return professionalAddress;
    }

    @Override
    public String getProfessionalZipCode() {
        return professionalZipCode;
    }

    @Override
    public String getProfessionalCity() {
        return professionalCity;
    }

    @Override
    public String getProfessionalState() {
        return professionalState;
    }

    @Override
    public String getProfessionalCountry() {
        return professionalCountry;
    }

    @Override
    public String getProfessionalWebsite() {
        return professionalWebsite;
    }

    @Override
    public long getManagerUserId() {
        return managerUserId;
    }

    @Override
    public String getIconName() {
        return iconName;
    }

    @Override
    public String getIconPath() {
        return iconPath;
    }

    @Override
    public long getCreatedBy() {
        return createdBy;
    }

    @Override
    public List<ExportedCustomUserInfoValue> getCustomUserInfoValues() {
        return Collections.unmodifiableList(customUserInfoValues);
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public void setPasswordEncrypted(final boolean passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setUserName(final String username) {
        userName = username;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setJobTitle(final String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setManagerUserId(final long managerUserId) {
        this.managerUserId = managerUserId;
    }

    public void setPersonalEmail(final String personalEmail) {
        this.personalEmail = personalEmail;
    }

    public void setPersonalPhoneNumber(final String personalPhoneNumber) {
        this.personalPhoneNumber = personalPhoneNumber;
    }

    public void setPersonalMobileNumber(final String personalMobileNumber) {
        this.personalMobileNumber = personalMobileNumber;
    }

    public void setPersonalFaxNumber(final String personalFaxNumber) {
        this.personalFaxNumber = personalFaxNumber;
    }

    public void setPersonalBuilding(final String personalBuilding) {
        this.personalBuilding = personalBuilding;
    }

    public void setPersonalRoom(final String personalRoom) {
        this.personalRoom = personalRoom;
    }

    public void setPersonalAddress(final String personalAddress) {
        this.personalAddress = personalAddress;
    }

    public void setPersonalZipCode(final String personalZipCode) {
        this.personalZipCode = personalZipCode;
    }

    public void setPersonalCity(final String personalCity) {
        this.personalCity = personalCity;
    }

    public void setPersonalState(final String personalState) {
        this.personalState = personalState;
    }

    public void setPersonalCountry(final String personalCountry) {
        this.personalCountry = personalCountry;
    }

    public void setPersonalWebsite(final String personalWebsite) {
        this.personalWebsite = personalWebsite;
    }

    public void setProfessionalEmail(final String professionalEmail) {
        this.professionalEmail = professionalEmail;
    }

    public void setProfessionalPhoneNumber(final String professionalPhoneNumber) {
        this.professionalPhoneNumber = professionalPhoneNumber;
    }

    public void setProfessionalMobileNumber(final String professionalMobileNumber) {
        this.professionalMobileNumber = professionalMobileNumber;
    }

    public void setProfessionalFaxNumber(final String professionalFaxNumber) {
        this.professionalFaxNumber = professionalFaxNumber;
    }

    public void setProfessionalBuilding(final String professionalBuilding) {
        this.professionalBuilding = professionalBuilding;
    }

    public void setProfessionalRoom(final String professionalRoom) {
        this.professionalRoom = professionalRoom;
    }

    public void setProfessionalAddress(final String professionalAddress) {
        this.professionalAddress = professionalAddress;
    }

    public void setProfessionalZipCode(final String professionalZipCode) {
        this.professionalZipCode = professionalZipCode;
    }

    public void setProfessionalCity(final String professionalCity) {
        this.professionalCity = professionalCity;
    }

    public void setProfessionalState(final String professionalState) {
        this.professionalState = professionalState;
    }

    public void setProfessionalCountry(final String professionalCountry) {
        this.professionalCountry = professionalCountry;
    }

    public void setProfessionalWebsite(final String professionalWebsite) {
        this.professionalWebsite = professionalWebsite;
    }

    public void setIconName(final String iconName) {
        this.iconName = iconName;
    }

    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
    }

    public void setCreatedBy(final long createdBy) {
        this.createdBy = createdBy;
    }

    public void setManagerUserName(final String managerUserName) {
        this.managerUserName = managerUserName;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setPersonalData(final ContactData contactData) {
        setPersonalAddress(contactData.getAddress());
        setPersonalBuilding(contactData.getBuilding());
        setPersonalCity(contactData.getCity());
        setPersonalCountry(contactData.getCountry());
        setPersonalEmail(contactData.getEmail());
        setPersonalFaxNumber(contactData.getFaxNumber());
        setPersonalMobileNumber(contactData.getMobileNumber());
        setPersonalPhoneNumber(contactData.getPhoneNumber());
        setPersonalRoom(contactData.getRoom());
        setPersonalState(contactData.getState());
        setPersonalWebsite(contactData.getWebsite());
        setPersonalZipCode(contactData.getZipCode());
    }

    public void setProfessionalData(final ContactData professionalData) {
        setProfessionalAddress(professionalData.getAddress());
        setProfessionalBuilding(professionalData.getBuilding());
        setProfessionalCity(professionalData.getCity());
        setProfessionalCountry(professionalData.getCountry());
        setProfessionalEmail(professionalData.getEmail());
        setProfessionalFaxNumber(professionalData.getFaxNumber());
        setProfessionalMobileNumber(professionalData.getMobileNumber());
        setProfessionalPhoneNumber(professionalData.getPhoneNumber());
        setProfessionalRoom(professionalData.getRoom());
        setProfessionalState(professionalData.getState());
        setProfessionalWebsite(professionalData.getWebsite());
        setProfessionalZipCode(professionalData.getZipCode());
    }

    public void setCustomUserInfoValues(final List<ExportedCustomUserInfoValue> customUserInfoValues) {
        this.customUserInfoValues = customUserInfoValues;
    }

    public void addCustomUserInfoValues(final ExportedCustomUserInfoValue customUserInfoValue) {
        customUserInfoValues.add(customUserInfoValue);
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + getUserName() + "," + getFirstName() + "," + getLastName() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (createdBy ^ createdBy >>> 32);
        result = prime * result + (customUserInfoValues == null ? 0 : customUserInfoValues.hashCode());
        result = prime * result + (enabled ? 1231 : 1237);
        result = prime * result + (firstName == null ? 0 : firstName.hashCode());
        result = prime * result + (iconName == null ? 0 : iconName.hashCode());
        result = prime * result + (iconPath == null ? 0 : iconPath.hashCode());
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (jobTitle == null ? 0 : jobTitle.hashCode());
        result = prime * result + (lastName == null ? 0 : lastName.hashCode());
        result = prime * result + (int) (managerUserId ^ managerUserId >>> 32);
        result = prime * result + (managerUserName == null ? 0 : managerUserName.hashCode());
        result = prime * result + (password == null ? 0 : password.hashCode());
        result = prime * result + (passwordEncrypted ? 1231 : 1237);
        result = prime * result + (personalAddress == null ? 0 : personalAddress.hashCode());
        result = prime * result + (personalBuilding == null ? 0 : personalBuilding.hashCode());
        result = prime * result + (personalCity == null ? 0 : personalCity.hashCode());
        result = prime * result + (personalCountry == null ? 0 : personalCountry.hashCode());
        result = prime * result + (personalEmail == null ? 0 : personalEmail.hashCode());
        result = prime * result + (personalFaxNumber == null ? 0 : personalFaxNumber.hashCode());
        result = prime * result + (personalMobileNumber == null ? 0 : personalMobileNumber.hashCode());
        result = prime * result + (personalPhoneNumber == null ? 0 : personalPhoneNumber.hashCode());
        result = prime * result + (personalRoom == null ? 0 : personalRoom.hashCode());
        result = prime * result + (personalState == null ? 0 : personalState.hashCode());
        result = prime * result + (personalWebsite == null ? 0 : personalWebsite.hashCode());
        result = prime * result + (personalZipCode == null ? 0 : personalZipCode.hashCode());
        result = prime * result + (professionalAddress == null ? 0 : professionalAddress.hashCode());
        result = prime * result + (professionalBuilding == null ? 0 : professionalBuilding.hashCode());
        result = prime * result + (professionalCity == null ? 0 : professionalCity.hashCode());
        result = prime * result + (professionalCountry == null ? 0 : professionalCountry.hashCode());
        result = prime * result + (professionalEmail == null ? 0 : professionalEmail.hashCode());
        result = prime * result + (professionalFaxNumber == null ? 0 : professionalFaxNumber.hashCode());
        result = prime * result + (professionalMobileNumber == null ? 0 : professionalMobileNumber.hashCode());
        result = prime * result + (professionalPhoneNumber == null ? 0 : professionalPhoneNumber.hashCode());
        result = prime * result + (professionalRoom == null ? 0 : professionalRoom.hashCode());
        result = prime * result + (professionalState == null ? 0 : professionalState.hashCode());
        result = prime * result + (professionalWebsite == null ? 0 : professionalWebsite.hashCode());
        result = prime * result + (professionalZipCode == null ? 0 : professionalZipCode.hashCode());
        result = prime * result + (title == null ? 0 : title.hashCode());
        result = prime * result + (userName == null ? 0 : userName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExportedUserImpl other = (ExportedUserImpl) obj;
        if (createdBy != other.createdBy) {
            return false;
        }
        if (customUserInfoValues == null) {
            if (other.customUserInfoValues != null) {
                return false;
            }
        } else if (!customUserInfoValues.equals(other.customUserInfoValues)) {
            return false;
        }
        if (enabled != other.enabled) {
            return false;
        }
        if (firstName == null) {
            if (other.firstName != null) {
                return false;
            }
        } else if (!firstName.equals(other.firstName)) {
            return false;
        }
        if (iconName == null) {
            if (other.iconName != null) {
                return false;
            }
        } else if (!iconName.equals(other.iconName)) {
            return false;
        }
        if (iconPath == null) {
            if (other.iconPath != null) {
                return false;
            }
        } else if (!iconPath.equals(other.iconPath)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (jobTitle == null) {
            if (other.jobTitle != null) {
                return false;
            }
        } else if (!jobTitle.equals(other.jobTitle)) {
            return false;
        }
        if (lastName == null) {
            if (other.lastName != null) {
                return false;
            }
        } else if (!lastName.equals(other.lastName)) {
            return false;
        }
        if (managerUserId != other.managerUserId) {
            return false;
        }
        if (managerUserName == null) {
            if (other.managerUserName != null) {
                return false;
            }
        } else if (!managerUserName.equals(other.managerUserName)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (passwordEncrypted != other.passwordEncrypted) {
            return false;
        }
        if (personalAddress == null) {
            if (other.personalAddress != null) {
                return false;
            }
        } else if (!personalAddress.equals(other.personalAddress)) {
            return false;
        }
        if (personalBuilding == null) {
            if (other.personalBuilding != null) {
                return false;
            }
        } else if (!personalBuilding.equals(other.personalBuilding)) {
            return false;
        }
        if (personalCity == null) {
            if (other.personalCity != null) {
                return false;
            }
        } else if (!personalCity.equals(other.personalCity)) {
            return false;
        }
        if (personalCountry == null) {
            if (other.personalCountry != null) {
                return false;
            }
        } else if (!personalCountry.equals(other.personalCountry)) {
            return false;
        }
        if (personalEmail == null) {
            if (other.personalEmail != null) {
                return false;
            }
        } else if (!personalEmail.equals(other.personalEmail)) {
            return false;
        }
        if (personalFaxNumber == null) {
            if (other.personalFaxNumber != null) {
                return false;
            }
        } else if (!personalFaxNumber.equals(other.personalFaxNumber)) {
            return false;
        }
        if (personalMobileNumber == null) {
            if (other.personalMobileNumber != null) {
                return false;
            }
        } else if (!personalMobileNumber.equals(other.personalMobileNumber)) {
            return false;
        }
        if (personalPhoneNumber == null) {
            if (other.personalPhoneNumber != null) {
                return false;
            }
        } else if (!personalPhoneNumber.equals(other.personalPhoneNumber)) {
            return false;
        }
        if (personalRoom == null) {
            if (other.personalRoom != null) {
                return false;
            }
        } else if (!personalRoom.equals(other.personalRoom)) {
            return false;
        }
        if (personalState == null) {
            if (other.personalState != null) {
                return false;
            }
        } else if (!personalState.equals(other.personalState)) {
            return false;
        }
        if (personalWebsite == null) {
            if (other.personalWebsite != null) {
                return false;
            }
        } else if (!personalWebsite.equals(other.personalWebsite)) {
            return false;
        }
        if (personalZipCode == null) {
            if (other.personalZipCode != null) {
                return false;
            }
        } else if (!personalZipCode.equals(other.personalZipCode)) {
            return false;
        }
        if (professionalAddress == null) {
            if (other.professionalAddress != null) {
                return false;
            }
        } else if (!professionalAddress.equals(other.professionalAddress)) {
            return false;
        }
        if (professionalBuilding == null) {
            if (other.professionalBuilding != null) {
                return false;
            }
        } else if (!professionalBuilding.equals(other.professionalBuilding)) {
            return false;
        }
        if (professionalCity == null) {
            if (other.professionalCity != null) {
                return false;
            }
        } else if (!professionalCity.equals(other.professionalCity)) {
            return false;
        }
        if (professionalCountry == null) {
            if (other.professionalCountry != null) {
                return false;
            }
        } else if (!professionalCountry.equals(other.professionalCountry)) {
            return false;
        }
        if (professionalEmail == null) {
            if (other.professionalEmail != null) {
                return false;
            }
        } else if (!professionalEmail.equals(other.professionalEmail)) {
            return false;
        }
        if (professionalFaxNumber == null) {
            if (other.professionalFaxNumber != null) {
                return false;
            }
        } else if (!professionalFaxNumber.equals(other.professionalFaxNumber)) {
            return false;
        }
        if (professionalMobileNumber == null) {
            if (other.professionalMobileNumber != null) {
                return false;
            }
        } else if (!professionalMobileNumber.equals(other.professionalMobileNumber)) {
            return false;
        }
        if (professionalPhoneNumber == null) {
            if (other.professionalPhoneNumber != null) {
                return false;
            }
        } else if (!professionalPhoneNumber.equals(other.professionalPhoneNumber)) {
            return false;
        }
        if (professionalRoom == null) {
            if (other.professionalRoom != null) {
                return false;
            }
        } else if (!professionalRoom.equals(other.professionalRoom)) {
            return false;
        }
        if (professionalState == null) {
            if (other.professionalState != null) {
                return false;
            }
        } else if (!professionalState.equals(other.professionalState)) {
            return false;
        }
        if (professionalWebsite == null) {
            if (other.professionalWebsite != null) {
                return false;
            }
        } else if (!professionalWebsite.equals(other.professionalWebsite)) {
            return false;
        }
        if (professionalZipCode == null) {
            if (other.professionalZipCode != null) {
                return false;
            }
        } else if (!professionalZipCode.equals(other.professionalZipCode)) {
            return false;
        }
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }
        if (userName == null) {
            if (other.userName != null) {
                return false;
            }
        } else if (!userName.equals(other.userName)) {
            return false;
        }
        return true;
    }

}
