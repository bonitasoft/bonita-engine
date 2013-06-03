/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

    public ExportedUserImpl() {
    }

    public ExportedUserImpl(final ExportedUser user) {
        firstName = user.getFirstName();
        lastName = user.getLastName();
        passwordEncrypted = user.isPasswordEncrypted();
        password = user.getPassword();
        userName = user.getUserName();
        jobTitle = user.getJobTitle();
        // delegeeUserName = user.getDelegeeUserName();
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

    @Override
    public String toString() {
        return getClass().getName() + "[" + getUserName() + "," + getFirstName() + "," + getLastName() + "]";
    }

}
