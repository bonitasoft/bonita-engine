package com.bonitasoft.engine.bdm;

import static org.apache.commons.lang3.StringUtils.left;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;

import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.model.field.RelationField.Type;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;

/**
 * @author Colin PUY
 */
public class RelationFieldAnnotator {

    private CodeGenerator codeGenerator;

    public RelationFieldAnnotator(CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    public void annotateRelationField(JDefinedClass entityClass, final RelationField rfield, final JFieldVar fieldVar) {
        JAnnotationUse relation = null;
        if (rfield.isCollection()) {
            if (rfield.getType() == Type.AGGREGATION) {
                relation = codeGenerator.addAnnotation(fieldVar, ManyToMany.class);
                JAnnotationUse joinTable = codeGenerator.addAnnotation(fieldVar, JoinTable.class);
                joinTable.param("name", getJoinTableName(entityClass.name(), rfield.getReference().getSimpleName()));
            } else {
                relation = codeGenerator.addAnnotation(fieldVar, OneToMany.class);
                JAnnotationUse joinColumn = addJoinColumn(fieldVar, entityClass.name());
                joinColumn.param("nullable", false);
            }
            codeGenerator.addAnnotation(fieldVar, OrderColumn.class);
        } else {
            if (rfield.getType() == Type.AGGREGATION) {
                relation = codeGenerator.addAnnotation(fieldVar, ManyToOne.class);
            } else {
                relation = codeGenerator.addAnnotation(fieldVar, OneToOne.class);
            }
            addJoinColumn(fieldVar, rfield.getName());
            relation.param("optional", rfield.isNullable());
        }
        relation.param("fetch", FetchType.EAGER);
        if (rfield.getType() == Type.COMPOSITION) {
            final JAnnotationArrayMember cascade = relation.paramArray("cascade");
            cascade.param(CascadeType.ALL);
        }
    }

    private JAnnotationUse addJoinColumn(final JFieldVar fieldVar, String columnName) {
        JAnnotationUse joinColumn = codeGenerator.addAnnotation(fieldVar, JoinColumn.class);
        joinColumn.param("name", getJoinColumnName(columnName));
        return joinColumn;
    }

    /**
     * Split names to 26 char to avoid joinColumn names longer than 30 char
     * protected for testing
     */
    protected String getJoinColumnName(String entityName) {
        return left(entityName.toUpperCase(), 26) + "_PID";
    }

    /**
     * Split names to 14 chars max to avoid joinTable names longer than 30 char (oracle restriction).
     * protected for testing
     */
    protected String getJoinTableName(String entityName, String relatedEntityName) {
        String name = left(entityName.toUpperCase(), 14);
        String refName = left(relatedEntityName.toUpperCase(), 14);
        return name + "_" + refName;
    }
}
