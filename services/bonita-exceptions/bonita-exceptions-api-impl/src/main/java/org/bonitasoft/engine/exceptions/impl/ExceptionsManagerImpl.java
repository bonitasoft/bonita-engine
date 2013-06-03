/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.exceptions.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exceptions.ExceptionsManager;

/**
 * the causes.properties entries should have this format:
 * <exceptionId>_<cause_number>=My message with {0} parameters {1} cause_number
 * <b>MUST</b> start with 0 and <b>MUST</b> not have empty gap (e.g. only have
 * cause 0 and 2 but not cause 1)
 * 
 * @author Baptiste Mesta
 */
public class ExceptionsManagerImpl implements ExceptionsManager {

    private static final String CAUSES_FILE = "causes";

    private final ResourceBundle bundle;

    private final Map<String, Integer> numberOfPossibleCause;

    public ExceptionsManagerImpl() {
        bundle = ResourceBundle.getBundle(this.getClass().getPackage().getName() + "." + CAUSES_FILE);
        numberOfPossibleCause = new HashMap<String, Integer>();
        final Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            final String key = keys.nextElement();
            final String[] split = key.split("_");// exception id should not contains
            // '_'
            final String eId = split[0];
            final int causeNb = Integer.valueOf(split[1]);
            if (numberOfPossibleCause.containsKey(eId)) {
                final Integer currentMax = numberOfPossibleCause.get(eId);
                if (causeNb > currentMax) {
                    numberOfPossibleCause.put(eId, causeNb);
                }
            } else {
                numberOfPossibleCause.put(eId, causeNb);
            }
        }
    }

    /**
     * @return the list of cause with all parameters replaced by '?'
     */
    @Override
    public List<String> getPossibleCauses(final String exceptionId) {
        final Integer numberOfCausesForThisException = numberOfPossibleCause.get(exceptionId);
        if (numberOfCausesForThisException != null) {
            final List<String> causes = new ArrayList<String>(numberOfCausesForThisException);
            String string;
            for (int i = 0; i <= numberOfCausesForThisException; i++) {
                string = bundle.getString(exceptionId + "_" + i);
                string = string.replaceAll("\\{[0-9]+\\}", "?");
                causes.add(string);
            }
            return causes;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * @param e
     *            the exception on which we want causes
     * @return causes of the exception
     */
    @Override
    public List<String> getPossibleCauses(final SBonitaException e) {
        NullCheckingUtil.checkArgsNotNull(e);
        if (e.getParameters() == null) {
            return getPossibleCauses(e.getExceptionId());
        } else {
            return getPossibleCauses(e.getExceptionId(), e.getParameters());
        }
    }

    /**
     * @param exceptionId
     *            the exceptionId of the causes
     * @param parameters
     *            parameters that will be injected in the causes
     * @return causes of the exception
     */
    @Override
    public List<String> getPossibleCauses(final String exceptionId, final Object... parameters) {
        NullCheckingUtil.checkArgsNotNull(parameters);
        final Integer numberOfCausesForThisException = numberOfPossibleCause.get(exceptionId);
        if (numberOfCausesForThisException != null) {
            final List<String> causes = new ArrayList<String>(numberOfCausesForThisException);
            for (int i = 0; i <= numberOfPossibleCause.get(exceptionId); i++) {
                String string = bundle.getString(exceptionId + "_" + i);
                string = MessageFormat.format(string, parameters);
                causes.add(string);
            }
            return causes;
        } else {
            return Collections.emptyList();
        }
    }

}
