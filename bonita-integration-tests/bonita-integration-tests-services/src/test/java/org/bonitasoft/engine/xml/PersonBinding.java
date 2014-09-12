package org.bonitasoft.engine.xml;

import java.util.Map;

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
        final Person person = new Person(firstName, lastName);
        person.setEmail(email);
        person.setPhone(phone);
        return person;
    }

    @Override
    public String getElementTag() {
        return "person";
    }

}
