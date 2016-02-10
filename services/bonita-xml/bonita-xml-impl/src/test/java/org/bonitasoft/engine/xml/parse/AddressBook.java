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
package org.bonitasoft.engine.xml.parse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.xml.Person;

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
