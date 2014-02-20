/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import javax.lang.model.SourceVersion;

/**
 * @author Matthieu Chaffotte
 */
public class Field {

    private static final String PERSISTENCE_ID = "persistenceId";

    private static final String PERSISTENCE_VERSION = "persistenceVersion";

    private String name;

    private FieldType type;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        if (!SourceVersion.isIdentifier(name) || SourceVersion.isKeyword(name) || isForbiddenIdentifier(name)) {
            throw new IllegalArgumentException(name + " is not a valid field identifier");
        }
        this.name = name;
    }

    private boolean isForbiddenIdentifier(final String name) {
        return PERSISTENCE_ID.equalsIgnoreCase(name) || PERSISTENCE_VERSION.equalsIgnoreCase(name);
    }

    public FieldType getType() {
        return type;
    }

    public void setType(final FieldType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
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
        final Field other = (Field) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

}
