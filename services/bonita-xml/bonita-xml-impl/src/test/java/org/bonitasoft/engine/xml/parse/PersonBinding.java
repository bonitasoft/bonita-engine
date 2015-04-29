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

import java.util.Map;

import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.Person;

/**
 * @author Matthieu Chaffotte
 */
public class PersonBinding extends ElementBinding {

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if ("firstname".equals(name)) {
            firstName = value;
        } else if ("lastname".equals(name)) {
            lastName = value;
        } else if ("email".equals(name)) {
            email = value;
        } else if ("phone".equals(name)) {
            phone = value;
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
    }

    @Override
    public Object getObject() {
        final org.bonitasoft.engine.xml.Person person = new Person(firstName, lastName);
        person.setEmail(email);
        person.setPhone(phone);
        return person;
    }

    @Override
    public String getElementTag() {
        return "person";
    }

}
