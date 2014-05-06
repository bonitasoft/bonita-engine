package com.bonitasoft.engine.bdm;

import java.util.List;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.Field;
import com.bonitasoft.engine.bdm.model.FieldType;

public class BOMBuilder {

    private Field buildField(final String name, final FieldType type) {
        final Field field = new Field();
        field.setName(name);
        field.setType(type);
        return field;
    }

    private BusinessObject buildMyBusinessObject() {
        final Field stringField = buildField("stringField", FieldType.STRING);
        stringField.setNullable(Boolean.TRUE);
        final Field booleanField = buildField("booleanField", FieldType.BOOLEAN);
        final Field dateField = buildField("dateField", FieldType.DATE);
        dateField.setNullable(Boolean.FALSE);
        final Field doubleField = buildField("doubleField", FieldType.DOUBLE);
        final Field integerField = buildField("integerField", FieldType.INTEGER);
        final Field textField = buildField("textField", FieldType.TEXT);

        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName("BusinessObject");
        employee.addField(stringField);
        employee.addField(booleanField);
        employee.addField(dateField);
        employee.addField(doubleField);
        employee.addField(integerField);
        employee.addField(textField);
        return employee;
    }

    public BusinessObjectModel buildDefaultBOM() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(buildMyBusinessObject());
        return bom;
    }

    public BusinessObjectModel buildEmptyBOM() {
        return new BusinessObjectModel();
    }

    public BusinessObjectModel buildBOMWithAnEmptyEntity() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(new BusinessObject());
        return bom;
    }

    public BusinessObjectModel buildBOMWithAnEmptyField() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject businessObject = new BusinessObject();
        businessObject.setQualifiedName("BusinessObject");
        businessObject.addField(new Field());
        bom.addBusinessObject(businessObject);
        return bom;
    }

    public BusinessObjectModel buildBOMWithUniqueConstraint() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject employee = buildMyBusinessObject();
        employee.addUniqueConstraint("UC_string_double", "stringField", "doubleField");
        bom.addBusinessObject(employee);
        return bom;
    }
    
    public BusinessObjectModel buildBOMWithQuery() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject employee = buildMyBusinessObject();
        employee.addQuery("employeeByName","Select e FROM Employee e WHERE e.name='romain'",List.class.getName());
        bom.addBusinessObject(employee);
        return bom;
    }

}
