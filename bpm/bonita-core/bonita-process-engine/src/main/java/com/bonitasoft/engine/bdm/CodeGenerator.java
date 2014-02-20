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
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.engine.commons.StringUtil;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;


/**
 * @author Romain Bioteau
 *
 */
public class CodeGenerator {

	private JCodeModel model ;

	public CodeGenerator(){
		this.model = new JCodeModel();
	}
	
	public void generate(File destDir) throws IOException, JClassAlreadyExistsException{
		model.build(destDir);
	}

	public JDefinedClass addClass(String fullyqualifiedName) throws JClassAlreadyExistsException{
		return model._class(fullyqualifiedName);
	}

	public JFieldVar addField(JDefinedClass definedClass,String fieldName,Class<?> type) throws JClassAlreadyExistsException{
		return definedClass.field(JMod.PRIVATE,type,fieldName);
	}

	public JFieldVar addField(JDefinedClass definedClass, String fieldName,JType type) {
		return definedClass.field(JMod.PRIVATE,type,fieldName);
	}

	public JMethod addSetter(JDefinedClass definedClass,JFieldVar field) {
		JMethod method = definedClass.method(JMod.PUBLIC, Void.TYPE, getSetterName(field));
		method.param(field.type(), field.name());
		method.body().assign(JExpr._this().ref(field.name()), JExpr.ref(field.name()));
		return method;
	}

	public JMethod addGetter(JDefinedClass definedClass,JFieldVar field) {
		JMethod method = definedClass.method(JMod.PUBLIC, field.type(), getGetterName(field));
		JBlock block = method.body();
		block._return(field);
		return method;
	}

	public String getGetterName(JFieldVar field) {
		return "get"+StringUtil.firstCharToUpperCase(field.name());
	}

	public String getSetterName(JFieldVar field) {
		return "set"+StringUtil.firstCharToUpperCase(field.name());
	}


	public JCodeModel getModel() {
		return model;
	}

	public JAnnotationUse addAnnotation(JAnnotatable annotable, Class<? extends Annotation> annotationType) {
		Set<ElementType> supportedElementTypes = getSupportedElementTypes(annotationType);
		checkAnnotationTarget(annotable, annotationType, supportedElementTypes);
		return annotable.annotate(annotationType);
	}

	protected void checkAnnotationTarget(JAnnotatable annotable,
			Class<? extends Annotation> annotationType,
			Set<ElementType> supportedElementTypes) {
		if(annotable instanceof JClass && !supportedElementTypes.isEmpty() && !supportedElementTypes.contains(ElementType.TYPE)){
			throw new IllegalArgumentException(annotationType.getName() + " is not supported for "+annotable);
		}
		if(annotable instanceof JFieldVar && !supportedElementTypes.isEmpty() && !supportedElementTypes.contains(ElementType.FIELD)){
			throw new IllegalArgumentException(annotationType.getName() + " is not supported for "+annotable);
		}
		if(annotable instanceof JMethod && !supportedElementTypes.isEmpty() && !supportedElementTypes.contains(ElementType.METHOD)){
			throw new IllegalArgumentException(annotationType.getName() + " is not supported for "+annotable);
		}
	}

	protected Set<ElementType> getSupportedElementTypes(
			Class<? extends Annotation> annotationType) {
		Set<ElementType> elementTypes = new HashSet<ElementType>();
		Target targetAnnotation = annotationType.getAnnotation(Target.class);
		if(targetAnnotation != null){
			ElementType[] value = targetAnnotation.value();
			if(value != null){
				for(ElementType et : value){
					elementTypes.add(et);
				}
			}
		}
		return elementTypes;
	}

}
