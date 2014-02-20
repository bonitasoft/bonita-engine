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

import java.util.Map.Entry;

import com.sun.codemodel.JAssignmentTarget;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * @author Romain Bioteau
 *
 */
public class EqualsBuilder {
	
	
	public JMethod generate(JDefinedClass definedClass){
		JMethod equalsMethod = definedClass.method(JMod.PUBLIC, boolean.class, "equals");
		JVar param = equalsMethod.param(definedClass.owner().ref(Object.class), "obj");
		JBlock body = equalsMethod.body();
		
		body._if(JExpr._this().eq(param))._then()._return(JExpr.TRUE);
		body._if(param.eq(JExpr._null()))._then()._return(JExpr.FALSE);
		body._if(JExpr.invoke("getClass").ne(param.invoke("getClass")))._then()._return(JExpr.FALSE);
		JVar other = body.decl(definedClass, "other", JExpr.cast(definedClass, param));
	
		for(Entry<String, JFieldVar> field : definedClass.fields().entrySet()){
			JFieldVar fieldVar = field.getValue();
			JType type = fieldVar.type();
			if(type.unboxify().isPrimitive()){
				body._if(JExpr.ref(fieldVar.name()).ne(other.ref(fieldVar.name())))._then()._return(JExpr.FALSE);
			}else{
				JConditional ifRefIsNull = body._if(JExpr.ref(fieldVar.name()).eq(JExpr._null()));
				ifRefIsNull._then()._if(other.ref(fieldVar.name()).ne(JExpr._null()))._then()._return(JExpr.FALSE);
				ifRefIsNull._elseif(JExpr.ref(fieldVar.name()).invoke("equals").arg(other.ref(fieldVar.name())).not())._then()._return(JExpr.FALSE);
			}
		}
		
		body._return(JExpr.TRUE);
		return equalsMethod;
	}

//	 /**
//		 * A equals method which compare object fields.
//	 	 * @generated
//		 */
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			«this.name» other = («this.name») obj;
//			«FOREACH this.EStructuralFeatureModelGenAnnotations AS featureAnnotation-»
//				«IF !featureAnnotation.reference && featureAnnotation.primitive »
//					if («featureAnnotation.name» != other.«featureAnnotation.name»)
//						return false; 
//				«ELSE-»
//					if («featureAnnotation.name» == null) {
//						if (other.«featureAnnotation.name» != null) 
//							return false;
//					} else if (!«featureAnnotation.name».equals(other.«featureAnnotation.name»))
//							return false;
//				«ENDIF-»
//		 	«ENDFOREACH-»
//			return true;
//		}
	
}
