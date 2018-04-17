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
package org.bonitasoft.engine.identity.impl;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + (address == null ? 0 : address.hashCode());
        result = prime * result + (building == null ? 0 : building.hashCode());
        result = prime * result + (city == null ? 0 : city.hashCode());
        result = prime * result + (country == null ? 0 : country.hashCode());
        result = prime * result + (email == null ? 0 : email.hashCode());
        result = prime * result + (faxNumber == null ? 0 : faxNumber.hashCode());
        result = prime * result + (mobileNumber == null ? 0 : mobileNumber.hashCode());
        result = prime * result + (personal ? 1231 : 1237);
        result = prime * result + (phoneNumber == null ? 0 : phoneNumber.hashCode());
        result = prime * result + (room == null ? 0 : room.hashCode());
        result = prime * result + (state == null ? 0 : state.hashCode());
        result = prime * result + (int) (userId ^ userId >>> 32);
        result = prime * result + (website == null ? 0 : website.hashCode());
        result = prime * result + (zipCode == null ? 0 : zipCode.hashCode());
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
        final ContactDataImpl other = (ContactDataImpl) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        if (building == null) {
            if (other.building != null) {
                return false;
            }
        } else if (!building.equals(other.building)) {
            return false;
        }
        if (city == null) {
            if (other.city != null) {
                return false;
            }
        } else if (!city.equals(other.city)) {
            return false;
        }
        if (country == null) {
            if (other.country != null) {
                return false;
            }
        } else if (!country.equals(other.country)) {
            return false;
        }
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        } else if (!email.equals(other.email)) {
            return false;
        }
        if (faxNumber == null) {
            if (other.faxNumber != null) {
                return false;
            }
        } else if (!faxNumber.equals(other.faxNumber)) {
            return false;
        }
        if (mobileNumber == null) {
            if (other.mobileNumber != null) {
                return false;
            }
        } else if (!mobileNumber.equals(other.mobileNumber)) {
            return false;
        }
        if (personal != other.personal) {
            return false;
        }
        if (phoneNumber == null) {
            if (other.phoneNumber != null) {
                return false;
            }
        } else if (!phoneNumber.equals(other.phoneNumber)) {
            return false;
        }
        if (room == null) {
            if (other.room != null) {
                return false;
            }
        } else if (!room.equals(other.room)) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (website == null) {
            if (other.website != null) {
                return false;
            }
        } else if (!website.equals(other.website)) {
            return false;
        }
        if (zipCode == null) {
            if (other.zipCode != null) {
                return false;
            }
        } else if (!zipCode.equals(other.zipCode)) {
            return false;
        }
        return true;
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
