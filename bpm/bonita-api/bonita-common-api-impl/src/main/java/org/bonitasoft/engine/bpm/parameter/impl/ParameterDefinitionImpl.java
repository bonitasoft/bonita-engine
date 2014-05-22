/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.bpm.parameter.impl;

import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;

/**
 * @author Matthieu Chaffotte
 */
public class ParameterDefinitionImpl extends NamedElementImpl implements ParameterDefinition {

    private static final long serialVersionUID = -3997656451808629180L;

    private final String type;

    private String description;

    public ParameterDefinitionImpl(final String parameterName, final String type) {
        super(parameterName);
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParameterDefinitionImpl other = (ParameterDefinitionImpl) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ParameterDefinitionImpl [type=");
        builder.append(type);
        builder.append(", description=");
        builder.append(description);
        builder.append("]");
        return builder.toString();
    }

}
