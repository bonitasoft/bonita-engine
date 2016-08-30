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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.bonitasoft.engine.identity.ContactData;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class ExportedUser {

    @XmlElement(name = "password")
    protected ExportedUserPassword passwordObject = new ExportedUserPassword();
    @XmlElement
    protected String firstName;
    @XmlElement
    protected String lastName;
    @XmlAttribute
    protected String userName;
    @XmlElement
    protected String iconName;
    @XmlElement
    protected String iconPath;
    @XmlElement
    protected String title;
    @XmlElement
    protected String jobTitle;
    @XmlElement(name = "manager")
    private String managerUserName;
    @XmlElement(defaultValue = "true")
    protected Boolean enabled = true;
    @XmlElement(name = "personalData")
    private ExportedContactInfo personalContactInfo = new ExportedContactInfo();
    @XmlElement(name = "professionalData")
    private ExportedContactInfo professionalContactInfo = new ExportedContactInfo();
    @XmlElementWrapper(name = "customUserInfoValues")
    @XmlElement(name = "customUserInfoValue")
    protected List<ExportedCustomUserInfoValue> customUserInfoValues = new ArrayList<>();

    //Deprecated but here to keep compatibility
    @XmlElementWrapper(name = "metaDatas")
    @XmlElement(name = "metaData")
    protected List<MetaData> metaDatas = new ArrayList<>();


    public ExportedUser() {
    }

    public ExportedUser(final ExportedUser user) {
        firstName = user.getFirstName();
        lastName = user.getLastName();
        passwordObject = user.getPasswordObject();
        userName = user.getUserName();
        jobTitle = user.getJobTitle();
        iconName = user.getIconName();
        iconPath = user.getIconPath();
        title = user.getTitle();
        enabled = user.isEnabled();
        personalContactInfo.email = user.getPersonalEmail();
        personalContactInfo.phoneNumber = user.getPersonalPhoneNumber();
        personalContactInfo.mobileNumber = user.getPersonalMobileNumber();
        personalContactInfo.faxNumber = user.getPersonalFaxNumber();
        personalContactInfo.building = user.getPersonalBuilding();
        personalContactInfo.room = user.getPersonalRoom();
        personalContactInfo.address = user.getPersonalAddress();
        personalContactInfo.zipCode = user.getPersonalZipCode();
        personalContactInfo.city = user.getPersonalCity();
        personalContactInfo.state = user.getPersonalState();
        personalContactInfo.country = user.getPersonalCountry();
        personalContactInfo.website = user.getPersonalWebsite();
        professionalContactInfo.email = user.getProfessionalEmail();
        professionalContactInfo.phoneNumber = user.getProfessionalEmail();
        professionalContactInfo.mobileNumber = user.getProfessionalMobileNumber();
        professionalContactInfo.faxNumber = user.getProfessionalFaxNumber();
        professionalContactInfo.building = user.getProfessionalBuilding();
        professionalContactInfo.room = user.getProfessionalRoom();
        professionalContactInfo.address = user.getProfessionalAddress();
        professionalContactInfo.zipCode = user.getProfessionalZipCode();
        professionalContactInfo.city = user.getProfessionalCity();
        professionalContactInfo.state = user.getProfessionalState();
        professionalContactInfo.country = user.getProfessionalCountry();
        professionalContactInfo.website = user.getProfessionalWebsite();
        customUserInfoValues = user.getCustomUserInfoValues();
    }


    public ExportedUserPassword getPasswordObject() {
        return passwordObject;
    }


    public boolean isPasswordEncrypted() {
        return getPasswordObject().isPasswordEncrypted();
    }

    public String getPassword() {
        return getPasswordObject().getPassword();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUserName() {
        return userName;
    }

    public String getIconName() {
        return iconName;
    }

    public String getIconPath() {
        return iconPath;
    }

    public String getTitle() {
        return title;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getManagerUserName() {
        return managerUserName;
    }

    public String getPersonalEmail() {
        return personalContactInfo.getEmail();
    }

    public String getPersonalPhoneNumber() {
        return personalContactInfo.getPhoneNumber();
    }

    public String getPersonalMobileNumber() {
        return personalContactInfo.getMobileNumber();
    }

    public String getPersonalFaxNumber() {
        return personalContactInfo.getFaxNumber();
    }

    public String getPersonalBuilding() {
        return personalContactInfo.getBuilding();
    }

    public String getPersonalRoom() {
        return personalContactInfo.getRoom();
    }

    public String getPersonalAddress() {
        return personalContactInfo.getAddress();
    }

    public String getPersonalZipCode() {
        return personalContactInfo.getZipCode();
    }

    public String getPersonalCity() {
        return personalContactInfo.getCity();
    }

    public String getPersonalState() {
        return personalContactInfo.getState();
    }

    public String getPersonalCountry() {
        return personalContactInfo.getCountry();
    }

    public String getPersonalWebsite() {
        return personalContactInfo.getWebsite();
    }

    public String getProfessionalEmail() {
        return professionalContactInfo.getEmail();
    }

    public String getProfessionalPhoneNumber() {
        return professionalContactInfo.getPhoneNumber();
    }

    public String getProfessionalMobileNumber() {
        return professionalContactInfo.getMobileNumber();
    }

    public String getProfessionalFaxNumber() {
        return professionalContactInfo.getFaxNumber();
    }

    public String getProfessionalBuilding() {
        return professionalContactInfo.getBuilding();
    }

    public String getProfessionalRoom() {
        return professionalContactInfo.getRoom();
    }

    public String getProfessionalAddress() {
        return professionalContactInfo.getAddress();
    }

    public String getProfessionalZipCode() {
        return professionalContactInfo.getZipCode();
    }

    public String getProfessionalCity() {
        return professionalContactInfo.getCity();
    }

    public String getProfessionalState() {
        return professionalContactInfo.getState();
    }

    public String getProfessionalCountry() {
        return professionalContactInfo.getCountry();
    }

    public String getProfessionalWebsite() {
        return professionalContactInfo.getWebsite();
    }

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
        this.passwordObject.setPasswordEncrypted(passwordEncrypted);
    }

    public void setPassword(final String password) {
        this.passwordObject.setPassword(password);
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

    public void setPersonalEmail(final String personalEmail) {
        personalContactInfo.setEmail(personalEmail);
    }

    public void setPersonalPhoneNumber(final String personalPhoneNumber) {
        this.personalContactInfo.setPhoneNumber(personalPhoneNumber);
    }

    public void setPersonalMobileNumber(final String personalMobileNumber) {
        this.personalContactInfo.setMobileNumber(personalMobileNumber);
    }

    public void setPersonalFaxNumber(final String personalFaxNumber) {
        this.personalContactInfo.setFaxNumber(personalFaxNumber);
    }

    public void setPersonalBuilding(final String personalBuilding) {
        this.personalContactInfo.setBuilding(personalBuilding);
    }

    public void setPersonalRoom(final String personalRoom) {
        this.personalContactInfo.setRoom(personalRoom);
    }

    public void setPersonalAddress(final String personalAddress) {
        this.personalContactInfo.setAddress(personalAddress);
    }

    public void setPersonalZipCode(final String personalZipCode) {
        this.personalContactInfo.setZipCode(personalZipCode);
    }

    public void setPersonalCity(final String personalCity) {
        this.personalContactInfo.setCity(personalCity);
    }

    public void setPersonalState(final String personalState) {
        this.personalContactInfo.setState(personalState);
    }

    public void setPersonalCountry(final String personalCountry) {
        this.personalContactInfo.setCountry(personalCountry);
    }

    public void setPersonalWebsite(final String personalWebsite) {
        this.personalContactInfo.setWebsite(personalWebsite);
    }

    public void setProfessionalEmail(final String professionalEmail) {
        this.professionalContactInfo.setEmail(professionalEmail);
    }

    public void setProfessionalPhoneNumber(final String professionalPhoneNumber) {
        this.professionalContactInfo.setPhoneNumber(professionalPhoneNumber);
    }

    public void setProfessionalMobileNumber(final String professionalMobileNumber) {
        this.professionalContactInfo.setMobileNumber(professionalMobileNumber);
    }

    public void setProfessionalFaxNumber(final String professionalFaxNumber) {
        this.professionalContactInfo.setFaxNumber(professionalFaxNumber);
    }

    public void setProfessionalBuilding(final String professionalBuilding) {
        this.professionalContactInfo.setBuilding(professionalBuilding);
    }

    public void setProfessionalRoom(final String professionalRoom) {
        this.professionalContactInfo.setRoom(professionalRoom);
    }

    public void setProfessionalAddress(final String professionalAddress) {
        this.professionalContactInfo.setAddress(professionalAddress);
    }

    public void setProfessionalZipCode(final String professionalZipCode) {
        this.professionalContactInfo.setZipCode(professionalZipCode);
    }

    public void setProfessionalCity(final String professionalCity) {
        this.professionalContactInfo.setCity(professionalCity);
    }

    public void setProfessionalState(final String professionalState) {
        this.professionalContactInfo.setState(professionalState);
    }

    public void setProfessionalCountry(final String professionalCountry) {
        this.professionalContactInfo.setCountry(professionalCountry);
    }

    public void setProfessionalWebsite(final String professionalWebsite) {
        this.professionalContactInfo.setWebsite(professionalWebsite);
    }

    public void setIconName(final String iconName) {
        this.iconName = iconName;
    }

    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
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

}
