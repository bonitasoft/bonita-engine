/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import static com.bonitasoft.engine.bdm.validator.rule.QueryParameterValidationRule.FORBIDDEN_PARAMETER_NAMES;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import com.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.Query;
import com.bonitasoft.engine.bdm.model.QueryParameter;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bdm.validator.BusinessObjectModelValidator;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 */
public abstract class AbstractBDMCodeGenerator extends CodeGenerator {

    private static final String DAO_SUFFIX = "DAO";

    protected static final String DAO_IMPL_SUFFIX = "DAOImpl";

    private static final String NEW_INSTANCE_METHOD_NAME = "newInstance";

    protected final BusinessObjectModel bom;

    public AbstractBDMCodeGenerator(final BusinessObjectModel bom) {
        super();
        if (bom == null) {
            throw new IllegalArgumentException("bom is null");
        }
        this.bom = bom;
    }

    public void buildJavaModelFromBom() throws JClassAlreadyExistsException, ClassNotFoundException {
        final EntityCodeGenerator entityCodeGenerator = new EntityCodeGenerator(this);
        for (final BusinessObject bo : bom.getBusinessObjects()) {
            final JDefinedClass entity = entityCodeGenerator.addEntity(bo);
            addDAO(bo, entity);
        }
    }

    protected void addNewInstanceMethodBody(final JMethod method, final JDefinedClass entity) {
        final JBlock methodBody = method.body();
        for (final JVar param : method.params()) {
            addNotNullParamCheck(methodBody, param);
        }
        final JVar instanceRef = methodBody.decl(entity, "instance", JExpr._new(entity));
        for (final JVar param : method.params()) {
            callSetter(methodBody, param, instanceRef, entity);
        }
        methodBody._return(instanceRef);
    }

    private void callSetter(final JBlock methodBody, final JVar param, final JVar instanceRef, final JDefinedClass entity) {
        final JMethod setter = getSetterForParam(param, entity);
        if (setter == null) {
            throw new IllegalStateException("No setter found for parameter " + param.name());
        }
        methodBody.add(instanceRef.invoke(setter).arg(param));
    }

    private JMethod getSetterForParam(final JVar param, final JDefinedClass entity) {
        final String setterName = getSetterName(param);
        return entity.getMethod(setterName, new JType[] { param.type() });
    }

    private void addNotNullParamCheck(final JBlock methodBody, final JVar param) {
        methodBody._if(param.eq(JExpr._null()))._then()
        ._throw(JExpr._new(getModel().ref(IllegalArgumentException.class)).arg(JExpr.lit(param.name() + " cannot be null")));
    }

    protected abstract void addDAO(final BusinessObject bo, JDefinedClass entity) throws JClassAlreadyExistsException, ClassNotFoundException;

    protected void addCopyConstructor(final JDefinedClass entityClass, final BusinessObject bo) {
        final JMethod copyConstructor = entityClass.constructor(JMod.PUBLIC);
        final JVar param = copyConstructor.param(entityClass, WordUtils.uncapitalize(entityClass.name()));
        final JBlock copyBody = copyConstructor.body();
        copyBody.assign(JExpr.refthis(Field.PERSISTENCE_ID), JExpr.invoke(JExpr.ref(param.name()), "getPersistenceId"));
        copyBody.assign(JExpr.refthis(Field.PERSISTENCE_VERSION), JExpr.invoke(JExpr.ref(param.name()), "getPersistenceVersion"));
        for (final Field field : bo.getFields()) {
            if (field.isCollection() != null && field.isCollection()) {
                final JClass fieldClass = toJavaClass(field);
                final JClass arrayListFieldClazz = narrowClass(ArrayList.class, fieldClass);
                if (field instanceof SimpleField) {
                    copyBody.assign(JExpr.refthis(field.getName()),
                            JExpr._new(arrayListFieldClazz).arg(JExpr.invoke(JExpr.ref(param.name()), getGetterName(field))));
                } else {
                    copyBody.assign(JExpr.refthis(field.getName()), JExpr._new(arrayListFieldClazz));
                    final JForEach forEach = copyBody.forEach(fieldClass, "i", JExpr.invoke(JExpr.ref(param.name()), getGetterName(field)));
                    forEach.body().invoke(JExpr.refthis(field.getName()), "add").arg(JExpr._new(fieldClass).arg(forEach.var()));
                }
            } else {
                copyBody.assign(JExpr.refthis(field.getName()), JExpr.invoke(JExpr.ref(param.name()), getGetterName(field)));
            }
        }
    }

