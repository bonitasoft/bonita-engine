/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import java.io.PrintWriter;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Baptiste Mesta
 */
public class TechnicalLoggerPrintWriter extends PrintWriter {

    private final TechnicalLoggerService logger;

    private StringBuilder buffer;

    private final TechnicalLogSeverity level;

    private final Object mutex = new Object();

    public TechnicalLoggerPrintWriter(final TechnicalLoggerService logger, final TechnicalLogSeverity level) {
        super(System.out);
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void println(final Object x) {
        synchronized (mutex) {
            if (buffer != null) {
                logger.log(this.getClass(), level, buffer.toString());
                buffer = null;
            }
        }
        logger.log(this.getClass(), level, String.valueOf(x));
    }

    @Override
    public void print(final Object obj) {
        synchronized (mutex) {
            if (buffer == null) {
                buffer = new StringBuilder();
            }
            buffer.append(obj);
        }
    }

}
