/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.client;

import static com.bonitasoft.engine.bdm.validator.rule.QueryParameterValidationRule.FORBIDDEN_PARAMETER_NAMES;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bonitasoft.engine.bdm.AbstractBDMCodeGenerator;
import com.bonitasoft.engine.bdm.BDMQueryUtil;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.Query;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JVar;

/**
 * @author Romain Bioteau
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class ClientBDMCodeGenerator extends AbstractBDMCodeGenerator {

    public ClientBDMCodeGenerator(final BusinessObjectModel bom) {
        super(bom);
    }

    @Override
    protected void addDAO(final BusinessObject bo, final JDefinedClass entity) throws JClassAlreadyExistsException, ClassNotFoundException {
        final JDefinedClass daoInterface = createDAOInterface(bo, entity);
        createDAOImpl(bo, entity, daoInterface);
    }

    private void createDAOImpl(final BusinessObject bo, final JDefinedClass entity, final JDefinedClass daoInterface) throws JClassAlreadyExistsException,
            ClassNotFoundException {
        final String daoImplClassName = toDaoImplClassname(bo);
        final JDefinedClass implClass = addClass(daoImplClassName);
        implClass._implements(daoInterface);

        createSessionConstructor(implClass);

        // Add method for provided queries
        for (final Query q : BDMQueryUtil.createProvidedQueriesForBusinessObject(bo)) {
            final JMethod method = createMethodForQuery(entity, implClass, q);
            addQueryMethodBody(entity.name(), method, q.getName(), entity.fullName());
        }

        // Add method for queries
        for (final Query q : bo.getQueries()) {
            final JMethod method = createMethodForQuery(entity, implClass, q);
            addQueryMethodBody(entity.name(), method, q.getName(), entity.fullName());
        }

        final JMethod method = createMethodForNewInstance(bo, entity, implClass);
        addNewInstanceMethodBody(method, entity);
    }

    private void createSessionConstructor(final JDefinedClass implClass) {
        final JClass apiSessionJClass = getModel().ref("org.bonitasoft.engine.session.APISession");
        implClass.field(JMod.PRIVATE, apiSessionJClass, "session");
        final JMethod constructor = implClass.constructor(JMod.PUBLIC);
        constructor.param(apiSessionJClass, "session");
        final JBlock body = constructor.body();
        body.assign(JExpr.refthis("session"), JExpr.ref("session"));
    }

    private void addQueryMethodBody(final String entityName, final JMethod method, final String queryName, final String returnType) {
        final JBlock body = method.body();

        final JTryBlock tryBlock = body._try();
        final JBlock tryBody = tryBlock.body();

        // Get CommandAPI
        final JClass tenantApiAccessorClass = getModel().ref("com.bonitasoft.engine.api.TenantAPIAccessor");
        final JClass commandApiType = getModel().ref("org.bonitasoft.engine.api.CommandAPI");
        final JVar commandApiRef = tryBody.decl(commandApiType, "commandApi", tenantApiAccessorClass.staticInvoke("getCommandAPI").arg(JExpr.ref("session")));

        // Create command parameters
        JClass mapClass = getModel().ref(Map.class);
        mapClass = mapClass.narrow(String.class, Serializable.class);

        JClass hashMapClass = getModel().ref(HashMap.class);
        hashMapClass = hashMapClass.narrow(String.class, Serializable.class);
        final JVar commandParametersRef = tryBody.decl(mapClass, "commandParameters", JExpr._new(hashMapClass));
        tryBody.invoke(commandParametersRef, "put").arg(JExpr.lit("queryName")).arg(JExpr.lit(entityName + "." + queryName));
        tryBody.invoke(commandParametersRef, "put").arg(JExpr.lit("returnType")).arg(JExpr.lit(returnType));

        // Set if should returns a List or a single value
        boolean isCollection = false;
        final JClass collectionClass = getModel().ref(Collection.class);
        if (method.type() instanceof JClass) {
            isCollection = collectionClass.isAssignableFrom((JClass) method.type());
        }
        tryBody.invoke(commandParametersRef, "put").arg(JExpr.lit("returnsList")).arg(JExpr.lit(isCollection));

        if (isCollection) {
            for (final String param : FORBIDDEN_PARAMETER_NAMES) {
                tryBody.invoke(commandParametersRef, "put").arg(JExpr.lit(param)).arg(JExpr.ref(param));
            }
        }

        // Add query parameters
        addQueryParameters(method, tryBody, mapClass, hashMapClass, commandParametersRef);

        // Execute command
        final JInvocation executeQuery = commandApiRef.invoke("execute").arg("executeBDMQuery").arg(commandParametersRef);
        final JClass serial = getModel().ref(byte[].class);
        final JClass omClass = getModel().ref(ObjectMapper.class);
        final JInvocation omObject = JExpr._new(omClass);
        final JVar omVar = tryBody.decl(omClass, "mapper", omObject);
        final JClass deserFeatureClass = getModel().ref(DeserializationFeature.class);
        tryBody.add(omVar.invoke("configure").arg(deserFeatureClass.staticRef("FAIL_ON_UNKNOWN_PROPERTIES")).arg(JExpr.FALSE));

        final JExpression invocation;
        final JClass ref = getModel().ref(returnType);
        final JExpression entityClassExpression = JExpr.dotclass(ref);
        if (isCollection) {
            final JClass list = getModel().ref(List.class);
            invocation = omVar.invoke("getTypeFactory").invoke("constructCollectionType").arg(JExpr.dotclass(list)).arg(entityClassExpression);
        } else if (method.type().binaryName().equals(returnType)) {
            invocation = entityClassExpression;
        } else {
            invocation = JExpr.dotclass(getModel().ref(method.type().binaryName()));
        }
        final JInvocation deserialize = omVar.invoke("readValue").arg(JExpr.cast(serial, executeQuery)).arg(invocation);
        tryBody._return(JExpr.cast(method.type(), deserialize));

        final JClass exceptionClass = getModel().ref(Exception.class);
        final JCatchBlock catchBlock = tryBlock._catch(exceptionClass);
        final JVar param = catchBlock.param("e");
        final JBlock catchBody = catchBlock.body();
        final JClass iaeClass = getModel().ref(IllegalArgumentException.class);
        catchBody._throw(JExpr._new(iaeClass).arg(JExpr.ref(null, param)));
    }

    private String toDaoImplClassname(final BusinessObject bo) {
        return bo.getQualifiedName() + DAO_IMPL_SUFFIX;
    }

}
