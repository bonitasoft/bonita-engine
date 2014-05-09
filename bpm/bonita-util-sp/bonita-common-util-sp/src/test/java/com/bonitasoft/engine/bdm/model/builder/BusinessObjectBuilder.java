package com.bonitasoft.engine.bdm.model.builder;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.field.Field;

public class BusinessObjectBuilder {

    private BusinessObject businessObject = new BusinessObject();

    public BusinessObjectBuilder(String qualifiedName) {
        this.businessObject = new BusinessObject();
        this.businessObject.setQualifiedName(qualifiedName);
    }

    public static BusinessObjectBuilder aBO(String qualifiedName) {
        return new BusinessObjectBuilder(qualifiedName);
    }

    public BusinessObject build() {
        return businessObject;
    }

    public BusinessObjectBuilder withField(Field field) {
        businessObject.addField(field);
        return this;
    }

}
