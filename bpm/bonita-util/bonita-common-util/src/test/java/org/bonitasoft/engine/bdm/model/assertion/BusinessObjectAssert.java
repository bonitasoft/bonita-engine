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

import static org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder.aBOM;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.xml.sax.SAXException;

import org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.RelationField;

/**
 * @author Colin PUY
 */
public class BusinessObjectAssert extends AbstractAssert<BusinessObjectAssert, BusinessObject> {

    protected BusinessObjectAssert(final BusinessObject actual) {
        super(actual, BusinessObjectAssert.class);
    }

    public static BusinessObjectAssert assertThat(final BusinessObject actual) {
        return new BusinessObjectAssert(actual);
    }

    public BusinessObjectAssert canBeMarshalled() {
        try {
            final BusinessObjectModel bom = marshallUnmarshall(actual);
            Assertions.assertThat(bom.getBusinessObjects().get(0)).isNotNull();
            isEqualTo(bom.getBusinessObjects().get(0));
        } catch (final Exception e) {
            failWithMessage("Expected <%s> to be marshallizable : <%s>", actual, e.getCause());
        }
        return this;
    }

    public BusinessObjectAssert cannotBeMarshalled() {
        try {
            marshallUnmarshall(actual);
            failWithMessage("Expected <%s> not to be marshallizable", actual);
        } catch (final Exception e) {
            // OK
        }
        return this;
    }

    private BusinessObjectModel marshallUnmarshall(final BusinessObject bo) throws JAXBException, IOException, SAXException {
        final BusinessObjectModelBuilder bom = aBOM().withBO(bo);
        addReferencedBoToBom(bo, bom);
        return Marshaller.marshallUnmarshall(bom.build());
    }

    private void addReferencedBoToBom(final BusinessObject bo, final BusinessObjectModelBuilder bom) {
        for (final Field field : bo.getFields()) {
            if (field instanceof RelationField && !bo.equals(((RelationField) field).getReference())) {
                bom.withBO(((RelationField) field).getReference());
            }
        }
    }
}
