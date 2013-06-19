/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package com.bonitasoft.engine.reporting;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Emmanuel Duchastenier
 */
public class ReportCreator implements Serializable {

    public enum ReportField {
        NAME, DESCRIPTION, SCREENSHOT// , PROVIDED;
    }

    private final Map<ReportField, Serializable> fields;

    public ReportCreator(final String name) {
        fields = new HashMap<ReportField, Serializable>(3);
        fields.put(ReportField.NAME, name);
    }

    // public ReportCreator setProvided(final boolean provided) {
    // fields.put(ReportField.PROVIDED, provided);
    // return this;
    // }

    public ReportCreator setDescription(final String description) {
        fields.put(ReportField.DESCRIPTION, description);
        return this;
    }

    public ReportCreator setScreenshot(final byte[] screenshot) {
        fields.put(ReportField.SCREENSHOT, screenshot);
        return this;
    }

    public Map<ReportField, Serializable> getFields() {
        return fields;
    }

}
