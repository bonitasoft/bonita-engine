package com.bonitasoft.engine.bdm;

import java.util.Arrays;

public class BOMBuilder {

    private Field buildField(final String name, final FieldType type) {
        final Field field = new Field();
        field.setName(name);
        field.setType(type);
        return field;
    }

    private BusinessObject buildMyBusinessObject() {
        final Field stringField = buildField("stringField", FieldType.STRING);
        final Field booleanField = buildField("booleanField", FieldType.BOOLEAN);
        final Field dateField = buildField("dateField", FieldType.DATE);
        final Field doubleField = buildField("doubleField", FieldType.DOUBLE);
        final Field integerField = buildField("integerField", FieldType.INTEGER);

        final BusinessObject employee = new BusinessObject();
        employee.setClassName("BusinessObject");
        employee.setFields(Arrays.asList(stringField, booleanField, dateField, doubleField, integerField));
        return employee;
    }

    public BusinessObjectModel buildDefaultBOM() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(buildMyBusinessObject());
        return bom;
    }

}
