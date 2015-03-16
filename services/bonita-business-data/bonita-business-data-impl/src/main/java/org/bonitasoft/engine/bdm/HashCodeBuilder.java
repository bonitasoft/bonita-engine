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
package org.bonitasoft.engine.bdm;

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
 * @author Matthieu Chaffotte
 */
public class HashCodeBuilder {

    public JMethod generate(final JDefinedClass definedClass) {
        final JMethod hashCodeMethod = definedClass.method(JMod.PUBLIC, int.class, "hashCode");
        final JBlock body = hashCodeMethod.body();
        final JVar prime = body.decl(JMod.FINAL, JType.parse(definedClass.owner(), int.class.getName()), "prime", JExpr.lit(31));
        final JVar result = body.decl(JType.parse(definedClass.owner(), int.class.getName()), "result", JExpr.lit(1));
        for (final Entry<String, JFieldVar> field : definedClass.fields().entrySet()) {
            final JFieldVar fieldVar = field.getValue();
            final JType type = fieldVar.type();
            if (!type.isPrimitive()) {
                final JVar refHashCode = body.decl(JType.parse(definedClass.owner(), int.class.getName()), fieldVar.name() + "Code", JExpr.lit(0));
                body._if(fieldVar.ne(JExpr._null()))._then().assign(refHashCode, fieldVar.invoke("hashCode"));
                body.assign(result, prime.mul(result).plus(refHashCode));
            } else {
                if (type.name().equals(boolean.class.getSimpleName())) {
                    final JVar refHashCode = body.decl(JType.parse(definedClass.owner(), int.class.getName()), fieldVar.name() + "Code", JExpr.lit(1237));
                    body._if(fieldVar)._then().assign(refHashCode, JExpr.lit(1231));
                    body.assign(result, prime.mul(result).plus(refHashCode));
                } else if (type.name().equals(long.class.getSimpleName())) {
                    body.assign(
                            result,
                            prime.mul(result).plus(
                                    JExpr.cast(JType.parse(definedClass.owner(), int.class.getName()),
                                            JExpr.direct(fieldVar.name() + " ^ (" + fieldVar.name() + " >>> 32)"))));
                } else if (type.name().equals(double.class.getSimpleName())) {
                    final JClass doubleType = definedClass.owner().ref(Double.class.getName());
                    final JVar temp = body.decl(JType.parse(definedClass.owner(), long.class.getName()), fieldVar.name() + "temp",
                            doubleType.staticInvoke("doubleToLongBits").arg(fieldVar));
                    body.assign(
                            result,
                            prime.mul(result).plus(
                                    JExpr.cast(JType.parse(definedClass.owner(), int.class.getName()),
                                            JExpr.direct(temp.name() + " ^ (" + temp.name() + " >>> 32)"))));
                } else if (type.name().equals(float.class.getSimpleName())) {
                    final JClass floatType = definedClass.owner().ref(Float.class.getName());
                    body.assign(result, prime.mul(result).plus(floatType.staticInvoke("floatToIntBits").arg(fieldVar)));
                }
            }
        }

        body._return(result);
        return hashCodeMethod;
    }

}
