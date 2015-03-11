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
package org.bonitasoft.engine.bdm;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;

import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.RelationField.Type;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;

import static org.apache.commons.lang3.StringUtils.left;

/**
 * @author Colin PUY
 */
public class RelationFieldAnnotator {

    private final CodeGenerator codeGenerator;

    public RelationFieldAnnotator(final CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    public void annotateRelationField(final JDefinedClass entityClass, final RelationField field, final JFieldVar fieldVar) {
        JAnnotationUse relation = null;
        if (field.isCollection()) {
            relation = annotateMultipleReference(entityClass, field, fieldVar);
        } else {
            relation = annotateSingleReference(field, fieldVar);
        }

        if (field.isLazy()) {
            relation.param("fetch", FetchType.LAZY);
            codeGenerator.addAnnotation(fieldVar, JsonIgnore.class);
        } else {
            relation.param("fetch", FetchType.EAGER);
        }

        if (field.getType() == Type.COMPOSITION) {
            relation.param("cascade", CascadeType.ALL);
        }
        else if (field.getType() == Type.AGGREGATION) {
            relation.param("cascade", CascadeType.MERGE);
        }
    }

    private JAnnotationUse annotateSingleReference(final RelationField field, final JFieldVar fieldVar) {
        JAnnotationUse relation;
        if (field.getType() == Type.AGGREGATION) {
            relation = codeGenerator.addAnnotation(fieldVar, ManyToOne.class);
        } else {
            relation = codeGenerator.addAnnotation(fieldVar, OneToOne.class);
            relation.param("orphanRemoval", true);
        }
        addJoinColumn(fieldVar, field.getName());
        relation.param("optional", field.isNullable());
        return relation;
    }

    private JAnnotationUse annotateMultipleReference(final JDefinedClass entityClass, final RelationField field, final JFieldVar fieldVar) {
        JAnnotationUse relation;
        if (field.getType() == Type.AGGREGATION) {
            relation = codeGenerator.addAnnotation(fieldVar, ManyToMany.class);
            addJoinTable(entityClass, field, fieldVar);

        } else {
            relation = codeGenerator.addAnnotation(fieldVar, OneToMany.class);
            relation.param("orphanRemoval", true);
            final JAnnotationUse joinColumn = addJoinColumn(fieldVar, entityClass.name());
            joinColumn.param("nullable", false);
        }
        codeGenerator.addAnnotation(fieldVar, OrderColumn.class);
        return relation;
    }

    private void addJoinTable(final JDefinedClass entityClass, final RelationField field, final JFieldVar fieldVar) {
        final JAnnotationUse joinTable = codeGenerator.addAnnotation(fieldVar, JoinTable.class);
        joinTable.param("name", getJoinTableName(entityClass.name(), field.getName()));

        final JAnnotationArrayMember joinColumns = joinTable.paramArray("joinColumns");
        final JAnnotationUse nameQueryAnnotation = joinColumns.annotate(JoinColumn.class);
        nameQueryAnnotation.param("name", getJoinColumnName(entityClass.name()));

        final JAnnotationArrayMember inverseJoinColumns = joinTable.paramArray("inverseJoinColumns");
        final JAnnotationUse a = inverseJoinColumns.annotate(JoinColumn.class);
        a.param("name", getJoinColumnName(field.getReference().getSimpleName()));
    }

    private JAnnotationUse addJoinColumn(final JFieldVar fieldVar, final String columnName) {
        final JAnnotationUse joinColumn = codeGenerator.addAnnotation(fieldVar, JoinColumn.class);
        joinColumn.param("name", getJoinColumnName(columnName));
        return joinColumn;
    }

    /**
     * Split names to 26 char to avoid joinColumn names longer than 30 char
     * protected for testing
     */
    protected String getJoinColumnName(final String entityName) {
        return left(entityName.toUpperCase(), 26) + "_PID";
    }

    /**
     * Split names to 14 chars max to avoid joinTable names longer than 30 char (oracle restriction).
     * protected for testing
     */
    protected String getJoinTableName(final String entityName, final String relatedEntityName) {
        final String name = left(entityName.toUpperCase(), 14);
        final String refName = left(relatedEntityName.toUpperCase(), 14);
        return name + "_" + refName;
    }
}
