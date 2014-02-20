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

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
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
public class HashCodeBuilder {

	public JMethod generate(JDefinedClass definedClass) {
		JMethod hashCodeMethod = definedClass.method(JMod.PUBLIC, int.class, "hashCode");
		JBlock body = hashCodeMethod.body();
		JVar prime = body.decl(JMod.FINAL,JType.parse(definedClass.owner(), int.class.getName()), "prime", JExpr.lit(31));
		JVar result = body.decl(JType.parse(definedClass.owner(), int.class.getName()), "result", JExpr.lit(1));
		for(Entry<String, JFieldVar> field : definedClass.fields().entrySet()){
			JFieldVar fieldVar = field.getValue();
			JType type = fieldVar.type();
			JType unboxifyType = type.unboxify();
			if(!unboxifyType.isPrimitive()){
				JVar refHashCode = body.decl(JType.parse(definedClass.owner(), int.class.getName()), "refHashCode",JExpr.lit(0));
				body._if(fieldVar.ne(JExpr._null()))._then().assign(refHashCode, fieldVar.invoke("hashCode"));
				body.assign(result, prime.mul(result).plus(refHashCode));
			}else{
				if(unboxifyType.name().equals(boolean.class.getSimpleName())){
					JVar refHashCode = body.decl(JType.parse(definedClass.owner(), int.class.getName()), "refHashCode",JExpr.lit(1237));
					body._if(fieldVar)._then().assign(refHashCode, JExpr.lit(1231));
					body.assign(result, prime.mul(result).plus(refHashCode));
				}else if(unboxifyType.name().equals(long.class.getSimpleName())){
					body.assign(result, prime.mul(result).plus(JExpr.cast(JType.parse(definedClass.owner(), int.class.getName()), JExpr.direct(fieldVar.name()+" ^ ("+fieldVar.name()+" >>> 32)"))));
				}else if(unboxifyType.name().equals(double.class.getSimpleName())){
					JClass doubleType = definedClass.owner().ref(Double.class.getName());
					JVar temp = body.decl(JType.parse(definedClass.owner(), long.class.getName()), "temp",doubleType.staticInvoke("doubleToLongBits").arg(fieldVar));
					body.assign(result, prime.mul(result).plus(JExpr.cast(JType.parse(definedClass.owner(), int.class.getName()), JExpr.direct(temp.name()+" ^ ("+temp.name()+" >>> 32)"))));
				}else if(unboxifyType.name().equals(float.class.getSimpleName())){
					JClass floatType = definedClass.owner().ref(Float.class.getName());
					body.assign(result, prime.mul(result).plus(floatType.staticInvoke("floatToIntBits").arg(fieldVar)));
				}
			}
		}

		body._return(result);
		return hashCodeMethod;
	}

	
}
