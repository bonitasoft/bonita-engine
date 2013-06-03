package org.bonitasoft.engine.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Employee extends LightEmployee implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<Address> addresses;
  
  public Employee(final long id, final String firstName, final String lastName, final int age) {
    super(id, firstName, lastName, age);
  }
  
  public List<Address> getAddresses() {
    return addresses;
  }
  
  public void addAddress(final Address address) {
    if (this.addresses == null) {
      this.addresses = new ArrayList<Address>();
    }
    this.addresses.add(address);
  }
  
  
  
}
