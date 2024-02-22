/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.data.instance.model.archive.builder;

import java.util.Date;

import org.bonitasoft.engine.data.instance.model.SBlobDataInstance;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.SLongTextDataInstance;
import org.bonitasoft.engine.data.instance.model.SShortTextDataInstance;
import org.bonitasoft.engine.data.instance.model.SXMLDataInstance;
import org.bonitasoft.engine.data.instance.model.SXMLObjectDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SABlobDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SABooleanDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADateDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADoubleDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SAFloatDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SAIntegerDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SALongDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SALongTextDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SAShortTextDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SAXMLDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SAXMLObjectDataInstance;

public class SADataInstanceBuilder {

    public SADataInstance createNewInstance(final SDataInstance sDataInstance) {
        final String className = sDataInstance.getClassName();
        SADataInstance saDataInstance = null;
        if (sDataInstance instanceof SShortTextDataInstance) {
            saDataInstance = new SAShortTextDataInstance(sDataInstance);
        } else if (sDataInstance instanceof SLongTextDataInstance) {
            saDataInstance = new SALongTextDataInstance(sDataInstance);
        } else if (sDataInstance instanceof SXMLDataInstance) {
            saDataInstance = new SAXMLDataInstance(sDataInstance);
        } else if (sDataInstance instanceof SBlobDataInstance) {
            saDataInstance = new SABlobDataInstance(sDataInstance);
        } else if (sDataInstance instanceof SXMLObjectDataInstance) {
            saDataInstance = new SAXMLObjectDataInstance(sDataInstance);
        } else {
            if (Integer.class.getName().equals(className)) {
                saDataInstance = new SAIntegerDataInstance(sDataInstance);
            } else if (Long.class.getName().equals(className)) {
                saDataInstance = new SALongDataInstance(sDataInstance);
            } else if (Boolean.class.getName().equals(className)) {
                saDataInstance = new SABooleanDataInstance(sDataInstance);
            } else if (Date.class.getName().equals(className)) {
                saDataInstance = new SADateDataInstance(sDataInstance);
            } else if (Double.class.getName().equals(className)) {
                saDataInstance = new SADoubleDataInstance(sDataInstance);
            } else if (Float.class.getName().equals(className)) {
                saDataInstance = new SAFloatDataInstance(sDataInstance);
            }
        }
        return saDataInstance;
    }

}
