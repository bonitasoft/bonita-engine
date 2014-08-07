/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationCreator implements Serializable {

    private static final long serialVersionUID = -916041825489100271L;

    public enum ApplicationField {
        NAME, DISPLAY_NAME, VERSION, PATH, DESCRIPTION, ICON_PATH;
    }

    private final Map<ApplicationField, Serializable> fields;

    public ApplicationCreator(final String name, final String version, final String path) {
        fields = new HashMap<ApplicationField, Serializable>(2);
        fields.put(ApplicationField.NAME, name);
        fields.put(ApplicationField.VERSION, version);
        fields.put(ApplicationField.PATH, path);
    }

    public String getName() {
        return fields.get(ApplicationField.NAME).toString();
    }

    public ApplicationCreator setDisplayName(final String displayName) {
        fields.put(ApplicationField.DISPLAY_NAME, displayName);
        return this;
    }

    public ApplicationCreator setDescription(final String description) {
        fields.put(ApplicationField.DESCRIPTION, description);
        return this;
    }

    public ApplicationCreator setIconPath(final String iconPath) {
        fields.put(ApplicationField.ICON_PATH, iconPath);
        return this;
    }

    public Map<ApplicationField, Serializable> getFields() {
        return fields;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (fields == null ? 0 : fields.hashCode());
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
        final ApplicationCreator other = (ApplicationCreator) obj;
        if (fields == null) {
            if (other.fields != null) {
                return false;
            }
        } else if (!fields.equals(other.fields)) {
            return false;
        }
        return true;
    }

}
