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
package org.bonitasoft.engine.scheduler.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.scheduler.AbstractBonitaJobListener;
import org.bonitasoft.engine.scheduler.AbstractBonitaPlatformJobListener;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Celine Souchet
 */
public class PlatformQuartzJobListener extends AbstractQuartzJobListener {

    private final List<AbstractBonitaPlatformJobListener> bonitaJobListeners;

    public PlatformQuartzJobListener(final List<AbstractBonitaPlatformJobListener> jobListeners) {
        bonitaJobListeners = jobListeners;
    }

    @Override
    public String getName() {
        return "PlatformQuartzJobListener";
    }

    @Override
    public void jobToBeExecuted(final JobExecutionContext context) {
        final Map<String, Serializable> mapContext = buildMapContext(context);

        for (final AbstractBonitaJobListener abstractBonitaJobListener : bonitaJobListeners) {
            abstractBonitaJobListener.jobToBeExecuted(mapContext);
        }
    }

    @Override
    public void jobExecutionVetoed(final JobExecutionContext context) {
        final Map<String, Serializable> mapContext = buildMapContext(context);

        for (final AbstractBonitaJobListener abstractBonitaJobListener : bonitaJobListeners) {
            abstractBonitaJobListener.jobExecutionVetoed(mapContext);
        }
    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
        final Map<String, Serializable> mapContext = buildMapContext(context);

        for (final AbstractBonitaJobListener abstractBonitaJobListener : bonitaJobListeners) {
            abstractBonitaJobListener.jobWasExecuted(mapContext, jobException);
        }
    }

}
