package com.bonitasoft.engine.bdm;

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
        employee.setQualifiedName("BusinessObject");
        employee.addField(stringField);
        employee.addField(booleanField);
        employee.addField(dateField);
        employee.addField(doubleField);
        employee.addField(integerField);

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

}
