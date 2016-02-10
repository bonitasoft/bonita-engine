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
package org.bonitasoft.engine.connectors;

import org.bonitasoft.engine.connector.AbstractConnector;

/**
 * @author Sebastien Chevassu
 * @author Celine Souchet
 */
public class TestConnector3 extends AbstractConnector {

    public static final String INPUT1 = "input1";

    public static final String INPUT2 = "input2";

    public static final String INPUT3 = "input3";

    public static final String INPUT4 = "input4";

    @Override
    public void validateInputParameters() {

    }

    @Override
    protected void executeBusinessLogic() {

        final String input1Value = (String) getInputParameter(INPUT1);
        if (input1Value != null) {
            VariableStorage.getInstance().setVariable(INPUT1, input1Value);
        }

        final String input2Value = (String) getInputParameter(INPUT2);
        if (input2Value != null) {
            VariableStorage.getInstance().setVariable(INPUT2, input2Value);
        }

        final String input3Value = (String) getInputParameter(INPUT3);
        if (input3Value != null) {
            VariableStorage.getInstance().setVariable(INPUT3, input3Value);
        }

        final String input4Value = (String) getInputParameter(INPUT4);
        if (input4Value != null) {
            VariableStorage.getInstance().setVariable(INPUT4, input4Value);
        }
    }

}
