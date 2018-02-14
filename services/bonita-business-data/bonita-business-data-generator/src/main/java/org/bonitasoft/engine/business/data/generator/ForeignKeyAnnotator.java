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
package org.bonitasoft.engine.business.data.generator;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.hibernate.annotations.ForeignKey;

/**
 * @author Laurent Leseigneur
 */
public class ForeignKeyAnnotator {

    private final CodeGenerator codeGenerator;

    public ForeignKeyAnnotator(CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    public void annotateForeignKeyName(JDefinedClass entityClass, JFieldVar jFieldVar, RelationField relationField) {

        final JAnnotationUse foreignKey = codeGenerator.addAnnotation(jFieldVar, ForeignKey.class);

        StringBuilder uniqueForeignKeyName = new StringBuilder()
                .append("FK_")
                .append(getReducedUniqueName(entityClass, jFieldVar, relationField));
        foreignKey.param("name", uniqueForeignKeyName.toString());

    }

    protected int getReducedUniqueName(JDefinedClass entityClass, JFieldVar jFieldVar, RelationField relationField) {
        final StringBuilder builder = new StringBuilder();
        builder.append(entityClass.name());
        builder.append("_");
        builder.append(jFieldVar.name());
        builder.append("_");
        builder.append(relationField.getReference().getSimpleName());
        return Math.abs(builder.toString().hashCode());
    }

}
