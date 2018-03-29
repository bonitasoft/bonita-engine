/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bdm.builder;

import static org.bonitasoft.engine.bdm.model.field.FieldType.BOOLEAN;
import static org.bonitasoft.engine.bdm.model.field.FieldType.DATE;
import static org.bonitasoft.engine.bdm.model.field.FieldType.DOUBLE;
import static org.bonitasoft.engine.bdm.model.field.FieldType.INTEGER;
import static org.bonitasoft.engine.bdm.model.field.FieldType.STRING;
import static org.bonitasoft.engine.bdm.model.field.FieldType.TEXT;
import static org.bonitasoft.engine.bdm.model.field.RelationField.FetchType.LAZY;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.RelationField.FetchType;
import org.bonitasoft.engine.bdm.model.field.RelationField.Type;
import org.bonitasoft.engine.bdm.model.field.SimpleField;

/**
 * @author Colin PUY
 */
public abstract class FieldBuilder {

    protected final Field field;

    private FieldBuilder(final Field field) {
        this.field = field;
    }

    public static Field aBooleanField(final String name) {
        return aSimpleField().withName(name).ofType(BOOLEAN).build();
    }

    public static SimpleFieldBuilder aStringField(final String name) {
        return aSimpleField().withName(name).ofType(STRING);
    }

    public static SimpleFieldBuilder aDateField(final String name) {
        return aSimpleField().withName(name).ofType(DATE);
    }

    public static SimpleFieldBuilder aDoubleField(final String name) {
        return aSimpleField().withName(name).ofType(DOUBLE);
    }

    public static SimpleFieldBuilder anIntegerField(final String name) {
        return aSimpleField().withName(name).ofType(INTEGER);
    }

    public static SimpleFieldBuilder aTextField(final String name) {
        return aSimpleField().withName(name).ofType(TEXT);
    }

    public static SimpleFieldBuilder aSimpleField() {
        return new SimpleFieldBuilder();
    }

    public static RelationFieldBuilder aRelationField() {
        return new RelationFieldBuilder();
    }

    public static Field anAggregationField(final String name, final BusinessObject reference) {
        final RelationField relationField = aRelationField(name, reference);
        relationField.setType(Type.AGGREGATION);
        return relationField;
    }

    public static Field aCompositionField(final String name, final BusinessObject reference) {
        final RelationField relationField = aRelationField(name, reference);
        relationField.setType(Type.COMPOSITION);
        return relationField;
    }

    private static RelationField aRelationField(final String name, final BusinessObject reference) {
        final RelationField relationField = new RelationField();
        relationField.setName(name);
        relationField.setReference(reference);
        return relationField;
    }

    public FieldBuilder withName(final String name) {
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

    public FieldBuilder multiple() {
        field.setCollection(true);
        return this;
    }

    public FieldBuilder multiple(final boolean collection) {
        field.setCollection(collection);
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

        public SimpleFieldBuilder ofType(final FieldType type) {
            ((SimpleField) field).setType(type);
            return this;
        }

        @Override
        public SimpleFieldBuilder withName(final String name) {
            return (SimpleFieldBuilder) super.withName(name);
        }

        @Override
        public SimpleFieldBuilder nullable() {
            return (SimpleFieldBuilder) super.nullable();
        }

        @Override
        public SimpleFieldBuilder notNullable() {
            return (SimpleFieldBuilder) super.notNullable();
        }

        public FieldBuilder withLength(final int length) {
            ((SimpleField) field).setLength(length);
            return this;
        }
    }

    public static class RelationFieldBuilder extends FieldBuilder {

        public RelationFieldBuilder() {
            super(new RelationField());
        }

        public RelationFieldBuilder ofType(final Type type) {
            ((RelationField) field).setType(type);
            return this;
        }

        public RelationFieldBuilder composition() {
            ((RelationField) field).setType(Type.COMPOSITION);
            return this;
        }

        public RelationFieldBuilder aggregation() {
            ((RelationField) field).setType(Type.AGGREGATION);
            return this;
        }

        public RelationFieldBuilder lazy() {
            ((RelationField) field).setFetchType(LAZY);
            return this;
        }

        public RelationFieldBuilder fetchType(final FetchType fetchType) {
            ((RelationField) field).setFetchType(fetchType);
            return this;
        }

        @Override
        public RelationFieldBuilder withName(final String name) {
            return (RelationFieldBuilder) super.withName(name);
        }

        @Override
        public RelationFieldBuilder multiple() {
            return (RelationFieldBuilder) super.multiple();
        }

        @Override
        public RelationFieldBuilder multiple(final boolean collection) {
            return (RelationFieldBuilder) super.multiple(collection);
        }

        public RelationFieldBuilder referencing(final BusinessObject bo) {
            ((RelationField) field).setReference(bo);
            return this;
        }

        @Override
        public RelationField build() {
            return (RelationField) super.build();
        }
    }
}
