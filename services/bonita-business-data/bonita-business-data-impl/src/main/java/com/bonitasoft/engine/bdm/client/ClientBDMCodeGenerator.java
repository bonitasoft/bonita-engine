/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bonitasoft.engine.bdm.AbstractBDMCodeGenerator;
import com.bonitasoft.engine.bdm.BusinessObject;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.Field;
import com.bonitasoft.engine.bdm.Query;
import com.bonitasoft.engine.bdm.QueryParameter;
import com.bonitasoft.engine.bdm.UniqueConstraint;
import com.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import com.bonitasoft.engine.bdm.validator.QueryNameUtil;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * @author Romain Bioteau
 */
public class ClientBDMCodeGenerator extends AbstractBDMCodeGenerator {

    private static final String DAO_SUFFIX = "DAO";

    private static final String DAO_IMPL_SUFFIX = "DAOImpl";

    public ClientBDMCodeGenerator(final BusinessObjectModel bom) {
        super(bom);
    }

    protected void addDAO(final BusinessObject bo, JDefinedClass entity) throws JClassAlreadyExistsException, ClassNotFoundException {
        JDefinedClass daoInterface = createDAOInterface(bo, entity);
        createDAOImpl(bo, entity, daoInterface);
    }

    private JDefinedClass createDAOInterface(final BusinessObject bo, JDefinedClass entity) throws JClassAlreadyExistsException, ClassNotFoundException {
        String daoInterfaceClassName = toDaoInterfaceClassname(bo);
        JDefinedClass daoInterface = addInterface(daoInterfaceClassName);
        addInterface(daoInterface, BusinessObjectDAO.class.getName());
        // Add method signature in interface for queries
        for (Query q : bo.getQueries()) {
            createMethodForQuery(entity, daoInterface, q);
        }
        // Add method signature in interface for unique constraint
        for (UniqueConstraint uc : bo.getUniqueConstraints()) {
            createMethodForUniqueConstraint(bo, entity, daoInterface, uc);
        }
        return daoInterface;
    }

    private JDefinedClass createDAOImpl(final BusinessObject bo, JDefinedClass entity, JDefinedClass daoInterface) throws JClassAlreadyExistsException,
            ClassNotFoundException {
        String daoImplClassName = toDaoImplClassname(bo);
        JDefinedClass implClass = addClass(daoImplClassName);
        implClass._implements(daoInterface);

        createSessionConstructor(implClass);

        // Add method for queries
        for (Query q : bo.getQueries()) {
            JMethod method = createMethodForQuery(entity, implClass, q);
            addQueryMethodBody(method, q.getName(), entity.fullName());
        }

        // Add method for unique constraint
        for (UniqueConstraint uc : bo.getUniqueConstraints()) {
            JMethod method = createMethodForUniqueConstraint(bo, entity, implClass, uc);
            addQueryMethodBody(method, QueryNameUtil.createQueryNameForUniqueConstraint(entity.name(), uc), entity.fullName());
        }
        return daoInterface;
    }

    private void createSessionConstructor(JDefinedClass implClass) {
        JClass apiSessionJClass = getModel().ref("org.bonitasoft.engine.session.APISession");
        implClass.field(JMod.PRIVATE, apiSessionJClass, "session");
        JMethod constructor = implClass.constructor(JMod.PUBLIC);
        constructor.param(apiSessionJClass, "session");
        JBlock body = constructor.body();
        body.assign(JExpr.refthis("session"), JExpr.ref("session"));
    }

