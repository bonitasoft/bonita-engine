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
import java.util.Collection;
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

import com.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import com.bonitasoft.engine.bdm.validator.BusinessObjectModelValidator;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

/**
 * @author Romain Bioteau
 */
public class BDMCodeGenerator extends CodeGenerator {

    private static final String DAO_SUFFIX = "DAO";

    private final BusinessObjectModel bom;

    public BDMCodeGenerator(final BusinessObjectModel bom) {
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

    protected void addDAO(final BusinessObject bo, JDefinedClass entity) throws JClassAlreadyExistsException, ClassNotFoundException {
        // TODO add BO DAO Interface + Impl
        String daoInterfaceClassName = toDaoInterfaceClassname(bo);
        JDefinedClass daoInterface = addInterface(daoInterfaceClassName);
        addInterface(daoInterface, BusinessObjectDAO.class.getName());

        // Add method signature in interface for queries
        for (Query q : bo.getQueries()) {
            String name = q.getName();
            JType returnType = getModel().parseType(q.getReturnType());
            JClass collectionType = (JClass) getModel().parseType(Collection.class.getName());
            if (returnType instanceof JClass && collectionType.isAssignableFrom((JClass) returnType)) {
                returnType = ((JClass) returnType).narrow(entity);
            }
            JMethod method = addMethodSignature(daoInterface, name, returnType);
            for (QueryParameter param : q.getQueryParameters()) {
                method.param(getModel().parseType(param.getClassName()), param.getName());
            }
        }

        // Add method signature in interface for unique constraint
        for (UniqueConstraint uc : bo.getUniqueConstraints()) {
            String name = createQueryNameForUniqueConstraint(entity, uc);
            JMethod method = addMethodSignature(daoInterface, name, entity);
            for (String param : uc.getFieldNames()) {
                method.param(getModel().parseType(getFieldType(param, bo)), param);
            }
        }

    }

    private String createQueryNameForUniqueConstraint(JDefinedClass entity, UniqueConstraint uc) {
        StringBuilder sb = new StringBuilder("get" + entity.name() + "By");
        for (String f : uc.getFieldNames()) {
            f = Character.toUpperCase(f.charAt(0)) + f.substring(1);
            sb.append(f);
            sb.append("And");
        }
        String name = sb.toString();
        if (name.endsWith("And")) {
            name = name.substring(0, name.length() - 3);
        }
        return name;
    }

    private String getFieldType(String param, BusinessObject bo) {
        for (Field f : bo.getFields()) {
            if (f.getName().equals(param)) {
                return f.getType().getClazz().getName();
            }
        }
        return null;
    }

    private String toDaoInterfaceClassname(BusinessObject bo) {
        return bo.getQualifiedName() + DAO_SUFFIX;
    }

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
        if (!uniqueConstraints.isEmpty()) {
            final JAnnotationArrayMember uniqueConstraintsArray = tableAnnotation.paramArray("uniqueConstraints");

            for (final UniqueConstraint uniqueConstraint : uniqueConstraints) {
                final JAnnotationUse uniqueConstraintAnnotatation = uniqueConstraintsArray.annotate(javax.persistence.UniqueConstraint.class);
                uniqueConstraintAnnotatation.param("name", uniqueConstraint.getName().toUpperCase());
                final JAnnotationArrayMember columnNamesParamArray = uniqueConstraintAnnotatation.paramArray("columnNames");
                for (final String fieldName : uniqueConstraint.getFieldNames()) {
                    columnNamesParamArray.param(fieldName.toUpperCase());
                }
            }
        }

        final List<Query> queries = bo.getQueries();
        if (!queries.isEmpty()) {
            final JAnnotationUse namedQueriesAnnotation = addAnnotation(entityClass, NamedQueries.class);
            final JAnnotationArrayMember valueArray = namedQueriesAnnotation.paramArray("value");
            for (final Query query : queries) {
                final JAnnotationUse nameQueryAnnotation = valueArray.annotate(NamedQuery.class);
                nameQueryAnnotation.param("name", query.getName());
                nameQueryAnnotation.param("query", query.getContent());
            }
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
