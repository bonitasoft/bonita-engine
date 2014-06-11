
package org.bonita.hr.impl;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Version;


/**
 * 
 */
@Entity(name = "Employee")
@Table(name = "EMPLOYEE")
@NamedQueries({
    @NamedQuery(name = "Employee.findByFirstName", query = "SELECT e\nFROM Employee e\nWHERE e.firstName= :firstName\nORDER BY e.persistenceId"),
    @NamedQuery(name = "Employee.find", query = "SELECT e\nFROM Employee e\nORDER BY e.persistenceId")
})
public class Employee
    implements org.bonita.hr.Employee
{

    @Id
    @GeneratedValue
    private Long persistenceId;
    @Version
    private Long persistenceVersion;
    @Column(name = "FIRSTNAME", nullable = true)
    private String firstName;
    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "EMPLOYEE_PID", nullable = false)
    @OrderColumn
    private List<org.bonita.hr.Address> addresses = new ArrayList<org.bonita.hr.Address>(10);
    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "EMPLOYEE_PID", nullable = false)
    @OrderColumn
    private List<org.bonita.hr.Skill> skills = new ArrayList<org.bonita.hr.Skill>(10);

    public Employee() {
    }

    public Employee(org.bonita.hr.Employee employee) {
        this.persistenceId = employee.getPersistenceId();
        this.persistenceVersion = employee.getPersistenceVersion();
        this.firstName = employee.getFirstName();
        this.addresses = new ArrayList<org.bonita.hr.Address>();
        for (org.bonita.hr.Address i: employee.getAddresses()) {
            this.addresses.add(new org.bonita.hr.impl.Address(i));
        }
        this.skills = new ArrayList<org.bonita.hr.Skill>();
        for (org.bonita.hr.Skill i: employee.getSkills()) {
            this.skills.add(new org.bonita.hr.impl.Skill(i));
        }
    }

    public void setPersistenceId(Long persistenceId) {
        this.persistenceId = persistenceId;
    }

    public Long getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceVersion(Long persistenceVersion) {
        this.persistenceVersion = persistenceVersion;
    }

    public Long getPersistenceVersion() {
        return persistenceVersion;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setAddresses(List<org.bonita.hr.Address> addresses) {
        this.addresses = addresses;
    }

    public List<org.bonita.hr.Address> getAddresses() {
        return addresses;
    }

    public void addToAddresses(org.bonita.hr.Address addTo) {
        addresses.add(addTo);
    }

    public void removeFromAddresses(org.bonita.hr.Address removeFrom) {
        addresses.remove(removeFrom);
    }

    public void setSkills(List<org.bonita.hr.Skill> skills) {
        this.skills = skills;
    }

    public List<org.bonita.hr.Skill> getSkills() {
        return skills;
    }

    public void addToSkills(org.bonita.hr.Skill addTo) {
        skills.add(addTo);
    }

    public void removeFromSkills(org.bonita.hr.Skill removeFrom) {
        skills.remove(removeFrom);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass()!= obj.getClass()) {
            return false;
        }
        Employee other = ((Employee) obj);
        if (persistenceId == null) {
            if (other.persistenceId!= null) {
                return false;
            }
        } else {
            if (!persistenceId.equals(other.persistenceId)) {
                return false;
            }
        }
        if (persistenceVersion == null) {
            if (other.persistenceVersion!= null) {
                return false;
            }
        } else {
            if (!persistenceVersion.equals(other.persistenceVersion)) {
                return false;
            }
        }
        if (firstName == null) {
            if (other.firstName!= null) {
                return false;
            }
        } else {
            if (!firstName.equals(other.firstName)) {
                return false;
            }
        }
        if (addresses == null) {
            if (other.addresses!= null) {
                return false;
            }
        } else {
            if (!addresses.equals(other.addresses)) {
                return false;
            }
        }
        if (skills == null) {
            if (other.skills!= null) {
                return false;
            }
        } else {
            if (!skills.equals(other.skills)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        int persistenceIdCode = 0;
        if (persistenceId!= null) {
            persistenceIdCode = persistenceId.hashCode();
        }
        result = ((prime*result)+ persistenceIdCode);
        int persistenceVersionCode = 0;
        if (persistenceVersion!= null) {
            persistenceVersionCode = persistenceVersion.hashCode();
        }
        result = ((prime*result)+ persistenceVersionCode);
        int firstNameCode = 0;
        if (firstName!= null) {
            firstNameCode = firstName.hashCode();
        }
        result = ((prime*result)+ firstNameCode);
        int addressesCode = 0;
        if (addresses!= null) {
            addressesCode = addresses.hashCode();
        }
        result = ((prime*result)+ addressesCode);
        int skillsCode = 0;
        if (skills!= null) {
            skillsCode = skills.hashCode();
        }
        result = ((prime*result)+ skillsCode);
        return result;
    }

}