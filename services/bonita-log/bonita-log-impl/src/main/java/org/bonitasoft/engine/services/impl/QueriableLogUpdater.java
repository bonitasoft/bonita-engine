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
package org.bonitasoft.engine.services.impl;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogBuilderFactory;
import org.bonitasoft.engine.services.QueriableLogSessionProvider;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class QueriableLogUpdater {


    /**
     *
     */
    private static final int MAX_MESSAGE_LENGTH = 255;
    private final QueriableLogSessionProvider sessionProvider;
    private final PlatformService platformService;
    private final TechnicalLoggerService logger;

    public QueriableLogUpdater(final QueriableLogSessionProvider sessionProvider, final PlatformService platformService, final TechnicalLoggerService logger) {
        this.sessionProvider = sessionProvider;
        this.platformService = platformService;
        this.logger = logger;
    }

    public SQueriableLog buildFinalLog(final String callerClassName, final String callerMethodName, final SQueriableLog log) {
        final SQueriableLogBuilderFactory fact = BuilderFactory.get(SQueriableLogBuilderFactory.class);
        final SQueriableLogBuilder builder = fact.fromInstance(log);

        final String rawMessage = log.getRawMessage();
        if (rawMessage.length() > MAX_MESSAGE_LENGTH) {
            final String truncatedMessage = rawMessage.substring(0, MAX_MESSAGE_LENGTH);
            builder.rawMessage(truncatedMessage);
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
                final StringBuilder stb = new StringBuilder();
                stb.append("The queriable log message is too long and will be truncated to ");
                stb.append(MAX_MESSAGE_LENGTH);
                stb.append(" characters. The original message is '");
                stb.append(rawMessage);
                stb.append("'. It will be truncated to '");
                stb.append(truncatedMessage);
                stb.append("'");
                logger.log(getClass(), TechnicalLogSeverity.INFO, stb.toString());
            }
        }
        final SQueriableLog finalLog = builder.callerClassName(callerClassName).callerMethodName(callerMethodName)
                .userId(sessionProvider.getUserId()).clusterNode(sessionProvider.getClusterNode())
                .productVersion(platformService.getSPlatformProperties().getPlatformVersion())
                .done();
        return finalLog;

    }

}
