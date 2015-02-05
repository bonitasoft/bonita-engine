/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.model.field;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Colin PUY
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public abstract class Field {

    public static final String PERSISTENCE_ID = "persistenceId";

    public static final String PERSISTENCE_VERSION = "persistenceVersion";

    @XmlAttribute(required = true)
    private String name;

    @XmlAttribute
    private Boolean nullable = true;

    @XmlAttribute
    private Boolean collection = false;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Boolean isNullable() {
        return nullable;
    }

    public void setNullable(final Boolean nullable) {
        this.nullable = nullable;
    }

    public Boolean isCollection() {
        return collection;
    }

    public void setCollection(final Boolean collection) {
        this.collection = collection;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(collection).append(name).append(nullable).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Field) {
            final Field other = (Field) obj;
            return new EqualsBuilder().append(collection, other.collection).append(name, other.name).append(nullable, other.nullable).isEquals();
        }
        return false;
    }
}
