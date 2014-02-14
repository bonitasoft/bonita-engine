package org.bonitasoft.engine.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Matthieu Chaffotte
 */
public class AddressBook {

    private final String name;

    private final String version;

    private Date lastUpdate;

    private final List<Person> persons;

    public AddressBook(final String name, final String version) {
        this.name = name;
        this.version = version;
        this.persons = new ArrayList<Person>();
    }

    public void addPerson(final Person person) {
        this.persons.add(person);
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public List<Person> getPersons() {
        return this.persons;
    }

    public Date getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

}
