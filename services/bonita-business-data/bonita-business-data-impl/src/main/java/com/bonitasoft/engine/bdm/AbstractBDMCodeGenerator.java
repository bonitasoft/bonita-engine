/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.bonitasoft.engine.bdm.validator.BusinessObjectModelValidator;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JType;

/**
 * @author Romain Bioteau
 */
public abstract class AbstractBDMCodeGenerator extends CodeGenerator {

    private final BusinessObjectModel bom;

    public AbstractBDMCodeGenerator(final BusinessObjectModel bom) {
        super();
        if (bom == null) {
            throw new IllegalArgumentException("bom is null");
        }
        this.bom = bom;
    }

    protected void buildASTFromBom() throws JClassAlreadyExistsException, ClassNotFoundException {
        for (final BusinessObject bo : bom.getBusinessObjects()) {
            JDefinedClass entity = addEntity(bo);
            addDAO(bo, entity);
        }
    }

    protected abstract void addDAO(final BusinessObject bo, JDefinedClass entity) throws JClassAlreadyExistsException, ClassNotFoundException;

    protected JDefinedClass addEntity(final BusinessObject bo) throws JClassAlreadyExistsException {
        final String qualifiedName = bo.getQualifiedName();
        validateClassNotExistsInRuntime(qualifiedName);

        JDefinedClass entityClass = addClass(qualifiedName);
        entityClass = addInterface(entityClass, Serializable.class.getName());
        entityClass = addInterface(entityClass, com.bonitasoft.engine.bdm.Entity.class.getName());
        entityClass.javadoc().add(bo.getDescription());

        final JAnnotationUse entityAnnotation = addAnnotation(entityClass, Entity.class);
        entityAnnotation.param("name", entityClass.name());

        final JAnnotationUse tableAnnotation = addAnnotation(entityClass, Table.class);
        tableAnnotation.param("name", entityClass.name().toUpperCase());
        final List<UniqueConstraint> uniqueConstraints = bo.getUniqueConstraints();

        final List<Query> queries = bo.getQueries();

        JAnnotationUse namedQueriesAnnotation = null;
        JAnnotationArrayMember valueArray = null;
        if (!queries.isEmpty() || !uniqueConstraints.isEmpty()) {
            namedQueriesAnnotation = addAnnotation(entityClass, NamedQueries.class);
            valueArray = namedQueriesAnnotation.paramArray("value");
        }

        if (!uniqueConstraints.isEmpty()) {
            final JAnnotationArrayMember uniqueConstraintsArray = tableAnnotation.paramArray("uniqueConstraints");
            for (final UniqueConstraint uniqueConstraint : uniqueConstraints) {
                final JAnnotationUse uniqueConstraintAnnotatation = uniqueConstraintsArray.annotate(javax.persistence.UniqueConstraint.class);
                uniqueConstraintAnnotatation.param("name", uniqueConstraint.getName().toUpperCase());
                final JAnnotationArrayMember columnNamesParamArray = uniqueConstraintAnnotatation.paramArray("columnNames");
                for (final String fieldName : uniqueConstraint.getFieldNames()) {
                    columnNamesParamArray.param(fieldName.toUpperCase());
                }

                // Generate a query for Unique Constraint
                addNamedQuery(entityClass,
                        valueArray,
                        BDMQueryUtil.createQueryNameForUniqueConstraint(entityClass.name(), uniqueConstraint),
                        BDMQueryUtil.createQueryContentForUniqueConstraint(entityClass.name(), uniqueConstraint));
            }
        }

        for (final Query query : queries) {
            addNamedQuery(entityClass, valueArray, query.getName(), query.getContent());
        }

        addPersistenceIdFieldAndAccessors(entityClass);
        addPersistenceVersionFieldAndAccessors(entityClass);

        for (final Field field : bo.getFields()) {
            final JFieldVar basicField = addBasicField(entityClass, field);
            addAccessors(entityClass, basicField);
        }

        addEqualsMethod(entityClass);
        addHashCodeMethod(entityClass);

        return entityClass;
    }

    private void addNamedQuery(JDefinedClass entityClass, JAnnotationArrayMember valueArray, final String name, final String content) {
        final JAnnotationUse nameQueryAnnotation = valueArray.annotate(NamedQuery.class);
        nameQueryAnnotation.param("name", name);
        nameQueryAnnotation.param("query", content);
    }

    private void validateClassNotExistsInRuntime(final String qualifiedName) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        boolean alreadyInRuntime = true;
        try {
            contextClassLoader.loadClass(qualifiedName);
        } catch (final ClassNotFoundException e) {
            alreadyInRuntime = false;
        }
        if (alreadyInRuntime) {
            throw new IllegalArgumentException("Class " + qualifiedName + " already exists in target runtime environment.");
        }
    }

    protected void addPersistenceIdFieldAndAccessors(final JDefinedClass entityClass) throws JClassAlreadyExistsException {
        final JFieldVar idFieldVar = addField(entityClass, Field.PERSISTENCE_ID, toJavaType(FieldType.LONG));
        addAnnotation(idFieldVar, Id.class);
        addAnnotation(idFieldVar, GeneratedValue.class);
        addAccessors(entityClass, idFieldVar);
    }

    protected void addPersistenceVersionFieldAndAccessors(final JDefinedClass entityClass) throws JClassAlreadyExistsException {
        final JFieldVar versionField = addField(entityClass, Field.PERSISTENCE_VERSION, toJavaType(FieldType.LONG));
        addAnnotation(versionField, Version.class);
        addAccessors(entityClass, versionField);
    }

    protected JFieldVar addBasicField(final JDefinedClass entityClass, final Field field) throws JClassAlreadyExistsException {
        final JFieldVar fieldVar = addField(entityClass, field.getName(), toJavaType(field.getType()));
        final JAnnotationUse columnAnnotation = addAnnotation(fieldVar, Column.class);
        columnAnnotation.param("name", field.getName().toUpperCase());
        final Boolean nullable = field.isNullable();
        columnAnnotation.param("nullable", nullable == null || nullable);
        if (field.getType() == FieldType.DATE) {
            final JAnnotationUse temporalAnnotation = addAnnotation(fieldVar, Temporal.class);
            temporalAnnotation.param("value", TemporalType.TIMESTAMP);
        } else if (FieldType.TEXT.equals(field.getType())) {
            addAnnotation(fieldVar, Lob.class);
        } else if (FieldType.STRING.equals(field.getType()) && field.getLength() != null && field.getLength() > 0) {
            columnAnnotation.param("length", field.getLength());
        }
        return fieldVar;
    }

    protected void addAccessors(final JDefinedClass entityClass, final JFieldVar fieldVar) throws JClassAlreadyExistsException {
        addSetter(entityClass, fieldVar);
        addGetter(entityClass, fieldVar);
    }

    protected JType toJavaType(final FieldType type) {
        return getModel().ref(type.getClazz());
    }

    @Override
    public void generate(final File destDir) throws IOException, JClassAlreadyExistsException, BusinessObjectModelValidationException, ClassNotFoundException {
        final BusinessObjectModelValidator validator = new BusinessObjectModelValidator();
        final ValidationStatus validationStatus = validator.validate(bom);
        if (!validationStatus.isOk()) {
            throw new BusinessObjectModelValidationException(validationStatus);
        }
        buildASTFromBom();
        super.generate(destDir);
    }

}
