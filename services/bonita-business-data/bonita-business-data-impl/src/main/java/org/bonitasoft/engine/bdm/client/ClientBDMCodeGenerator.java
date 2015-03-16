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
package org.bonitasoft.engine.bdm.client;

import static org.bonitasoft.engine.bdm.validator.rule.QueryParameterValidationRule.FORBIDDEN_PARAMETER_NAMES;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bdm.AbstractBDMCodeGenerator;
import org.bonitasoft.engine.bdm.BDMQueryUtil;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.Query;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
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

    private static final String CLIENT_RESOURCES_PACKAGES = "org.bonitasoft.engine.bdm.dao.client.resources";

    public ClientBDMCodeGenerator() {
        super();
    }

    @Override
    protected void addDAO(final BusinessObject bo, final JDefinedClass entity) throws JClassAlreadyExistsException {
        final JDefinedClass daoInterface = createDAOInterface(bo, entity);
        createDAOImpl(bo, entity, daoInterface);
    }

    private void createDAOImpl(final BusinessObject bo, final JDefinedClass entity, final JDefinedClass daoInterface) throws JClassAlreadyExistsException {
        final String daoImplClassName = toDaoImplClassname(bo);
        final JDefinedClass implClass = addClass(daoImplClassName);
        implClass._implements(daoInterface);

        createConstructor(implClass);

        // Add method for provided queries
        for (final Query q : BDMQueryUtil.createProvidedQueriesForBusinessObject(bo)) {
            final JMethod method = createMethodForQuery(entity, implClass, q);
            addQueryMethodBody(entity.name(), method, q.getName(), entity.fullName(), q.getReturnType());
        }

        // Add method for queries
        for (final Query q : bo.getQueries()) {
            final JMethod method = createMethodForQuery(entity, implClass, q);
            addQueryMethodBody(entity.name(), method, q.getName(), entity.fullName(), q.getReturnType());
        }

        final JMethod method = createMethodForNewInstance(bo, entity, implClass);
        addNewInstanceMethodBody(method, entity);
    }

    private void createConstructor(final JDefinedClass implClass) {
        final JClass apiSessionJClass = getModel().ref("org.bonitasoft.engine.session.APISession");
        implClass.field(JMod.PRIVATE, apiSessionJClass, "session");

        final JClass deserializerClass = getModel().ref(CLIENT_RESOURCES_PACKAGES + ".BusinessObjectDeserializer");
        implClass.field(JMod.PRIVATE, deserializerClass, "deserializer");

        final JClass proxyfierClass = getModel().ref(CLIENT_RESOURCES_PACKAGES + ".proxy.Proxyfier");
        implClass.field(JMod.PRIVATE, proxyfierClass, "proxyfier");

        final JMethod constructor = implClass.constructor(JMod.PUBLIC);
        constructor.param(apiSessionJClass, "session");

        final JBlock body = constructor.body();
        body.assign(JExpr.refthis("session"), JExpr.ref("session"));
        body.assign(JExpr.refthis("deserializer"), JExpr._new(deserializerClass));
        final JClass lazyLoaderClass = getModel().ref(CLIENT_RESOURCES_PACKAGES + ".proxy.LazyLoader");
        final JVar lazyLoaderRef = body.decl(lazyLoaderClass, "lazyLoader", JExpr._new(lazyLoaderClass).arg(JExpr.ref("session")));
        body.assign(JExpr.refthis("proxyfier"), JExpr._new(proxyfierClass).arg(lazyLoaderRef));
    }

    private void addQueryMethodBody(final String entityName, final JMethod method, final String queryName, final String entityClassName,
            final String queryReturnType) {
        final JBlock body = method.body();

        final JTryBlock tryBlock = body._try();
        final JBlock tryBody = tryBlock.body();

        // Get CommandAPI
        final JClass tenantApiAccessorClass = getModel().ref("org.bonitasoft.engine.api.TenantAPIAccessor");
        final JClass commandApiType = getModel().ref("org.bonitasoft.engine.api.CommandAPI");
        final JVar commandApiRef = tryBody.decl(commandApiType, "commandApi", tenantApiAccessorClass.staticInvoke("getCommandAPI").arg(JExpr.ref("session")));

        // Create command parameters
        JClass mapClass = getModel().ref(Map.class);
        mapClass = mapClass.narrow(String.class, Serializable.class);

        JClass hashMapClass = getModel().ref(HashMap.class);
        hashMapClass = hashMapClass.narrow(String.class, Serializable.class);
        final JVar commandParametersRef = tryBody.decl(mapClass, "commandParameters", JExpr._new(hashMapClass));
        tryBody.invoke(commandParametersRef, "put").arg(JExpr.lit("queryName")).arg(JExpr.lit(entityName + "." + queryName));

        // Set if should returns a List or a single value
        boolean isCollection = false;
        final JClass collectionClass = getModel().ref(Collection.class);
        if (method.type() instanceof JClass) {
            isCollection = collectionClass.isAssignableFrom((JClass) method.type());
        }
        tryBody.invoke(commandParametersRef, "put").arg(JExpr.lit("returnsList")).arg(JExpr.lit(isCollection));

        if (isCollection) {
            tryBody.invoke(commandParametersRef, "put").arg(JExpr.lit("returnType")).arg(JExpr.lit(entityClassName));
            for (final String param : FORBIDDEN_PARAMETER_NAMES) {
                tryBody.invoke(commandParametersRef, "put").arg(JExpr.lit(param)).arg(JExpr.ref(param));
            }
        } else {
            tryBody.invoke(commandParametersRef, "put").arg(JExpr.lit("returnType")).arg(JExpr.lit(queryReturnType));
        }

        // Add query parameters
        addQueryParameters(method, tryBody, mapClass, hashMapClass, commandParametersRef);

        // Execute command
        final JInvocation executeQuery = commandApiRef.invoke("execute").arg("executeBDMQuery").arg(commandParametersRef);
        final JClass byteArrayClass = getModel().ref(byte[].class);
        final JFieldRef deserializerFieldRef = JExpr.ref("deserializer");
        final JFieldRef proxyfierFieldRef = JExpr.ref("proxyfier");
        JExpression entityClassExpression = null;
        if (isCollection) {
            entityClassExpression = JExpr.dotclass(getModel().ref(entityClassName));
        } else {
            entityClassExpression = JExpr.dotclass(getModel().ref(queryReturnType));
        }
        JInvocation deserialize = null;
        if (isCollection) {
            deserialize = deserializerFieldRef.invoke("deserializeList").arg(JExpr.cast(byteArrayClass, executeQuery)).arg(entityClassExpression);
            tryBody._return(proxyfierFieldRef.invoke("proxify").arg(deserialize));
        } else if (queryReturnType.equals(entityClassName)) {
            deserialize = deserializerFieldRef.invoke("deserialize").arg(JExpr.cast(byteArrayClass, executeQuery)).arg(entityClassExpression);
            tryBody._return(proxyfierFieldRef.invoke("proxify").arg(deserialize));
        } else {
            tryBody._return(JExpr.cast(getModel().ref(queryReturnType), executeQuery));
        }

        final JClass exceptionClass = getModel().ref(Exception.class);
        final JCatchBlock catchBlock = tryBlock._catch(exceptionClass);
        final JVar param = catchBlock.param("e");
        final JBlock catchBody = catchBlock.body();
        final JClass iaeClass = getModel().ref(IllegalArgumentException.class);
        catchBody._throw(JExpr._new(iaeClass).arg(JExpr.ref(null, param)));
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

    private String toDaoImplClassname(final BusinessObject bo) {
        return bo.getQualifiedName() + DAO_IMPL_SUFFIX;
    }

}
