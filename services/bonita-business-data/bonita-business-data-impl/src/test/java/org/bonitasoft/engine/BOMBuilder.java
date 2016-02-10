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
package org.bonitasoft.engine;

import java.util.List;

import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;

public class BOMBuilder {

    public static BOMBuilder aBOM() {
        return new BOMBuilder();
    }

    public BusinessObjectModel build() {
        final SimpleField firstName = new SimpleField();
        firstName.setName("firstName");
        firstName.setType(FieldType.STRING);
        final SimpleField lastName = new SimpleField();
        lastName.setName("lastName");
        lastName.setType(FieldType.STRING);
        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName("com.company.Employee");
        employee.addField(firstName);
        employee.addField(lastName);
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employee);
        return bom;
    }

    public BusinessObjectModel buildModelWithMultipleBoolean() {
        final SimpleField booleans = new SimpleField();
        booleans.setName("booleanList");
        booleans.setType(FieldType.BOOLEAN);
        booleans.setCollection(true);

        final BusinessObject booleansBO = new BusinessObject();
        booleansBO.setQualifiedName("com.company.Demo");
        booleansBO.addField(booleans);

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(booleansBO);
        return bom;
    }

    public BusinessObjectModel buildModelWithAllSupportedTypes() {
        final BusinessObject invoice = new BusinessObject();
        invoice.setQualifiedName("com.company.pojo.ComplexInvoice");
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(invoice);
        return bom;
    }

    public BusinessObjectModel buildModelWithConstrainedFields() {
        final BusinessObject constrained = new BusinessObject();
        constrained.setQualifiedName("com.company.pojo.ConstrainedItem");
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(constrained);
        return bom;
    }

    public BusinessObjectModel buildComplex() {
        final SimpleField firstName = new SimpleField();
        firstName.setName("firstName");
        firstName.setType(FieldType.STRING);
        final SimpleField lastName = new SimpleField();
        lastName.setName("lastName");
        lastName.setType(FieldType.STRING);
        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName("com.company.Employee");
        employee.addField(firstName);
        employee.addField(lastName);

        employee.addQuery("getEmployee", "SELECT e FROM Employee e", List.class.getName());
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employee);
        return bom;
    }

    public BusinessObjectModel buildPerson() {
        final SimpleField nickNames = new SimpleField();
        nickNames.setName("nickNames");
        nickNames.setType(FieldType.STRING);
        nickNames.setLength(Integer.valueOf(15));
        nickNames.setCollection(Boolean.TRUE);

        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName("com.company.Person");
        employee.addField(nickNames);

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employee);
        return bom;
    }

    public byte[] buildZip() {
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        try {
            return converter.zip(build());
        } catch (final Exception e) {
            throw new RuntimeException("Unable to build BOM zip");
        }
    }

}
