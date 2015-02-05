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
package org.bonitasoft.engine.command.helper.designer;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;

/**
 * Created by vince on 1/28/14.
 */
public class ConditionalTransition extends Transition {

    private Fragment otherwise;

    private Expression expression;

    public ConditionalTransition(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void bind(String source, String target, ProcessDefinitionBuilder builder) {
        builder.addTransition(source, target, expression);

        if(otherwise != null) {
            otherwise.build(builder);
            otherwise.bind(source, new DefaultTransition(), builder);
        }
    }

    public ConditionalTransition otherwise(Fragment otherwise) {
        this.otherwise = otherwise;
        return this;
    }
}