    private void addQueryMethodBody(JMethod method, String queryName, String returnType) {
        JBlock body = method.body();

        // Get CommandAPI
        JClass tenantApiAccessorClass = getModel().ref("com.bonitasoft.engine.api.TenantAPIAccessor");
        JClass commandApiType = getModel().ref("org.bonitasoft.engine.api.CommandAPI");
        JVar commandApiRef = body.decl(commandApiType, "commandApi", tenantApiAccessorClass.staticInvoke("getCommandAPI").arg(
                JExpr.ref("session")));

        // Create command parameters
        JClass mapClass = getModel().ref(Map.class);
        mapClass = mapClass.narrow(String.class, Serializable.class);

        JClass hashMapClass = getModel().ref(HashMap.class);
        hashMapClass = hashMapClass.narrow(String.class, Serializable.class);
        JVar commandParametersRef = body.decl(mapClass, "commandParameters", JExpr._new(hashMapClass));
        body.invoke(commandParametersRef, "put").arg(JExpr.lit("queryName")).arg(JExpr.lit(queryName));
        body.invoke(commandParametersRef, "put").arg(JExpr.lit("returnType")).arg(JExpr.lit(returnType));

        // Set if should returns a List or a single value
        boolean assignableFrom = false;
        JClass collectionClass = getModel().ref(Collection.class);
        if (method.type() instanceof JClass) {
            assignableFrom = collectionClass.isAssignableFrom((JClass) method.type());
        }
        body.invoke(commandParametersRef, "put").arg(JExpr.lit("returnsList")).arg(JExpr.lit(assignableFrom));

        // Add query parameters
        if (!method.params().isEmpty()) {
            JVar queryParametersRef = body.decl(mapClass, "queryParameters", JExpr._new(hashMapClass));
            for (JVar param : method.params()) {
                body.invoke(queryParametersRef, "put").arg(JExpr.lit(param.name())).arg(param);
            }
            body.invoke(commandParametersRef, "put").arg(JExpr.lit("queryParameters")).arg(JExpr.cast(getModel().ref(Serializable.class), queryParametersRef));
        }

        // Execute command
        body._return(JExpr.cast(method.type(), commandApiRef.invoke("execute").arg("executeBDMQuery").arg(commandParametersRef)));
    }

    private String toDaoImplClassname(BusinessObject bo) {
        return bo.getQualifiedName() + DAO_IMPL_SUFFIX;
    }

    private JMethod createMethodForUniqueConstraint(final BusinessObject bo, JDefinedClass entity, JDefinedClass targetClass, UniqueConstraint uc)
            throws ClassNotFoundException {
        String name = QueryNameUtil.createQueryNameForUniqueConstraint(entity.name(), uc);
        JMethod queryMethod = createQueryMethod(entity, targetClass, name, entity.fullName());
        for (String param : uc.getFieldNames()) {
            queryMethod.param(getModel().parseType(getFieldType(param, bo)), param);
        }
        return queryMethod;
    }

    private JMethod createMethodForQuery(JDefinedClass entity, JDefinedClass targetClass, Query q) throws ClassNotFoundException {
        JMethod queryMethod = createQueryMethod(entity, targetClass, q.getName(), q.getReturnType());
        for (QueryParameter param : q.getQueryParameters()) {
            queryMethod.param(getModel().parseType(param.getClassName()), param.getName());
        }
        return queryMethod;
    }

    private JMethod createQueryMethod(JDefinedClass entity, JDefinedClass targetClass, String name, String returnTypeName) throws ClassNotFoundException {
        JType returnType = getModel().parseType(returnTypeName);
        JClass collectionType = (JClass) getModel().parseType(Collection.class.getName());
        if (returnType instanceof JClass && collectionType.isAssignableFrom((JClass) returnType)) {
            returnType = ((JClass) returnType).narrow(entity);
        }
        JMethod method = addMethodSignature(targetClass, name, returnType);
        addThrows(method, "org.bonitasoft.engine.command.CommandNotFoundException");
        addThrows(method, "org.bonitasoft.engine.command.CommandExecutionException");
        addThrows(method, "org.bonitasoft.engine.command.CommandParameterizationException");
        addThrows(method, "org.bonitasoft.engine.exception.BonitaHomeNotSetException");
        addThrows(method, "org.bonitasoft.engine.exception.UnknownAPITypeException");
        addThrows(method, "org.bonitasoft.engine.exception.ServerAPIException");

        return method;
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

}
