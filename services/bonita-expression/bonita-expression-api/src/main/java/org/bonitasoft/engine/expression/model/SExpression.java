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
package org.bonitasoft.engine.expression.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author Zhao na
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public interface SExpression extends Serializable {

    String TYPE_CONSTANT = "TYPE_CONSTANT";

    String TYPE_VARIABLE = "TYPE_VARIABLE";

    String TYPE_PATTERN = "TYPE_PATTERN";

    String TYPE_READ_ONLY_SCRIPT = "TYPE_READ_ONLY_SCRIPT";

    String TYPE_READ_WRITE_SCRIPT = "TYPE_READ_WRITE_SCRIPT";

    String TYPE_PARAMETER = "TYPE_PARAMETER";

    String TYPE_I18N = "TYPE_I18N";

    String GROOVY = "GROOVY";

    String JAVASCRIPT = "JAVASCRIPT";

    String TYPE_INPUT = "TYPE_INPUT";

    String TYPE_LIST = "TYPE_LIST";

    String TYPE_CONDITION = "TYPE_CONDITION";

    String getName();

    String getContent();

    String getExpressionType();

    ExpressionKind getExpressionKind();

    String getReturnType();

    String getInterpreter();

    List<SExpression> getDependencies();

    boolean hasDependencies();

    int getDiscriminant();

}
