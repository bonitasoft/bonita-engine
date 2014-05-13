/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model.field;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.bonitasoft.engine.bdm.model.BusinessObject;

/**
 * @author Colin PUY
 */
@XmlType
public class RelationField extends Field {

    public enum Type {
        AGGREGATION, COMPOSITION;
    }

    @XmlAttribute(required = true)
    private Type type;

    @XmlIDREF
    @XmlAttribute(required = true)
    private BusinessObject reference;

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public BusinessObject getReference() {
        return reference;
    }

    public void setReference(final BusinessObject reference) {
        this.reference = reference;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(reference).append(type).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof RelationField) {
            final RelationField other = (RelationField) obj;
            return new EqualsBuilder().appendSuper(super.equals(obj)).append(reference, other.reference).append(type, other.type).isEquals();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", getName()).append("type", type).append("reference", reference)
                .toString();
    }

}
