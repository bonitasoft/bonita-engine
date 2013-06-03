package org.bonitasoft.engine.xml;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Map;

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
