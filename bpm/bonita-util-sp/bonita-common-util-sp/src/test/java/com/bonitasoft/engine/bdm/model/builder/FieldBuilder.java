package com.bonitasoft.engine.bdm.model.builder;

import com.bonitasoft.engine.bdm.model.SimpleField;
import com.bonitasoft.engine.bdm.model.FieldType;

public class FieldBuilder {

    private final FieldType fieldType;
    private final String name;
    private Boolean collection;
    private Integer length;
    private Boolean nullable;

    public FieldBuilder(String name, FieldType fieldType) {
        this.name = name;
        this.fieldType = fieldType;
    }

    public static FieldBuilder aBooleanField(String name) {
        return new FieldBuilder(name, FieldType.BOOLEAN);
    }
    
    public SimpleField build() {
        SimpleField field = new SimpleField();
        field.setType(fieldType);
        field.setName(name);
        if (collection != null) field.setCollection(collection);
        if (length != null) field.setLength(length);
        if (nullable != null) field.setNullable(nullable);
        return field;
    }
}
