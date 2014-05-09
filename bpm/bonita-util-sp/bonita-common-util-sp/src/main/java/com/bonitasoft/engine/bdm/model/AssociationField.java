package com.bonitasoft.engine.bdm.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "associationField")
public class AssociationField extends Field {

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

    public void setType(Type type) {
        this.type = type;
    }

    public BusinessObject getReference() {
        return reference;
    }

    public void setReference(BusinessObject reference) {
        this.reference = reference;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((reference == null) ? 0 : reference.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AssociationField other = (AssociationField) obj;
        if (reference == null) {
            if (other.reference != null)
                return false;
        } else if (!reference.equals(other.reference))
            return false;
        if (type != other.type)
            return false;
        return true;
    }
    
    
}
