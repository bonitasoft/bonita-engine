package com.bonitasoft.engine.bdm.model.builder;

import static com.bonitasoft.engine.bdm.model.field.FieldType.BOOLEAN;
import static com.bonitasoft.engine.bdm.model.field.FieldType.STRING;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.model.field.RelationField.Type;
import com.bonitasoft.engine.bdm.model.field.SimpleField;

public class FieldBuilder {

    public static Field aBooleanField(String name) {
        return aSimpleField().withName(name).ofType(BOOLEAN).build();
    }

    public static SimpleFieldBuilder aStringField(String name) {
        return aSimpleField().withName(name).ofType(STRING);
    }
    
    public static Field anAggregationField(String name, BusinessObject reference) {
        RelationField relationField = new RelationField();
        relationField.setName(name);
        relationField.setType(Type.AGGREGATION);
        relationField.setReference(reference);
        return relationField;
    }
    
    public static SimpleFieldBuilder aSimpleField() {
        return new SimpleFieldBuilder();
    }

    public static class SimpleFieldBuilder extends FieldBuilder {
        private SimpleField field = new SimpleField();

        public SimpleFieldBuilder withName(String name) {
            field.setName(name);
            return this;
        }
        
        public SimpleFieldBuilder ofType(FieldType type) {
            field.setType(type);
            return this;
        }
        
        public SimpleFieldBuilder nullable() {
            field.setNullable(true);
            return this;
        }

        public SimpleField build() {
            return field;
        }
    }
}
