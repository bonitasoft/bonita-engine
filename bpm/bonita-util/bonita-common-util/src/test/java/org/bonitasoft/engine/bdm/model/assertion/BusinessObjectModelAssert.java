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
package org.bonitasoft.engine.bdm.model.assertion;

import org.assertj.core.api.AbstractAssert;

import org.bonitasoft.engine.bdm.model.BusinessObjectModel;

/**
 * @author Colin PUY
 */
public class BusinessObjectModelAssert extends AbstractAssert<BusinessObjectModelAssert, BusinessObjectModel> {

    protected BusinessObjectModelAssert(final BusinessObjectModel actual) {
        super(actual, BusinessObjectModelAssert.class);
    }

    public static BusinessObjectModelAssert assertThat(final BusinessObjectModel actual) {
        return new BusinessObjectModelAssert(actual);
    }

    public BusinessObjectModelAssert canBeMarshalled() {
        try {
            BusinessObjectModel bom = Marshaller.marshallUnmarshall(actual);
            isEqualTo(bom);
        } catch (Exception e) {
            failWithMessage("Expected <%s> to be marshallizable : <%s>", actual, e.getCause());
        }
        return this;
    }

    public BusinessObjectModelAssert cannotBeMarshalled() {
        try {
            Marshaller.marshallUnmarshall(actual);
            failWithMessage("Expected <%s> not to be marshallizable", actual);
        } catch (Exception e) {
            // OK
        }
        return this;
    }
}
