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

public class DefaultTransition extends Transition {

    private ConditionalBranch branch;

    @Override
    public void bind(String source, String target, ProcessDefinitionBuilder builder) {
        builder.addDefaultTransition(source, target);
        if(branch != null) {
            branch.build(source, builder);
        }
    }

    public interface ConditionalBranch {

        void build(String source, ProcessDefinitionBuilder builder);

        DefaultTransition goingTo(Fragment then);
    }

    public ConditionalBranch toMeet(final Expression expression) {
        branch = new ConditionalBranch() {

            private Fragment then;

            public void build(String source, ProcessDefinitionBuilder builder) {
                then.build(builder);
                then.bind(source, new ConditionalTransition(expression), builder);
            }

            public DefaultTransition goingTo(Fragment then) {
                this.then = then;
                return DefaultTransition.this;
            }
        };
        return branch;
    }
}
