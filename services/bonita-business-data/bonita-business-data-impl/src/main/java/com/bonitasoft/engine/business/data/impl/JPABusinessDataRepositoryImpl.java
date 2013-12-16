/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.business.data.BusinessDataNotFoundException;
import com.bonitasoft.engine.business.data.BusinessDataRespository;
import com.bonitasoft.engine.business.data.NonUniqueResultException;

/**
 * @author Matthieu Chaffotte
 */
public class JPABusinessDataRepositoryImpl implements BusinessDataRespository {

	private EntityManagerFactory entityManagerFactory;

	@Override
	public void deploy(final byte[] bdrArchive) {
		transformBDRArchive(bdrArchive);

	}

	protected void transformBDRArchive(final byte[] bdrArchive) {
		List<String> classNameList = null;
		try {
			classNameList = IOUtil.getClassNameList(bdrArchive);
		} catch (IOException e) {
			throw new SBonitaRuntimeException(e);
		}

		if (classNameList == null || classNameList.isEmpty()) {
			throw new IllegalStateException("No entity found in bdr archive");
		}
		byte[] persistenceFileContent = null;
		try {
			persistenceFileContent = getPersistenceFileContentFor(classNameList);
		} catch (ParserConfigurationException e) {
			throw new SBonitaRuntimeException(e);
		} catch (SAXException e) {
			throw new SBonitaRuntimeException(e);
		} catch (IOException e) {
			throw new SBonitaRuntimeException(e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new SBonitaRuntimeException(e);
		} catch (TransformerException e) {
			throw new SBonitaRuntimeException(e);
		}
	}

	protected byte[] getPersistenceFileContentFor(final List<String> classNames) throws IOException, TransformerException, ParserConfigurationException, SAXException {
		PersistenceUnitBuilder builder = new PersistenceUnitBuilder();
		for(String classname : classNames){
			builder.addClass(classname);
		}
		return IOUtil.toByteArray(builder.done());
	}

	@Override
	public void start() {
		final Map<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put("hibernate.ejb.resource_scanner", InactiveScanner.class.getName());
		entityManagerFactory = Persistence.createEntityManagerFactory("BDR", configOverrides);
	}

	@Override
	public void stop() {
		if (entityManagerFactory != null) {
			entityManagerFactory.close();
			entityManagerFactory = null;
		}
	}

	@Override
	public <T> T find(final Class<T> entityClass, final Serializable primaryKey) throws BusinessDataNotFoundException {
		final EntityManager em = getEntityManager();
		final T entity = em.find(entityClass, primaryKey);
		if (entity == null) {
			throw new BusinessDataNotFoundException("Impossible to get data with id: " + primaryKey);
		}
		return entity;
	}

	@Override
	public <T> T find(final Class<T> resultClass, final String qlString, final Map<String, Object> parameters) throws BusinessDataNotFoundException,
	NonUniqueResultException {
		final EntityManager em = getEntityManager();
		final TypedQuery<T> query = em.createQuery(qlString, resultClass);
		if (parameters != null) {
			for (final Entry<String, Object> parameter : parameters.entrySet()) {
				query.setParameter(parameter.getKey(), parameter.getValue());
			}
		}
		try {
			return query.getSingleResult();
		} catch (final javax.persistence.NonUniqueResultException nure) {
			throw new NonUniqueResultException(nure);
		} catch (final NoResultException nre) {
			throw new BusinessDataNotFoundException("Impossible to get data using query: " + qlString + " and parameters: " + parameters, nre);
		}
	}

	private EntityManager getEntityManager() {
		if (entityManagerFactory == null) {
			throw new IllegalStateException("The BDR is not started");
		}
		final EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.joinTransaction();
		return entityManager;
	}

	@Override
	public void persist(final Object entity) {
		if (entity == null) {
			return;
		}
		final EntityManager em = getEntityManager();
		em.persist(entity);
	}

}
