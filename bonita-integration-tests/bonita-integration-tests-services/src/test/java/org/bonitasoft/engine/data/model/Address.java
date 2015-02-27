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
package org.bonitasoft.engine.data.model;

import java.io.Serializable;

public class Address implements Serializable {

  private static final long serialVersionUID = 1L;

  private long id;
  private String street;
  private String city;
  private int zipCode;
  
  public Address(final long id, final String street, final String city, final int zipCode) {
    super();
    this.id = id;
    this.street = street;
    this.city = city;
    this.zipCode = zipCode;
  }

  public long getId() {
    return id;
  }
  
  public String getStreet() {
    return street;
  }

  public String getCity() {
    return city;
  }

  public int getZipCode() {
    return zipCode;
  }
  
  
}
