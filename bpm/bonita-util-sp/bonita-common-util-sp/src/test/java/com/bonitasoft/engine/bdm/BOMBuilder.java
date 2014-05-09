package com.bonitasoft.engine.bdm;

import java.util.List;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.SimpleField;

public class BOMBuilder {

    private BusinessObjectModel businessObjectModel = new BusinessObjectModel();

    public static BOMBuilder aBOM() {
        return new BOMBuilder();
    }

    public BOMBuilder withBO(BusinessObject bo) {
        businessObjectModel.addBusinessObject(bo);
        return this;
    }

    public BusinessObjectModel build() {
        return businessObjectModel;
    }

    private SimpleField buildField(final String name, final FieldType type) {
        final SimpleField field = new SimpleField();
        field.setName(name);
        field.setType(type);
        return field;
    }

    private BusinessObject buildMyBusinessObject() {
        final SimpleField stringField = buildField("stringField", FieldType.STRING);
        stringField.setNullable(Boolean.TRUE);
        final SimpleField booleanField = buildField("booleanField", FieldType.BOOLEAN);
        final SimpleField dateField = buildField("dateField", FieldType.DATE);
        dateField.setNullable(Boolean.FALSE);
        final SimpleField doubleField = buildField("doubleField", FieldType.DOUBLE);
        final SimpleField integerField = buildField("integerField", FieldType.INTEGER);
        final SimpleField textField = buildField("textField", FieldType.TEXT);

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
        businessObject.addField(new SimpleField());
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
        employee.addQuery("employeeByName", "Select e FROM Employee e WHERE e.name='romain'", List.class.getName());
        bom.addBusinessObject(employee);
        return bom;
    }

}
