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
 * @author Matthieu Chaffotte
 */
public class EqualsBuilder {

    public JMethod generate(final JDefinedClass definedClass) {
        final JMethod equalsMethod = definedClass.method(JMod.PUBLIC, boolean.class, "equals");
        final JVar param = equalsMethod.param(definedClass.owner().ref(Object.class), "obj");

        final JBlock body = equalsMethod.body();
        body._if(JExpr._this().eq(param))._then()._return(JExpr.TRUE);
        body._if(param.eq(JExpr._null()))._then()._return(JExpr.FALSE);
        body._if(JExpr.invoke("getClass").ne(param.invoke("getClass")))._then()._return(JExpr.FALSE);
        final JVar other = body.decl(definedClass, "other", JExpr.cast(definedClass, param));

        for (final Entry<String, JFieldVar> field : definedClass.fields().entrySet()) {
            final JFieldVar fieldVar = field.getValue();
            final JType type = fieldVar.type();
            if (type.isPrimitive()) {
                body._if(JExpr.ref(fieldVar.name()).ne(other.ref(fieldVar.name())))._then()._return(JExpr.FALSE);
            } else {
                final JConditional ifRefIsNull = body._if(JExpr.ref(fieldVar.name()).eq(JExpr._null()));
                ifRefIsNull._then()._if(other.ref(fieldVar.name()).ne(JExpr._null()))._then()._return(JExpr.FALSE);
                ifRefIsNull._elseif(JExpr.ref(fieldVar.name()).invoke("equals").arg(other.ref(fieldVar.name())).not())._then()._return(JExpr.FALSE);
            }
        }

        body._return(JExpr.TRUE);
        return equalsMethod;
    }

}
