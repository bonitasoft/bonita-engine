/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Matthieu Chaffotte
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "description", "fields", "uniqueConstraints", "queries" })
public class BusinessObject {

    @XmlAttribute(required = true)
    private String qualifiedName;

    @XmlElement
    private String description;

    @XmlElementWrapper(name = "fields", required = true)
    @XmlElement(name = "field", required = true)
    private List<Field> fields;

    @XmlElementWrapper(name = "uniqueConstraints")
    @XmlElement(name = "uniqueConstraint")
    private List<UniqueConstraint> uniqueConstraints;

    @XmlElementWrapper(name = "queries")
    @XmlElement(name = "query")
    private List<Query> queries;

    public BusinessObject() {
        fields = new ArrayList<Field>();
        uniqueConstraints = new ArrayList<UniqueConstraint>();
        queries = new ArrayList<Query>();
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(final String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(final List<Field> fields) {
        this.fields = fields;
    }

    public void addField(final Field field) {
        fields.add(field);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public UniqueConstraint addUniqueConstraint(final String name, final String... fieldNames) {
        if (fieldNames == null || fieldNames.length == 0) {
            throw new IllegalArgumentException("fieldNames cannot be null or empty");
        }
        final UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setName(name);

        uniqueConstraint.setFieldNames(Arrays.asList(fieldNames));
        if (uniqueConstraints.add(uniqueConstraint)) {
            return uniqueConstraint;
        }
        return null;
    }

    public List<UniqueConstraint> getUniqueConstraints() {
        return uniqueConstraints;
    }

    public void setUniqueConstraints(final List<UniqueConstraint> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
    }

    public Query addQuery(final String name, final String queryContent, final String returnType) {
        final Query query = new Query(name, queryContent, returnType);
        queries.add(query);
        return query;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(final List<Query> queries) {
        this.queries = queries;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (fields == null ? 0 : fields.hashCode());
        result = prime * result + (qualifiedName == null ? 0 : qualifiedName.hashCode());
        result = prime * result + (queries == null ? 0 : queries.hashCode());
        result = prime * result + (uniqueConstraints == null ? 0 : uniqueConstraints.hashCode());
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
        final BusinessObject other = (BusinessObject) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (fields == null) {
            if (other.fields != null) {
                return false;
            }
        } else if (!fields.equals(other.fields)) {
            return false;
        }
        if (qualifiedName == null) {
            if (other.qualifiedName != null) {
                return false;
            }
        } else if (!qualifiedName.equals(other.qualifiedName)) {
            return false;
        }
        if (queries == null) {
            if (other.queries != null) {
                return false;
            }
        } else if (!queries.equals(other.queries)) {
            return false;
        }
        if (uniqueConstraints == null) {
            if (other.uniqueConstraints != null) {
                return false;
            }
        } else if (!uniqueConstraints.equals(other.uniqueConstraints)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BusinessObject [qualifiedName=" + qualifiedName + ", description=" + description + ", fields=" + fields + ", uniqueConstraints="
                + uniqueConstraints + ", queries=" + queries + "]";
    }

}
