/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model.builder;

import static com.bonitasoft.engine.bdm.model.field.FieldType.BOOLEAN;
import static com.bonitasoft.engine.bdm.model.field.FieldType.DATE;
import static com.bonitasoft.engine.bdm.model.field.FieldType.DOUBLE;
import static com.bonitasoft.engine.bdm.model.field.FieldType.INTEGER;
import static com.bonitasoft.engine.bdm.model.field.FieldType.STRING;
import static com.bonitasoft.engine.bdm.model.field.FieldType.TEXT;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.model.field.RelationField.Type;
import com.bonitasoft.engine.bdm.model.field.SimpleField;

/**
 * @author Colin PUY
 */
public abstract class FieldBuilder {

    protected final Field field;

    private FieldBuilder(Field field) {
        this.field = field;
    }

    public static Field aBooleanField(String name) {
        return aSimpleField().withName(name).ofType(BOOLEAN).build();
    }

    public static SimpleFieldBuilder aStringField(String name) {
        return aSimpleField().withName(name).ofType(STRING);
    }

    public static SimpleFieldBuilder aDateField(String name) {
        return aSimpleField().withName(name).ofType(DATE);
    }

    public static SimpleFieldBuilder aDoubleField(String name) {
        return aSimpleField().withName(name).ofType(DOUBLE);
    }

    public static SimpleFieldBuilder anIntegerField(String name) {
        return aSimpleField().withName(name).ofType(INTEGER);
    }

    public static SimpleFieldBuilder aTextField(String name) {
        return aSimpleField().withName(name).ofType(TEXT);
    }

    public static SimpleFieldBuilder aSimpleField() {
        return new SimpleFieldBuilder();
    }

    public static Field anAggregationField(String name, BusinessObject reference) {
        RelationField relationField = aRelationField(name, reference);
        relationField.setType(Type.AGGREGATION);
        return relationField;
    }
    
    public static Field aCompositionField(String name, BusinessObject reference) {
        RelationField relationField = aRelationField(name, reference);
        relationField.setType(Type.COMPOSITION);
        return relationField;
    }
    
    private static RelationField aRelationField(String name, BusinessObject reference) {
        RelationField relationField = new RelationField();
        relationField.setName(name);
        relationField.setReference(reference);
        return relationField;
    }

    public FieldBuilder withName(String name) {
        field.setName(name);
        return this;
    }
    
    public FieldBuilder nullable() {
        field.setNullable(true);
        return this;
    }
    
    public FieldBuilder notNullable() {
        field.setNullable(false);
        return this;
    }
    
    public Field build() {
        return field;
    }

    /**
     * SimpleFieldBuilder 
     */
    public static class SimpleFieldBuilder extends FieldBuilder {

        public SimpleFieldBuilder() {
            super(new SimpleField());
        }

        public SimpleFieldBuilder ofType(FieldType type) {
            ((SimpleField) field).setType(type);
            return this;
        }

        public SimpleFieldBuilder withName(String name) {
            return (SimpleFieldBuilder) super.withName(name);
        }
        
        public SimpleFieldBuilder nullable() {
            return (SimpleFieldBuilder) super.nullable();
        }

        public SimpleFieldBuilder notNullable() {
            return (SimpleFieldBuilder) super.notNullable();
        }
    }
}
