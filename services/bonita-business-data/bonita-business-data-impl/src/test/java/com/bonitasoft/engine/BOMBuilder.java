package com.bonitasoft.engine;

import java.util.List;

import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.SimpleField;

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
        employee.setQualifiedName("com.bonitasoft.Employee");
        employee.addField(firstName);
        employee.addField(lastName);
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employee);
        return bom;
    }

    public BusinessObjectModel buildModelWithAllSupportedTypes() {
        final BusinessObject invoice = new BusinessObject();
        invoice.setQualifiedName("com.bonitasoft.pojo.ComplexInvoice");
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(invoice);
        return bom;
    }

    public BusinessObjectModel buildModelWithConstrainedFields() {
        final BusinessObject constrained = new BusinessObject();
        constrained.setQualifiedName("com.bonitasoft.pojo.ConstrainedItem");
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
        employee.setQualifiedName("com.bonitasoft.Employee");
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
        employee.setQualifiedName("com.bonitasoft.Person");
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
