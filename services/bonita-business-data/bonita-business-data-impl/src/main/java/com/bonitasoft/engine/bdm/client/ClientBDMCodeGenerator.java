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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bonitasoft.engine.bdm.AbstractBDMCodeGenerator;
import com.bonitasoft.engine.bdm.BDMQueryUtil;
import com.bonitasoft.engine.bdm.BusinessObject;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.Field;
import com.bonitasoft.engine.bdm.Query;
import com.bonitasoft.engine.bdm.QueryParameter;
import com.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
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

    private static final List<String> FILTERED_METHOD_PARAMS = Arrays.asList("startIndex", "maxResults");

    public ClientBDMCodeGenerator(final BusinessObjectModel bom) {
        super(bom);
    }

    @Override
    protected void addDAO(final BusinessObject bo, final JDefinedClass entity) throws JClassAlreadyExistsException, ClassNotFoundException {
        JDefinedClass daoInterface = createDAOInterface(bo, entity);
        createDAOImpl(bo, entity, daoInterface);
    }

    private JDefinedClass createDAOInterface(final BusinessObject bo, final JDefinedClass entity) throws JClassAlreadyExistsException, ClassNotFoundException {
        String daoInterfaceClassName = toDaoInterfaceClassname(bo);
        JDefinedClass daoInterface = addInterface(daoInterfaceClassName);
        addInterface(daoInterface, BusinessObjectDAO.class.getName());

        // Add method signature in interface for provided queries
        for (Query q : BDMQueryUtil.createProvidedQueriesForBusinessObject(bo)) {
            createMethodForQuery(entity, daoInterface, q);
        }

        // Add method signature in interface for custom queries
        for (Query q : bo.getQueries()) {
            createMethodForQuery(entity, daoInterface, q);
        }

        return daoInterface;
    }

    private void createDAOImpl(final BusinessObject bo, final JDefinedClass entity, final JDefinedClass daoInterface) throws JClassAlreadyExistsException,
            ClassNotFoundException {
        String daoImplClassName = toDaoImplClassname(bo);
        JDefinedClass implClass = addClass(daoImplClassName);
        implClass._implements(daoInterface);

        createSessionConstructor(implClass);

        // Add method for provided queries
        for (Query q : BDMQueryUtil.createProvidedQueriesForBusinessObject(bo)) {
            JMethod method = createMethodForQuery(entity, daoInterface, q);
            addQueryMethodBody(method, q.getName(), entity.fullName());
        }

        // Add method for queries
        for (Query q : bo.getQueries()) {
            JMethod method = createMethodForQuery(entity, implClass, q);
            addQueryMethodBody(method, q.getName(), entity.fullName());
        }

    }

    private void createSessionConstructor(final JDefinedClass implClass) {
        JClass apiSessionJClass = getModel().ref("org.bonitasoft.engine.session.APISession");
        implClass.field(JMod.PRIVATE, apiSessionJClass, "session");
        JMethod constructor = implClass.constructor(JMod.PUBLIC);
        constructor.param(apiSessionJClass, "session");
        JBlock body = constructor.body();
        body.assign(JExpr.refthis("session"), JExpr.ref("session"));
    }

    private void addQueryMethodBody(final JMethod method, final String queryName, final String returnType) {
        JBlock body = method.body();

        // Get CommandAPI
        JClass tenantApiAccessorClass = getModel().ref("com.bonitasoft.engine.api.TenantAPIAccessor");
        JClass commandApiType = getModel().ref("org.bonitasoft.engine.api.CommandAPI");
        JVar commandApiRef = body.decl(commandApiType, "commandApi", tenantApiAccessorClass.staticInvoke("getCommandAPI").arg(JExpr.ref("session")));

        // Create command parameters
        JClass mapClass = getModel().ref(Map.class);
        mapClass = mapClass.narrow(String.class, Serializable.class);

        JClass hashMapClass = getModel().ref(HashMap.class);
        hashMapClass = hashMapClass.narrow(String.class, Serializable.class);
        JVar commandParametersRef = body.decl(mapClass, "commandParameters", JExpr._new(hashMapClass));
        body.invoke(commandParametersRef, "put").arg(JExpr.lit("queryName")).arg(JExpr.lit(queryName));
        body.invoke(commandParametersRef, "put").arg(JExpr.lit("returnType")).arg(JExpr.lit(returnType));

        // Set if should returns a List or a single value
        boolean isCollection = false;
        JClass collectionClass = getModel().ref(Collection.class);
        if (method.type() instanceof JClass) {
            isCollection = collectionClass.isAssignableFrom((JClass) method.type());
        }
        body.invoke(commandParametersRef, "put").arg(JExpr.lit("returnsList")).arg(JExpr.lit(isCollection));

        if (isCollection) {
            for (String param : FILTERED_METHOD_PARAMS) {
                body.invoke(commandParametersRef, "put").arg(JExpr.lit(param)).arg(JExpr.ref(param));
            }
        }

        // Add query parameters
        addQueryParameters(method, body, mapClass, hashMapClass, commandParametersRef);

        // Execute command
        body._return(JExpr.cast(method.type(), commandApiRef.invoke("execute").arg("executeBDMQuery").arg(commandParametersRef)));
    }

    protected void addQueryParameters(final JMethod method, final JBlock body, final JClass mapClass, final JClass hashMapClass, final JVar commandParametersRef) {
        if (!method.params().isEmpty()) {
            JVar queryParametersRef = body.decl(mapClass, "queryParameters", JExpr._new(hashMapClass));
            for (JVar param : method.params()) {
                if (!FILTERED_METHOD_PARAMS.contains(param.name())) {
                    body.invoke(queryParametersRef, "put").arg(JExpr.lit(param.name())).arg(param);
                }
            }
            body.invoke(commandParametersRef, "put").arg(JExpr.lit("queryParameters")).arg(JExpr.cast(getModel().ref(Serializable.class), queryParametersRef));
        }
    }

    private String toDaoImplClassname(final BusinessObject bo) {
        return bo.getQualifiedName() + DAO_IMPL_SUFFIX;
    }

    private JMethod createMethodForQuery(final JDefinedClass entity, final JDefinedClass targetClass, final Query q) throws ClassNotFoundException {
        JMethod queryMethod = createQueryMethod(entity, targetClass, q.getName(), q.getReturnType());
        for (QueryParameter param : q.getQueryParameters()) {
            queryMethod.param(getModel().parseType(param.getClassName()), param.getName());
        }
        addOptionalPaginationParameters(queryMethod, q.getReturnType());
        return queryMethod;
    }

    private void addOptionalPaginationParameters(final JMethod queryMethod, final String returnType) throws ClassNotFoundException {
        if ("java.util.List".equals(returnType)) {
            for (String param : FILTERED_METHOD_PARAMS) {
                queryMethod.param(getModel().parseType(int.class.getName()), param);
            }
        }
    }

    private JMethod createQueryMethod(final JDefinedClass entity, final JDefinedClass targetClass, final String name, final String returnTypeName)
            throws ClassNotFoundException {
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

    private String getFieldType(final String param, final BusinessObject bo) {
        for (Field f : bo.getFields()) {
            if (f.getName().equals(param)) {
                return f.getType().getClazz().getName();
            }
        }
        return null;
    }

    private String toDaoInterfaceClassname(final BusinessObject bo) {
        return bo.getQualifiedName() + DAO_SUFFIX;
    }

}
