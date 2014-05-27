/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Matthieu Chaffotte
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BusinessObjectModel {

    @XmlElementWrapper(name = "businessObjects", required = true)
    @XmlElement(name = "businessObject", required = true)
    private List<BusinessObject> businessObjects;

    public BusinessObjectModel() {
        businessObjects = new ArrayList<BusinessObject>();
    }

    public List<BusinessObject> getBusinessObjects() {
        return businessObjects;
    }

    public void setBusinessObjects(List<BusinessObject> businessObjects) {
        this.businessObjects = businessObjects;
    }

    public void addBusinessObject(final BusinessObject businessObject) {
        businessObjects.add(businessObject);
    }

    public Set<String> getBusinessObjectsClassNames() {
        HashSet<String> set = new HashSet<String>();
        for (BusinessObject o : businessObjects) {
            set.add(o.getQualifiedName());
        }
        return set;
    }

    public List<BusinessObject> getReferencedBusinessObjectsByComposition() {
        List<BusinessObject> refs = new ArrayList<BusinessObject>();
        for (BusinessObject bo : businessObjects) {
            refs.addAll(bo.getReferencedBusinessObjectsByComposition());
        }
        return refs;
    }

    public List<UniqueConstraint> getUniqueConstraints() {
        List<UniqueConstraint> constraints = new ArrayList<UniqueConstraint>();
        for (BusinessObject bo : businessObjects) {
            constraints.addAll(bo.getUniqueConstraints());
        }
        return constraints;
    }
    
    public List<Index> getIndexes() {
        List<Index> indexes = new ArrayList<Index>();
        for (BusinessObject bo : businessObjects) {
            indexes.addAll(bo.getIndexes());
        }
        return indexes;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(businessObjects).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof BusinessObjectModel) {
            final BusinessObjectModel other = (BusinessObjectModel) obj;
            return new EqualsBuilder()
                    .append(businessObjects, other.businessObjects)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append("businessObjects", businessObjects).toString();
    }
}
