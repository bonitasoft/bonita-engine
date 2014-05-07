package com.bonitasoft.engine.bdm.model.builder;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.Field;
import com.bonitasoft.engine.bdm.model.FieldType;
import com.bonitasoft.engine.bdm.model.Relationship;

public class FieldBuilder {

    private final FieldType fieldType;
    private final String name;
    private Boolean collection;
    private Integer length;
    private Boolean nullable;
    private Relationship relationship;

    public FieldBuilder(String name, FieldType fieldType) {
        this.name = name;
        this.fieldType = fieldType;
    }

    public static FieldBuilder aCompositionField(String name, BusinessObject compositeBO) {
        FieldBuilder builder = new FieldBuilder(name, FieldType.BUSINESSOBJECT);
        Relationship relationship = new Relationship();
        relationship.setBusinessObject(compositeBO);
        relationship.setMode(Relationship.Mode.COMPOSITION);
        builder.relationship = relationship;
        return builder;
    }
    
    public static FieldBuilder aBooleanField(String name) {
        return new FieldBuilder(name, FieldType.BOOLEAN);
    }
    
    public Field build() {
        Field field = new Field();
        field.setType(fieldType);
        field.setName(name);
        if (collection != null) field.setCollection(collection);
        if (length != null) field.setLength(length);
        if (nullable != null) field.setNullable(nullable);
        if (relationship != null) field.setRelationship(relationship);
        return field;
    }
}
