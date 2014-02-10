package org.bonitasoft.engine.data.model;

public class LightEmployee {

  private long id;
  private String firstName;
  private String lastName;
  private int age;
  
  public LightEmployee(final long id, final String firstName, final String lastName, final int age) {
    super();
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.age = age;
  }
  
  public long getId() {
    return id;
  }
  public String getFirstName() {
    return firstName;
  }
  public String getLastName() {
    return lastName;
  }
  public int getAge() {
    return age;
  }
}
