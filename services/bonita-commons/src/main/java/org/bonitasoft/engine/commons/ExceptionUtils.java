/**
 * Copyright (C) 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.commons;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class ExceptionUtils {

    public static String printLightWeightStacktrace(Throwable exception) {
        return printLightWeightStacktrace(exception, 5);
    }

    public static String printLightWeightStacktrace(Throwable exception, int numberOfFrames) {
        List<Throwable> throwableList = org.apache.commons.lang3.exception.ExceptionUtils.getThrowableList(exception);
        if (throwableList.isEmpty()) {
            return null;
        }
        Collections.reverse(throwableList);
        Throwable rootCause = throwableList.get(0);
        return throwableList.stream().map(ExceptionUtils::print).collect(Collectors.joining("\n\twrapped by "))
                + "\n exception was generated here:" + getStacktrace(rootCause, numberOfFrames);
    }

    private static String getStacktrace(Throwable t, int numberOfFrames) {
        String[] stackFrames = org.apache.commons.lang3.exception.ExceptionUtils.getStackFrames(t);
        return Arrays.stream(stackFrames).skip(1).limit(numberOfFrames).collect(Collectors.joining("\n"));
    }

    protected static String print(Throwable e) {
        return e.getClass().getName() + ": " + StringUtils.defaultString(e.getMessage());
    }

    public static String printRootCauseOnly(Throwable exception) {
        return print(org.apache.commons.lang3.exception.ExceptionUtils.getRootCause(exception));
    }
}
