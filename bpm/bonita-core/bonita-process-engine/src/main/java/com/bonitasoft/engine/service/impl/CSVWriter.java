/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class CSVWriter {

    private final PrintWriter writer;

    private final Writer baseWriter;

    public static final char ESCAPE_CHARACTER = '"';

    public static final char SEPARATOR = ',';

    public static final char NO_QUOTE_CHARACTER = '\u0000';

    public static final String LINE_END = "\n";

    public CSVWriter(final Writer writer, final String[] ColumnNames) {
        this.baseWriter = writer;
        this.writer = new PrintWriter(writer);
        writeNext(ColumnNames);
    }

    public void writeNext(final String[] nextLine) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nextLine.length; i++) {

            if (i != 0) {
                sb.append(SEPARATOR);
            }

            String nextElement = nextLine[i];
            if (nextElement == null) {
                continue;
            }
            for (int j = 0; j < nextElement.length(); j++) {
                char nextChar = nextElement.charAt(j);
                sb.append(nextChar);
            }
        }
        sb.append(LINE_END);
        writer.write(sb.toString());
        writer.flush();

    }

    public void close() throws IOException {
        writer.flush();
        writer.close();
        baseWriter.close();
    }

}
