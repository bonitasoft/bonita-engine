/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.log.asyncflush;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.scheduler.exception.SJobConfigurationException;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class QueriableLogRecordJob extends AbstractJob {

    private static final long serialVersionUID = -5222364133911713673L;

    private static final Logger LOGGER = LoggerFactory.getLogger(QueriableLogRecordJob.class);

    private List<SQueriableLog> logs;

    private transient QueriableLoggerService queriableLogService;

    @Override
    public void execute() throws SJobExecutionException {
        if (queriableLogService == null) {
            throw new SJobExecutionException("Missing mandatory service: QueriableLoggerService");
        }
        LOGGER.debug("QueriableLogRecordJob calling logger service");
        final SQueriableLog[] logsArray = new SQueriableLog[logs.size()];
        queriableLogService.log(this.getClass().getName(), "execute", logs.toArray(logsArray));
        LOGGER.debug("QueriableLogRecordJob has called logger service");
    }

    @Override
    public String getDescription() {
        return "Persist one or more logs when a recording is triggered";
    }

    public void setQueriableLoggerService(final QueriableLoggerService queriableLoggerService) {
        queriableLogService = queriableLoggerService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        super.setAttributes(attributes);
        NullCheckingUtil.checkArgsNotNull(attributes.get("logs"));
        logs = (List<SQueriableLog>) attributes.get("logs");
    }

}
