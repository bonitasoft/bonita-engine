/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.test;

import java.io.IOException;
import java.util.function.Consumer;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.xml.sax.SAXException;

public class BDMTestUtil {

    public static byte[] getZip(BusinessObjectModel model) throws IOException, JAXBException, SAXException {
        BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        return converter.zip(model);
    }

    public static BusinessObjectModel buildSimpleBom(final String boQualifiedName) {
        return businessObjectModel(bom -> {
            bom.addBusinessObject(businessObject(boQualifiedName, (businessObject) -> {
                businessObject.addField(stringField("aField"));
            }));
        });
    }

    public static BusinessObjectModel businessObjectModel(Consumer<BusinessObjectModel> apply) {
        BusinessObjectModel businessObjectModel = new BusinessObjectModel();
        apply.accept(businessObjectModel);
        return businessObjectModel;
    }

    public static BusinessObject businessObject(String boQualifiedName, Consumer<BusinessObject> apply) {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName(boQualifiedName);
        apply.accept(bo);
        return bo;
    }

    public static SimpleField stringField(String name) {
        final SimpleField field = new SimpleField();
        field.setName(name);
        field.setType(FieldType.STRING);
        return field;
    }

}
