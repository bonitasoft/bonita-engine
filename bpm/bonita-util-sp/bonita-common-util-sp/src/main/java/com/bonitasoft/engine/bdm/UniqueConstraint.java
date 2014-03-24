/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

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
public class UniqueConstraint {

    @XmlAttribute(required = true)
    private String name;

    @XmlElementWrapper(name = "fieldNames", required = true)
    @XmlElement(name = "fieldName", required = true)
    private List<String> fieldNames;

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

}
