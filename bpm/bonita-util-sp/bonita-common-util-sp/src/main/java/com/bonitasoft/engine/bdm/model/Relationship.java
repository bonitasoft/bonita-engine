package com.bonitasoft.engine.bdm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
public class Relationship {

    public enum Mode {
        AGGREGATION, COMPOSITION;
    }

    @XmlIDREF
    @XmlAttribute
    private BusinessObject businessObject;

    @XmlElement(required = true)
    private Mode mode;

    public BusinessObject getBusinessObject() {
        return businessObject;
    }

    public void setBusinessObject(BusinessObject businessObject) {
        this.businessObject = businessObject;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Relationship) {
            final Relationship other = (Relationship) obj;
            return new EqualsBuilder().append(businessObject, other.businessObject).append(mode, other.mode).isEquals();
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(businessObject).append(mode).toHashCode();
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append(businessObject).append(mode).build();
    }
}
