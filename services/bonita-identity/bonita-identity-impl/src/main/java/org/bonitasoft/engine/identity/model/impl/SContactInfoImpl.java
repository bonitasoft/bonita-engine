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
package org.bonitasoft.engine.identity.model.impl;

import org.bonitasoft.engine.identity.model.SContactInfo;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class SContactInfoImpl extends SPersistentObjectImpl implements SContactInfo {

    private static final long serialVersionUID = 6618013690854797483L;

    private Long userId;

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

    private boolean personal;

    public SContactInfoImpl() {
        super();
    }

    public SContactInfoImpl(final SContactInfo contactInfo) {
        this();
        userId = contactInfo.getUserId();
        personal = contactInfo.isPersonal();
        address = contactInfo.getAddress();
        building = contactInfo.getBuilding();
        city = contactInfo.getCity();
        country = contactInfo.getCountry();
        email = contactInfo.getEmail();
        faxNumber = contactInfo.getFaxNumber();
        mobileNumber = contactInfo.getMobileNumber();
        phoneNumber = contactInfo.getPhoneNumber();
        room = contactInfo.getRoom();
        state = contactInfo.getState();
        website = contactInfo.getWebsite();
        zipCode = contactInfo.getZipCode();
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
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

    public void setPersonal(final boolean personal) {
        this.personal = personal;
    }

    @Override
    public String getDiscriminator() {
        return this.getClass().getSimpleName();
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
    public boolean isPersonal() {
        return personal;
    }

}
