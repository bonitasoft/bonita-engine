/**
 * Copyright (C) 2015-2017 BonitaSoft S.A.
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
package org.bonitasoft.engine.business.data.impl;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.engine.bdm.BDMQueryUtil;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.QueryParameter;
import org.bonitasoft.engine.bdm.model.field.RelationField.Type;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataQueryResult;
import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataQueryMetadataImpl;
import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataQueryResultImpl;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.BusinessDataService;
import org.bonitasoft.engine.business.data.JsonBusinessDataSerializer;
import org.bonitasoft.engine.business.data.NonUniqueResultException;
import org.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.JavaMethodInvoker;
import org.bonitasoft.engine.commons.TypeConverterUtil;
import org.bonitasoft.engine.commons.exceptions.SReflectException;

public class BusinessDataServiceImpl implements BusinessDataService {

    private final BusinessDataRepository businessDataRepository;

    private final JsonBusinessDataSerializer jsonBusinessDataSerializer;

    private final BusinessDataModelRepository businessDataModelRepository;

    private final TypeConverterUtil typeConverterUtil;

    private BusinessDataReloader businessDataReloader;

    private final CountQueryProvider countQueryProvider;

    public BusinessDataServiceImpl(final BusinessDataRepository businessDataRepository, final JsonBusinessDataSerializer jsonBusinessDataSerializer,
            final BusinessDataModelRepository businessDataModelRepository, final TypeConverterUtil typeConverterUtil,
            BusinessDataReloader businessDataReloader, CountQueryProvider countQueryProvider) {
        this.businessDataRepository = businessDataRepository;
        this.jsonBusinessDataSerializer = jsonBusinessDataSerializer;
        this.businessDataModelRepository = businessDataModelRepository;
        this.typeConverterUtil = typeConverterUtil;
        this.businessDataReloader = businessDataReloader;
        this.countQueryProvider = countQueryProvider;
    }

    @Override
    public boolean isBusinessData(final Object data) {
        return isEntity(data) || isListOfEntities(data);
    }

    private boolean isListOfEntities(final Object data) {
        if (data == null) {
            return false;
        }
        if (!List.class.isAssignableFrom(data.getClass())) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        final List dataList = (List) data;
        if (dataList.isEmpty()) {
            return true;
        }
        return isEntity(dataList.get(0));

    }

    private boolean isEntity(final Object data) {
        if (data == null) {
            return false;
        }
        return Entity.class.isAssignableFrom(data.getClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object callJavaOperation(final Object businessObject, final Object valueToSetObjectWith, final String methodName, final String parameterType)
            throws SBusinessDataNotFoundException, SBusinessDataRepositoryException {
        if (businessObject == null) {
            throw new SBusinessDataNotFoundException("business data is null");
        }
        if (isEntity(businessObject)) {
            return callJavaOperationOnEntity((Entity) businessObject, valueToSetObjectWith, methodName, parameterType);
        }
        if (isListOfEntities(businessObject)) {
            return callJavaOperationOnEntityList((List<Entity>) businessObject, valueToSetObjectWith, methodName, parameterType);
        }
        throw new SBusinessDataRepositoryException("not a business data");
    }

    private Object callJavaOperationOnEntityList(final List<Entity> businessObject, final Object valueToSetObjectWith, final String methodName,
            final String parameterType)
                    throws SBusinessDataRepositoryException, SBusinessDataNotFoundException {
        try {
            invokeJavaMethod(businessObject, methodName, parameterType, valueToSetObjectWith);
            return businessObject;
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryException(e);
        }
    }

    private Object callJavaOperationOnEntity(final Entity businessObject, final Object valueToSetObjectWith, final String methodName,
            final String parameterType)
                    throws SBusinessDataRepositoryException, SBusinessDataNotFoundException {
        Entity jpaEntity = businessDataReloader.reloadEntitySoftly(businessObject);
        final Object valueToSet = loadValueToSet(businessObject, valueToSetObjectWith, methodName);
        try {
            invokeJavaMethod(jpaEntity, methodName, parameterType, valueToSet);
            // TODO Auto-generated method stub
            return jpaEntity;
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryException(e);
        }
    }

    protected void invokeJavaMethod(final Object objectToSet, final String methodName, final String parameterType, final Object valueToSet)
            throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final JavaMethodInvoker methodInvoker = new JavaMethodInvoker();
        methodInvoker.invokeJavaMethod(parameterType, valueToSet, objectToSet, methodName, parameterType);
    }

    @SuppressWarnings("unchecked")
    private Object loadValueToSet(final Entity businessObject, final Object valueToSetObjectWith, final String methodName)
            throws SBusinessDataNotFoundException, SBusinessDataRepositoryException {
        Object valueToSet;
        if (isEntity(valueToSetObjectWith)) {
            final Type relationType = getRelationType(businessObject, methodName);
            valueToSet = getPersistedValue((Entity) valueToSetObjectWith, relationType);
        } else if (isListOfEntities(valueToSetObjectWith)) {
            final Type relationType = getRelationType(businessObject, methodName);
            valueToSet = getPersistedValues((List<Entity>) valueToSetObjectWith, relationType);
        } else {
            valueToSet = valueToSetObjectWith;
        }
        return valueToSet;
    }

    private List<Long> getPrimaryKeys(final List<Entity> entities) throws SBusinessDataNotFoundException {
        List<Long> primaryKeys;
        primaryKeys = new ArrayList<>();
        for (final Entity entity : entities) {
            if (entity.getPersistenceId() == null) {
                throw new SBusinessDataNotFoundException("persistenceId of business data is null");
            }
            primaryKeys.add(entity.getPersistenceId());
        }
        return primaryKeys;
    }

    private Object getPersistedValues(final List<Entity> entities, final Type type) throws SBusinessDataNotFoundException {
        if (entities.isEmpty()) {
            return new ArrayList<Entity>();
        }
        if (Type.AGGREGATION.equals(type)) {
            return businessDataRepository.findByIds(businessDataReloader.getEntityRealClass(entities.get(0)), getPrimaryKeys(entities));
        } else {
            return entities;
        }
    }

    private Entity getPersistedValue(final Entity entity, final Type type) throws SBusinessDataNotFoundException {
        if (Type.AGGREGATION.equals(type)) {
            return businessDataReloader.reloadEntity(entity);
        } else {
            return entity;
        }
    }

    private Type getRelationType(final Entity businessObject, final String methodName) throws SBusinessDataRepositoryException {
        final String fieldName = ClassReflector.getFieldName(methodName);
        Annotation[] annotations;
        try {
            annotations = businessObject.getClass().getDeclaredField(fieldName).getAnnotations();
        } catch (final NoSuchFieldException e) {
            return null;
        } catch (final SecurityException e) {
            throw new SBusinessDataRepositoryException(e);
        }
        for (final Annotation annotation : annotations) {
            final Set<Class<? extends Annotation>> annotationKeySet = getAnnotationKeySet();
            if (annotationKeySet.contains(annotation.annotationType())) {
                try {
                    final Method cascade = annotation.getClass().getMethod("cascade");
                    final CascadeType[] cascadeTypes = (CascadeType[]) cascade.invoke(annotation);
                    if (CascadeType.MERGE.equals(cascadeTypes[0])) {
                        return Type.AGGREGATION;
                    }
                    if (CascadeType.ALL.equals(cascadeTypes[0])) {
                        return Type.COMPOSITION;
                    }
                } catch (final Exception e) {
                    throw new SBusinessDataRepositoryException(e);
                }
            }
        }
        return null;
    }

    private Set<Class<? extends Annotation>> getAnnotationKeySet() {
        // FIXME use custom annotation on methods
        final Set<Class<? extends Annotation>> annotationKeySet = new HashSet<>();
        annotationKeySet.add(OneToOne.class);
        annotationKeySet.add(OneToMany.class);
        annotationKeySet.add(ManyToMany.class);
        annotationKeySet.add(ManyToOne.class);
        return annotationKeySet;
    }

    @Override
    public Serializable getJsonEntity(final String entityClassName, final Long identifier, final String businessDataURIPattern)
            throws SBusinessDataNotFoundException, SBusinessDataRepositoryException {
        final Class<? extends Entity> entityClass = loadClass(entityClassName);
        final Entity entity = businessDataRepository.findById(entityClass, identifier);
        return jsonBusinessDataSerializer.serializeEntity(entity, businessDataURIPattern);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Serializable getJsonEntities(final String entityClassName, final List<Long> identifiers, final String businessDataURIPattern)
            throws SBusinessDataRepositoryException {
        final Class<? extends Entity> entityClass = loadClass(entityClassName);
        final List<? extends Entity> entities = businessDataRepository.findByIdentifiers(entityClass, identifiers);
        return jsonBusinessDataSerializer.serializeEntities(entities, businessDataURIPattern);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Serializable getJsonChildEntity(final String entityClassName, final Long identifier, final String childFieldName,
            final String businessDataURIPattern)
                    throws SBusinessDataNotFoundException, SBusinessDataRepositoryException {
        final Class<? extends Entity> entityClass = loadClass(entityClassName);
        final Object entity = businessDataRepository.findById(entityClass, identifier);

        Object childEntity;
        java.lang.reflect.Type getterReturnType;
        try {
            final String getterName = ClassReflector.getGetterName(childFieldName);
            childEntity = ClassReflector.invokeGetter(entity, getterName);
            getterReturnType = ClassReflector.getGetterReturnType(entityClass, getterName);
        } catch (final SReflectException e) {
            throw new SBusinessDataRepositoryException(e);
        }

        if (childEntity == null) {
            return JsonBusinessDataSerializer.EMPTY_OBJECT;
        }
        if (childEntity instanceof Entity) {
            final Entity unwrap = businessDataRepository.unwrap((Entity) childEntity);
            return jsonBusinessDataSerializer.serializeEntity(unwrap, businessDataURIPattern);
        } else if (childEntity instanceof List) {
            final Class<?> type = (Class<?>) ((ParameterizedType) getterReturnType).getActualTypeArguments()[0];
            if (Entity.class.isAssignableFrom(type)) {
                return jsonBusinessDataSerializer.serializeEntities((List<Entity>) childEntity, businessDataURIPattern);
            }
        }
        return null;
    }

    @Override
    public BusinessDataQueryResult getJsonQueryEntities(final String entityClassName, final String queryName, final Map<String, Serializable> parameters,
            final Integer startIndex, final Integer maxResults, final String businessDataURIPattern) throws SBusinessDataRepositoryException {
        final Class<? extends Entity> businessDataClass = loadClass(entityClassName);
        BusinessObject businessObject = getBusinessObjectFromClassName(entityClassName);
        final Query queryDefinition = getQueryDefinition(businessObject, entityClassName, queryName);
        final Map<String, Serializable> queryParameters = getQueryParameters(queryDefinition, parameters);
        final List<? extends Serializable> list = businessDataRepository.findListByNamedQuery(getQualifiedQueryName(businessDataClass, queryName),
                getQueryReturnType(queryDefinition, entityClassName), queryParameters, startIndex, maxResults);

        BusinessDataQueryMetadataImpl businessDataQueryMetadata = null;
        final Query countQueryDefinition = getCountQueryDefinition(businessDataClass, businessObject, queryDefinition);
        if (countQueryDefinition != null) {
            try {
                businessDataQueryMetadata = new BusinessDataQueryMetadataImpl(startIndex, maxResults,
                        (Long) businessDataRepository.findByNamedQuery(getQualifiedQueryName(businessDataClass, countQueryDefinition.getName()),
                                getQueryReturnType(countQueryDefinition, entityClassName), queryParameters));
            } catch (NonUniqueResultException e) {
                throw new SBusinessDataRepositoryException("unable to count results for query " + queryName);
            }
        }
        Serializable jsonResults = jsonBusinessDataSerializer.serializeEntities((List<Entity>) list, businessDataURIPattern);
        return new BusinessDataQueryResultImpl(jsonResults, businessDataQueryMetadata);
    }

    private Query getCountQueryDefinition(Class<? extends Entity> businessDataClass, BusinessObject businessObject, Query queryDefinition) {
        final Query countQueryDefinition = countQueryProvider.getCountQueryDefinition(businessObject, queryDefinition);
        if (ensureQueryIsDefinedInEntity(businessDataClass, countQueryDefinition)) {
            return countQueryDefinition;
        }
        return null;
    }

    private boolean ensureQueryIsDefinedInEntity(Class<? extends Entity> businessDataClass, Query countQueryDefinition) {
        final NamedQueries namedQueries = businessDataClass.getAnnotation(NamedQueries.class);
        if (namedQueries == null || countQueryDefinition == null) {
            return false;
        }
        for (NamedQuery namedQuery : namedQueries.value()) {
            if (namedQuery.name().equals(getQualifiedQueryName(businessDataClass, countQueryDefinition.getName()))) {
                return true;
            }
        }
        return false;
    }

    private Class<? extends Serializable> getQueryReturnType(final Query queryDefinition, final String entityClassName)
            throws SBusinessDataRepositoryException {
        if (queryDefinition.hasMultipleResults()) {
            return loadClass(entityClassName);
        }
        try {
            return (Class<? extends Serializable>) Thread.currentThread().getContextClassLoader().loadClass(queryDefinition.getReturnType());
        } catch (final ClassNotFoundException e) {
            throw new SBusinessDataRepositoryException("unable to load class " + queryDefinition.getReturnType());
        }
    }

    private String getQualifiedQueryName(final Class<? extends Entity> businessDataClass, final String queryName) {
        return String.format("%s.%s", businessDataClass.getSimpleName(), queryName);
    }

    private Map<String, Serializable> getQueryParameters(final Query queryDefinition, final Map<String, Serializable> parameters)
            throws SBusinessDataRepositoryException {
        final Set<String> errors = new HashSet<>();
        final Map<String, Serializable> queryParameters = new HashMap<>();
        for (final QueryParameter queryParameter : queryDefinition.getQueryParameters()) {
            if (parameters != null && parameters.containsKey(queryParameter.getName())) {
                queryParameters.put(queryParameter.getName(),
                        convertToType(loadSerializableClass(queryParameter.getClassName()), parameters.get(queryParameter.getName())));
            } else {
                errors.add(queryParameter.getName());
            }
        }
        if (!errors.isEmpty()) {
            final StringBuilder errorMessage = new StringBuilder().append("parameter(s) are missing for query named ").append(queryDefinition.getName())
                    .append(" : ");
            errorMessage.append(StringUtils.join(errors, ","));
            throw new SBusinessDataRepositoryException(errorMessage.toString());
        }
        return queryParameters;
    }

    private Serializable convertToType(final Class<? extends Serializable> clazz, final Serializable parameterValue) {
        return (Serializable) typeConverterUtil.convertToType(clazz, parameterValue);
    }

    private Query getQueryDefinition(BusinessObject businessObject, String className, final String queryName) throws SBusinessDataRepositoryException {
        final List<Query> allQueries = new ArrayList<>();
        allQueries.addAll(businessObject.getQueries());
        allQueries.addAll(BDMQueryUtil.createProvidedQueriesForBusinessObject(businessObject));
        for (final Query query : allQueries) {
            if (query.getName().equals(queryName)) {
                return query;
            }
        }
        throw new SBusinessDataRepositoryException("unable to get query " + queryName + " for business object " + className);
    }

    private BusinessObject getBusinessObjectFromClassName(String className) throws SBusinessDataRepositoryException {
        BusinessObject businessObject = null;
        final BusinessObjectModel businessObjectModel = businessDataModelRepository.getBusinessObjectModel();
        if (businessObjectModel != null) {
            for (final BusinessObject currentBo : businessObjectModel.getBusinessObjects()) {
                if (currentBo.getQualifiedName().equals(className)) {
                    businessObject = currentBo;
                }
            }
        }
        return businessObject;
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends Entity> loadClass(final String returnType) throws SBusinessDataRepositoryException {
        try {
            return (Class<? extends Entity>) Thread.currentThread().getContextClassLoader().loadClass(returnType);
        } catch (final ClassNotFoundException e) {
            throw new SBusinessDataRepositoryException(e);
        }
    }

    protected Class<? extends Serializable> loadSerializableClass(final String className) throws SBusinessDataRepositoryException {
        try {
            return (Class<? extends Serializable>) Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (final ClassNotFoundException e) {
            throw new SBusinessDataRepositoryException("unable to load class " + className);
        }
    }

}
