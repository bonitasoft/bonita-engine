package com.bonitasoft.engine.bdm.model.builder;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.model.field.RelationField.Type;
import com.bonitasoft.engine.bdm.model.field.SimpleField;

public class FieldBuilder {

    public static Field aBooleanField(String name) {
        SimpleField field = new SimpleField();
        field.setType(FieldType.BOOLEAN);
        field.setName(name);
        return field;
    }

    public static Field anAggregationField(String name, BusinessObject reference) {
        RelationField relationField = new RelationField();
        relationField.setName(name);
        relationField.setType(Type.AGGREGATION);
        relationField.setReference(reference);
        return relationField;
    }

}
