/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import javax.persistence.ForeignKey;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import org.bonitasoft.engine.bdm.model.field.RelationField;

/**
 * @author Laurent Leseigneur
 */
public class ForeignKeyAnnotator {

    public void annotateForeignKeyName(JAnnotationUse joinColumn, JDefinedClass entityClass, JFieldVar jFieldVar,
            RelationField relationField) {
        JAnnotationUse foreignKey = joinColumn.annotationParam("foreignKey", ForeignKey.class);

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
