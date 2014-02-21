/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.bdm;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JType;


/**
 * @author Romain Bioteau
 *
 */
public class BDMCodeGenerator extends CodeGenerator{

	private BusinessObjectModel bom;

	public BDMCodeGenerator(BusinessObjectModel bom){
		super();
		if(bom == null){
			throw new IllegalArgumentException("bom is null");
		}
		this.bom = bom;
	}

	protected void buildASTFromBom() throws JClassAlreadyExistsException{
		for(BusinessObject bo : bom.getEntities()){
			addEntity(bo);
		}
	}

	protected void addEntity(BusinessObject bo) throws JClassAlreadyExistsException {
		String qualifiedName = bo.getQualifiedName();
		validateClassNotExistsInRuntime(qualifiedName);
		
		JDefinedClass entityClass = addClass(qualifiedName);
		entityClass = addInterface(entityClass, Serializable.class.getName());
		entityClass = addInterface(entityClass, com.bonitasoft.engine.bdm.Entity.class.getName());
		
		JAnnotationUse entityAnnotation = addAnnotation(entityClass, Entity.class);
		entityAnnotation.param("name", entityClass.name());
		
		addPersistenceIdFieldAndAccessors(entityClass);
		addPersistenceVersionFieldAndAccessors(entityClass);
		
		if(bo.getFields() != null){
			for(Field field : bo.getFields()){
				JFieldVar basicField = addBasicField(entityClass, field);
				addAccessors(entityClass,basicField);
			}
		}
		
		addEqualsMethod(entityClass);
		addHashCodeMethod(entityClass);
	}

	private void validateClassNotExistsInRuntime(String qualifiedName) {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		boolean alreadyInRuntime = true;
		try {
			contextClassLoader.loadClass(qualifiedName);
		} catch (ClassNotFoundException e) {
			alreadyInRuntime = false;
		}
		if(alreadyInRuntime){
			throw new IllegalArgumentException("Class "+qualifiedName+" already exists in target runtime environment.");
		}
	}

	protected void addPersistenceIdFieldAndAccessors(JDefinedClass entityClass)
			throws JClassAlreadyExistsException {
		JFieldVar idFieldVar = addField(entityClass, Field.PERSISTENCE_ID, toJavaType(FieldType.LONG));
		addAnnotation(idFieldVar, Id.class);
		addAnnotation(idFieldVar, GeneratedValue.class);
		addAccessors(entityClass,idFieldVar);
	}
	
	protected void addPersistenceVersionFieldAndAccessors(JDefinedClass entityClass)
			throws JClassAlreadyExistsException {
		JFieldVar versionField = addField(entityClass, Field.PERSISTENCE_VERSION, toJavaType(FieldType.LONG));
		addAnnotation(versionField, Version.class);
		addAccessors(entityClass,versionField);
	}

	protected JFieldVar addBasicField(JDefinedClass entityClass, Field field) throws JClassAlreadyExistsException {
		JFieldVar fieldVar = addField(entityClass, field.getName(), toJavaType(field.getType()));
		addAnnotation(fieldVar, Basic.class);
		if(field.getType() == FieldType.DATE){
			JAnnotationUse temporalAnnotation = addAnnotation(fieldVar,Temporal.class);
			temporalAnnotation.param("value", TemporalType.TIMESTAMP);
		}
		return fieldVar;
	}
	
	protected void addAccessors(JDefinedClass entityClass, JFieldVar fieldVar) throws JClassAlreadyExistsException {
		addSetter(entityClass, fieldVar);
		addGetter(entityClass, fieldVar);
	}

	protected JType toJavaType(FieldType type) {
		return getModel().ref(type.getClazz());
	}
	
	@Override
	public void generate(File destDir) throws IOException,
			JClassAlreadyExistsException {
		buildASTFromBom();
		super.generate(destDir);
	}

}
