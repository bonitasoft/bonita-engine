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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Map;

import org.bonitasoft.engine.xml.AddressBook;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.Person;

/**
 * @author Matthieu Chaffotte
 */
public class AddressBookBinding extends ElementBinding {

    private AddressBook addressBook;

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if ("lastUpdate".equals(name)) {
            try {
                addressBook.setLastUpdate(DateFormat.getInstance().parse(value));
            } catch (final ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if ("person".equals(name)) {
            addressBook.addPerson((Person) value);
        }
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        final String name = attributes.get("name");
        final String version = attributes.get("version");
        addressBook = new AddressBook(name, version);
    }

    @Override
    public Object getObject() {
        return addressBook;
    }

    @Override
    public String getElementTag() {
        return "addressbook";
    }

}
