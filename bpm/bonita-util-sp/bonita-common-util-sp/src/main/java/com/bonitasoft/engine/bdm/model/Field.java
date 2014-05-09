package com.bonitasoft.engine.bdm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public abstract class Field {

    public static final String PERSISTENCE_ID = "persistenceId";

    public static final String PERSISTENCE_VERSION = "persistenceVersion";
    
    @XmlAttribute(required = true)
    private String name;

    @XmlAttribute
    private Boolean nullable;

    @XmlAttribute
    private Boolean collection;

    public String getName() {
        return name;
    }

    public void setName(String name) {
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((collection == null) ? 0 : collection.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((nullable == null) ? 0 : nullable.hashCode());
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
        Field other = (Field) obj;
        if (collection == null) {
            if (other.collection != null)
                return false;
        } else if (!collection.equals(other.collection))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (nullable == null) {
            if (other.nullable != null)
                return false;
        } else if (!nullable.equals(other.nullable))
            return false;
        return true;
    }
    
    
}
