/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
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
 * @deprecated from version 7.0.0 on, use {@link org.bonitasoft.engine.bdm.model.field.RelationField} instead.
 */
@XmlType
@Deprecated
public class RelationField extends Field {

    public enum Type {
        AGGREGATION, COMPOSITION;
    }

    public enum FetchType {
        EAGER, LAZY
    }

    @XmlAttribute(required = true)
    private Type type;

    @XmlIDREF
    @XmlAttribute(required = true)
    private BusinessObject reference;

    @XmlAttribute(required = true)
    private FetchType fetchType = FetchType.EAGER;

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

    public FetchType getFetchType() {
        return fetchType;
    }

    public void setFetchType(final FetchType fetchType) {
        this.fetchType = fetchType;
    }

    public boolean isLazy() {
        return fetchType == FetchType.LAZY;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(type);
        if (reference != null) {
            hashCodeBuilder = hashCodeBuilder.append(reference.getQualifiedName()).append(reference.getDescription());
        }
        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof RelationField) {
            final RelationField other = (RelationField) obj;
            if (reference == null ^ other.reference == null) {
                return false;
            }
            EqualsBuilder builder = new EqualsBuilder().appendSuper(super.equals(obj)).append(type, other.type);
            if (reference != null && other.reference != null) {
                builder = builder.append(reference.getQualifiedName(), other.reference.getQualifiedName());
            }
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", getName()).append("type", type).append("reference", reference)
                .toString();
    }

}
