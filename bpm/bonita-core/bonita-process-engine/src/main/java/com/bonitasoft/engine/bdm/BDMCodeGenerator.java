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

import java.util.Date;

import javax.persistence.Entity;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;


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

	protected JCodeModel buildASTFromBom() throws JClassAlreadyExistsException{
		for(BusinessObject bo : bom.getEntities()){
			addEntity(bo);
		}
		return getModel();
	}

	protected void addEntity(BusinessObject bo) throws JClassAlreadyExistsException {
		JDefinedClass entityClass = addClass(bo.getQualifiedName());
		JAnnotationUse entityAnnotation = addAnnotation(entityClass, Entity.class);
		entityAnnotation.param("name", entityClass.name());
		if(bo.getFields() != null){
			for(Field field : bo.getFields()){
				addFieldAndAccessors(entityClass,field);
			}
		}
	}

	protected void addFieldAndAccessors(JDefinedClass entityClass, Field field) throws JClassAlreadyExistsException {
		JFieldVar fieldVar = addField(entityClass, field.getName(), toJavaType(field.getType()));
		addSetter(entityClass, fieldVar);
		addGetter(entityClass, fieldVar);
	}

	private Class<?> toJavaType(FieldType type) {
		switch (type) {
		case STRING:return String.class;
		case INTEGER:return Integer.class;
		case FLOAT:return Integer.class;
		case LONG:return Integer.class;
		case DOUBLE:return Double.class;
		case BOOLEAN:return Boolean.class;
		case DATE:return Date.class;
		default:throw new IllegalStateException(type.name() + " is not mapped to a java type");
		}
	}

}
