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

    @SuppressWarnings("unused")
    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if ("firstname".equals(name)) {
            this.firstName = value;
        } else if ("lastname".equals(name)) {
            this.lastName = value;
        } else if ("email".equals(name)) {
            this.email = value;
        } else if ("phone".equals(name)) {
            this.phone = value;
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void setChildObject(final String name, final Object value) {
    }

    @SuppressWarnings("unused")
    @Override
    public void setAttributes(final Map<String, String> attributes) {
    }

    @Override
    public Object getObject() {
        final Person person = new Person(this.firstName, this.lastName);
        person.setEmail(this.email);
        person.setPhone(this.phone);
        return person;
    }

    @Override
    public String getElementTag() {
        return "person";
    }

}
