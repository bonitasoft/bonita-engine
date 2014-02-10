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
