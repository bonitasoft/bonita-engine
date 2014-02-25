package com.bonitasoft.engine;

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
    
    public byte[] buildZip() {
        BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        try {
            return converter.zip(this.build());
        } catch (Exception e) {
           throw new RuntimeException("Unable to build BOM zip");
        }
    }
}
