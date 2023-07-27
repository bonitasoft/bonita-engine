/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bdm.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

import org.bonitasoft.engine.bdm.BDMSimpleNameProvider;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.RelationField.Type;
import org.bonitasoft.engine.bdm.model.field.SimpleField;

/**
 * @author Matthieu Chaffotte
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "description", "fields", "uniqueConstraints", "queries", "indexes" })
public class BusinessObject {

    @XmlID
    @XmlAttribute(required = true)
    private String qualifiedName;

    @XmlElement
    private String description;

    @XmlElementWrapper(name = "fields", required = true)
    @XmlElements({ @XmlElement(name = "field", type = SimpleField.class, required = true),
            @XmlElement(name = "relationField", type = RelationField.class, required = true) })
    private List<Field> fields;

    @XmlElementWrapper(name = "uniqueConstraints")
    @XmlElement(name = "uniqueConstraint")
    private List<UniqueConstraint> uniqueConstraints;

    @XmlElementWrapper(name = "indexes")
    @XmlElement(name = "index", required = false)
    private List<Index> indexes;

    @XmlElementWrapper(name = "queries")
    @XmlElement(name = "query")
    private List<Query> queries;

    public BusinessObject() {
        fields = new ArrayList<>();
        uniqueConstraints = new ArrayList<>();
        queries = new ArrayList<>();
        indexes = new ArrayList<>();
    }

    public BusinessObject(String qualifiedName) {
        this();
        this.qualifiedName = qualifiedName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(final String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(final List<Field> fields) {
        this.fields = fields;
    }

    public void addField(final Field field) {
        fields.add(field);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void addUniqueConstraint(UniqueConstraint uniqueConstraint) {
        uniqueConstraints.add(uniqueConstraint);
    }

    public UniqueConstraint addUniqueConstraint(final String name, final String... fieldNames) {
        if (fieldNames == null || fieldNames.length == 0) {
            throw new IllegalArgumentException("fieldNames cannot be null or empty");
        }
        final UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setName(name);
        uniqueConstraint.setFieldNames(Arrays.asList(fieldNames));

        addUniqueConstraint(uniqueConstraint);
        return uniqueConstraint;
    }

    public List<UniqueConstraint> getUniqueConstraints() {
        return uniqueConstraints;
    }

    public void setUniqueConstraints(final List<UniqueConstraint> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
    }

    public Query addQuery(final String name, final String queryContent, final String returnType) {
        return addQuery(name, null, queryContent, returnType);
    }

    public Query addQuery(final String name, final String description, final String queryContent,
            final String returnType) {
        final Query query = new Query(name, description, queryContent, returnType);
        queries.add(query);
        return query;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(final List<Query> queries) {
        this.queries = queries;
    }

    public void addIndex(Index index) {
        indexes.add(index);
    }

    public Index addIndex(final String name, final String... fieldNames) {
        if (fieldNames == null || fieldNames.length == 0) {
            throw new IllegalArgumentException("fieldNames cannot be null or empty");
        }
        final Index index = new Index();
        index.setName(name);

        index.setFieldNames(Arrays.asList(fieldNames));
        addIndex(index);
        return index;
    }

    public List<Index> getIndexes() {
        return indexes;
    }

    public void setIndexes(final List<Index> indexes) {
        this.indexes = indexes;
    }

    public List<BusinessObject> getReferencedBusinessObjectsByComposition() {
        List<BusinessObject> refs = new ArrayList<>();
        for (Field field : fields) {
            if (isACompositionField(field)) {
                refs.add(((RelationField) field).getReference());
            }
        }
        return refs;
    }

    private boolean isACompositionField(Field field) {
        return field instanceof RelationField && Type.COMPOSITION == ((RelationField) field).getType();
    }

    public boolean isARelationField(final String name) {
        boolean relationField = false;
        boolean found = false;
        final Iterator<Field> iter = fields.iterator();
        while (!found && iter.hasNext()) {
            final Field field = iter.next();
            if (field.getName().equals(name)) {
                found = true;
                if (field instanceof RelationField) {
                    relationField = true;
                }
            }
        }
        return relationField;
    }

    public String getSimpleName() {
        return BDMSimpleNameProvider.getSimpleBusinessObjectName(qualifiedName);
    }

    public Field getField(String fieldName) {
        for (final Field f : getFields()) {
            if (f.getName().equals(fieldName)) {
                return f;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BusinessObject that = (BusinessObject) o;
        return Objects.equals(qualifiedName, that.qualifiedName) && Objects.equals(description, that.description)
                && Objects.equals(fields, that.fields) && Objects.equals(uniqueConstraints, that.uniqueConstraints)
                && Objects.equals(indexes, that.indexes) && Objects.equals(queries, that.queries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifiedName, description, fields, uniqueConstraints, indexes, queries);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BusinessObject.class.getSimpleName() + "[", "]")
                .add("description='" + description + "'")
                .add("fields=" + fields)
                .add("indexes=" + indexes)
                .add("qualifiedName='" + qualifiedName + "'")
                .add("queries=" + queries)
                .add("uniqueConstraints=" + uniqueConstraints)
                .toString();
    }

}