    @Override
    public void generate(final File destDir) throws IOException, JClassAlreadyExistsException, BusinessObjectModelValidationException, ClassNotFoundException {
        final BusinessObjectModelValidator validator = new BusinessObjectModelValidator();
        final ValidationStatus validationStatus = validator.validate(bom);
        if (!validationStatus.isOk()) {
            throw new BusinessObjectModelValidationException(validationStatus);
        }
        buildJavaModelFromBom();
        super.generate(destDir);
    }

    protected JDefinedClass createDAOInterface(final BusinessObject bo, final JDefinedClass entity) throws JClassAlreadyExistsException, ClassNotFoundException {
        final String daoInterfaceClassName = toDaoInterfaceClassname(bo);
        final JDefinedClass daoInterface = addInterface(daoInterfaceClassName);
        addInterface(daoInterface, BusinessObjectDAO.class.getName());

        // Add method signature in interface for provided queries
        for (final Query q : BDMQueryUtil.createProvidedQueriesForBusinessObject(bo)) {
            createMethodForQuery(entity, daoInterface, q);
        }

        // Add method signature in interface for custom queries
        for (final Query q : bo.getQueries()) {
            createMethodForQuery(entity, daoInterface, q);
        }

        createMethodForNewInstance(bo, entity, daoInterface);

        return daoInterface;
    }

    protected JMethod createMethodForNewInstance(final BusinessObject bo, final JDefinedClass entity, final JDefinedClass daoInterface) {
        final JMethod newInstanceMethod = addMethodSignature(daoInterface, NEW_INSTANCE_METHOD_NAME, entity);
        for (final Field field : bo.getFields()) {
            if (!field.isNullable()) {
                String typeClassName = null;
                if (field instanceof SimpleField) {
                    typeClassName = ((SimpleField) field).getType().getClazz().getName();
                } else if (field instanceof RelationField) {
                    typeClassName = ((RelationField) field).getReference().getQualifiedName();
                }
                newInstanceMethod.param(getModel().ref(typeClassName), field.getName());
            }
        }
        return newInstanceMethod;
    }

    protected JMethod createMethodForQuery(final JDefinedClass entity, final JDefinedClass targetClass, final Query query) throws ClassNotFoundException {
        final String methodName = query.getName();
        final JMethod queryMethod = createQueryMethod(entity, targetClass, methodName, query.getReturnType());
        for (final QueryParameter param : query.getQueryParameters()) {
            queryMethod.param(getModel().ref(param.getClassName()), param.getName());
        }
        addOptionalPaginationParameters(queryMethod, query.getReturnType());
        return queryMethod;
    }

    private JMethod createQueryMethod(final JDefinedClass entity, final JDefinedClass targetClass, final String name, final String returnTypeName)
            throws ClassNotFoundException {
        JType returnType;
        if (returnTypeName.equals(entity.fullName())) {
            returnType = entity;
        } else {
            returnType = getModel().ref(returnTypeName);
        }
        final JClass collectionType = getModel().ref(Collection.class.getName());
        if (returnType instanceof JClass && collectionType.isAssignableFrom((JClass) returnType)) {
            returnType = ((JClass) returnType).narrow(entity);
        }
        return addMethodSignature(targetClass, name, returnType);
    }

    private String toDaoInterfaceClassname(final BusinessObject bo) {
        return bo.getQualifiedName() + DAO_SUFFIX;
    }

    private void addOptionalPaginationParameters(final JMethod queryMethod, final String returnType) throws ClassNotFoundException {
        if (List.class.getName().equals(returnType)) {
            for (final String param : FORBIDDEN_PARAMETER_NAMES) {
                queryMethod.param(getModel().ref(int.class.getName()), param);
            }
        }
    }

    protected void addQueryParameters(final JMethod method, final JBlock body, final JClass mapClass, final JClass hashMapClass, final JVar commandParametersRef) {
        if (!method.params().isEmpty()) {
            final JVar queryParametersRef = body.decl(mapClass, "queryParameters", JExpr._new(hashMapClass));
            for (final JVar param : method.params()) {
                if (!FORBIDDEN_PARAMETER_NAMES.contains(param.name())) {
                    body.invoke(queryParametersRef, "put").arg(JExpr.lit(param.name())).arg(param);
                }
            }
            body.invoke(commandParametersRef, "put").arg(JExpr.lit("queryParameters")).arg(JExpr.cast(getModel().ref(Serializable.class), queryParametersRef));
        }
    }

    protected static String suffixPackage(final String qualifiedName, final String packageSuffix) {
        final int pointIdx = qualifiedName.lastIndexOf('.');
        return qualifiedName.substring(0, pointIdx + 1) + packageSuffix + "." + qualifiedName.substring(pointIdx + 1);
    }

}
