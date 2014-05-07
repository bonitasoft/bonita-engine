package com.bonitasoft.engine.bdm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
public class Relationship {
    
    private enum Type {
        AGGREGATION, COMPOSITION;
    }
    
    @XmlIDREF
    @XmlElement(required = true)
    private BusinessObject businessObject;

    @XmlElement(required = true)
    private Type type;

    public BusinessObject getBusinessObject() {
        return businessObject;
    }

    public void setBusinessObject(BusinessObject businessObject) {
        this.businessObject = businessObject;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

}
