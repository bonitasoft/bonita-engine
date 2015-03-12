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
package org.bonitasoft.engine.xml;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.ServicesBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class ParserTest {

    private static ServicesBuilder servicesBuilder;

    private static Parser parser;

    static {
        servicesBuilder = new ServicesBuilder();
        parser = servicesBuilder.getParser();
    }

    @Before
    public void before() {
        final List<Class<? extends ElementBinding>> bindings = new ArrayList<Class<? extends ElementBinding>>();
        bindings.add(AddressBookBinding.class);
        bindings.add(PersonBinding.class);
        parser.setBindings(bindings);
    }

    @Test
    public void getASimpleAddressBook() throws Exception {
        InputStream xsdSchema = null;
        try {
            xsdSchema = this.getClass().getResourceAsStream("/addressBook.xsd");
            parser.setSchema(xsdSchema);
            final URL urlFile = this.getClass().getResource("/MyAddressBook.xml");
            final File xmlFile = new File(urlFile.toURI());
            final AddressBook addressbook = (AddressBook) parser.getObjectFromXML(xmlFile);
            Assert.assertNotNull(addressbook);
            Assert.assertEquals("MyPersonalAddressBook", addressbook.getName());
            Assert.assertEquals("1.0", addressbook.getVersion());
            final List<Person> persons = addressbook.getPersons();
            Assert.assertEquals(1, persons.size());
            final Person person = persons.get(0);
            Assert.assertEquals("John", person.getFirstName());
            Assert.assertEquals("Doe", person.getLastName());
            Assert.assertEquals("john.doe@doeland.com", person.getEmail());
            Assert.assertNull(person.getPhone());
        } finally {
            if (xsdSchema != null) {
                xsdSchema.close();
            }
        }
    }

    @Test
    public void validateASimpleAddressBook() throws Exception {
        InputStream xsdSchema = null;
        try {
            xsdSchema = this.getClass().getResourceAsStream("/addressBook.xsd");
            parser.setSchema(xsdSchema);
            final URL urlFile = this.getClass().getResource("/MyAddressBook.xml");
            final File xmlFile = new File(urlFile.toURI());
            parser.validate(xmlFile);
        } finally {
            if (xsdSchema != null) {
                xsdSchema.close();
            }
        }
    }

    @Test(expected = SValidationException.class)
    public void validateInvalidSimpleAddressBook() throws Exception {
        InputStream xsdSchema = null;
        try {
            xsdSchema = this.getClass().getResourceAsStream("/addressBook.xsd");
            parser.setSchema(xsdSchema);
            final URL urlFile = this.getClass().getResource("/MyAddressBook_invalid.xml");
            final File xmlFile = new File(urlFile.toURI());
            parser.validate(xmlFile);
        } finally {
            if (xsdSchema != null) {
                xsdSchema.close();
            }
        }
    }

    @Test
    public void validateASimpleAddressBookTwice() throws Exception {
        InputStream xsdSchema = null;
        try {
            xsdSchema = this.getClass().getResourceAsStream("/addressBook.xsd");
            parser.setSchema(xsdSchema);
            final URL urlFile = this.getClass().getResource("/MyAddressBook.xml");
            final File xmlFile = new File(urlFile.toURI());
            parser.validate(xmlFile);
            parser.validate(xmlFile);
        } finally {
            if (xsdSchema != null) {
                xsdSchema.close();
            }
        }
    }

    @Test
    public void getASimpleAddressBook1() throws Exception {
        final URL urlFile = this.getClass().getResource("/MyAddressBook.xml");
        final File xmlFile = new File(urlFile.toURI());
        final AddressBook addressbook = (AddressBook) parser.getObjectFromXML(xmlFile.getAbsolutePath());
        Assert.assertNotNull(addressbook);
        Assert.assertEquals("MyPersonalAddressBook", addressbook.getName());
        Assert.assertEquals("1.0", addressbook.getVersion());
        final List<Person> persons = addressbook.getPersons();
        Assert.assertEquals(1, persons.size());
        final Person person = persons.get(0);
        Assert.assertEquals("John", person.getFirstName());
        Assert.assertEquals("Doe", person.getLastName());
        Assert.assertEquals("john.doe@doeland.com", person.getEmail());
        Assert.assertNull(person.getPhone());
    }

}
