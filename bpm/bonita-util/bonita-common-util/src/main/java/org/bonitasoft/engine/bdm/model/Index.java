/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author Matthieu Chaffotte
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Index implements NamedElement {

    @XmlAttribute(required = true)
    private String name;

    @XmlElementWrapper(name = "fieldNames", required = true)
    @XmlElement(name = "fieldName", required = true)
    private List<String> fieldNames;

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(final List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (fieldNames == null ? 0 : fieldNames.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Index other = (Index) obj;
        if (fieldNames == null) {
            if (other.fieldNames != null) {
                return false;
            }
        } else if (!fieldNames.equals(other.fieldNames)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Index [name=" + name + ", fieldNames=" + fieldNames + "]";
    }

}
