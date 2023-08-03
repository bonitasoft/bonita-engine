/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.identity.impl;

import java.util.Objects;

import org.bonitasoft.engine.identity.ContactData;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class ContactDataImpl implements ContactData {

    private static final long serialVersionUID = 7363463979895325102L;

    private long userId;

    private boolean personal;

    private String email;

    private String phoneNumber;

    private String mobileNumber;

    private String faxNumber;

    private String building;

    private String room;

    private String address;

    private String zipCode;

    private String city;

    private String state;

    private String country;

    private String website;

    protected ContactDataImpl() {
        super();
    }

    public ContactDataImpl(final long userId) {
        this.userId = userId;
    }

    public ContactDataImpl(final ContactData contactData) {
        super();
        userId = contactData.getUserId();
        personal = contactData.isPersonal();
        email = contactData.getEmail();
        phoneNumber = contactData.getPhoneNumber();
        mobileNumber = contactData.getMobileNumber();
        faxNumber = contactData.getFaxNumber();
        building = contactData.getBuilding();
        room = contactData.getRoom();
        address = contactData.getAddress();
        zipCode = contactData.getZipCode();
        city = contactData.getCity();
        state = contactData.getState();
        country = contactData.getCountry();
        website = contactData.getWebsite();
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getMobileNumber() {
        return mobileNumber;
    }

    @Override
    public String getFaxNumber() {
        return faxNumber;
    }

    @Override
    public String getBuilding() {
        return building;
    }

    @Override
    public String getRoom() {
        return room;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public String getZipCode() {
        return zipCode;
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public String getWebsite() {
        return website;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, personal, email, phoneNumber, mobileNumber, faxNumber, building, room, address,
                zipCode, city, state, country, website);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ContactDataImpl that = (ContactDataImpl) o;
        return userId == that.userId && personal == that.personal && Objects.equals(email, that.email)
                && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(mobileNumber, that.mobileNumber)
                && Objects.equals(faxNumber, that.faxNumber) && Objects.equals(building, that.building)
                && Objects.equals(room, that.room) && Objects.equals(address, that.address)
                && Objects.equals(zipCode, that.zipCode) && Objects.equals(city, that.city)
                && Objects.equals(state, that.state) && Objects.equals(country, that.country)
                && Objects.equals(website, that.website);
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setMobileNumber(final String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setFaxNumber(final String faxNumber) {
        this.faxNumber = faxNumber;
    }

    public void setBuilding(final String building) {
        this.building = building;
    }

    public void setRoom(final String room) {
        this.room = room;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public void setZipCode(final String zipCode) {
        this.zipCode = zipCode;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public boolean isPersonal() {
        return personal;
    }

    public void setPersonal(final boolean personal) {
        this.personal = personal;
    }

}
