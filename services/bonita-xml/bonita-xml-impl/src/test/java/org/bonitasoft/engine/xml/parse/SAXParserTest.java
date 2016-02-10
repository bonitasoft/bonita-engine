package org.bonitasoft.engine.xml.parse;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.xml.AddressBook;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.Person;
import org.bonitasoft.engine.xml.SAXValidator;
import org.bonitasoft.engine.xml.SValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SAXParserTest {

    private Parser parser = new SAXParser(new SAXValidator(), mock(TechnicalLoggerService.class));

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