package com.bonitasoft.engine;

import java.util.List;

import com.bonitasoft.engine.bdm.BusinessObject;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.Field;
import com.bonitasoft.engine.bdm.FieldType;

public class BOMBuilder {

    public static BOMBuilder aBOM() {
        return new BOMBuilder();
    }

    public BusinessObjectModel build() {
        final Field firstName = new Field();
        firstName.setName("firstName");
        firstName.setType(FieldType.STRING);
        final Field lastName = new Field();
        lastName.setName("lastName");
        lastName.setType(FieldType.STRING);
        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName("com.bonitasoft.Employee");
        employee.addField(firstName);
        employee.addField(lastName);
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employee);
        return bom;
    }

    public BusinessObjectModel buildComplex() {
        final Field firstName = new Field();
        firstName.setName("firstName");
        firstName.setType(FieldType.STRING);
        final Field lastName = new Field();
        lastName.setName("lastName");
        lastName.setType(FieldType.STRING);
        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName("com.bonitasoft.Employee");
        employee.addField(firstName);
        employee.addField(lastName);

        employee.addQuery("getEmployee", "SELECT e FROM Employee e",List.class.getName());
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
