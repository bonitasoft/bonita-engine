/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.data.instance.model.archive.builder.impl;

import java.util.Date;

import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.builder.SADataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.archive.builder.SADataInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.model.archive.impl.SABlobDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.impl.SABooleanDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.impl.SADataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.impl.SADateDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.impl.SADoubleDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.impl.SAFloatDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.impl.SAIntegerDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.impl.SALongDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.impl.SALongTextDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.impl.SAShortTextDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.impl.SAXMLDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.impl.SAXMLObjectDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SBlobDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SLongTextDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SShortTextDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SXMLDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SXMLObjectDataInstanceImpl;

/**
 * @author Feng Hui
 * @author Celine Souchet
 */
public class SADataInstanceBuilderFactoryImpl implements SADataInstanceBuilderFactory {

    @Override
    public SADataInstanceBuilder createNewInstance(final SDataInstance sDataInstance) {
        final String className = sDataInstance.getClassName();
        SADataInstanceImpl saDataInstanceImpl = null;
        if (sDataInstance instanceof SShortTextDataInstanceImpl) {
            saDataInstanceImpl = new SAShortTextDataInstanceImpl(sDataInstance);
        } else if (sDataInstance instanceof SLongTextDataInstanceImpl) {
            saDataInstanceImpl = new SALongTextDataInstanceImpl(sDataInstance);
        } else if (sDataInstance instanceof SXMLDataInstanceImpl) {
            saDataInstanceImpl = new SAXMLDataInstanceImpl(sDataInstance);
        } else if (sDataInstance instanceof SBlobDataInstanceImpl) {
            saDataInstanceImpl = new SABlobDataInstanceImpl(sDataInstance);
        } else if (sDataInstance instanceof SXMLObjectDataInstanceImpl) {
            saDataInstanceImpl = new SAXMLObjectDataInstanceImpl(sDataInstance);
        } else {
            if (Integer.class.getName().equals(className)) {
                saDataInstanceImpl = new SAIntegerDataInstanceImpl(sDataInstance);
            } else if (Long.class.getName().equals(className)) {
                saDataInstanceImpl = new SALongDataInstanceImpl(sDataInstance);
            } else if (Boolean.class.getName().equals(className)) {
                saDataInstanceImpl = new SABooleanDataInstanceImpl(sDataInstance);
            } else if (Date.class.getName().equals(className)) {
                saDataInstanceImpl = new SADateDataInstanceImpl(sDataInstance);
            } else if (Double.class.getName().equals(className)) {
                saDataInstanceImpl = new SADoubleDataInstanceImpl(sDataInstance);
            } else if (Float.class.getName().equals(className)) {
                saDataInstanceImpl = new SAFloatDataInstanceImpl(sDataInstance);
            }
        }
        return new SADataInstanceBuilderImpl(saDataInstanceImpl);
    }

}
