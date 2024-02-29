/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.transaction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XAResourceRetriever {

    private final Map<String, Optional<Method>> cache = new HashMap<>();

    public Optional<List<XAResource>> retrieveResources(Transaction transaction) {
        return cache.computeIfAbsent(transaction.getClass().getName(), key -> getMethodByReflection(transaction))
                .map(method -> getResources(transaction, method))
                .filter(s -> !s.isEmpty())
                .map(ArrayList::new);
    }

    private Optional<Method> getMethodByReflection(Transaction transaction) {
        try {
            return Optional.of(transaction.getClass().getMethod("getResources"));
        } catch (Exception e) {
            log.warn(
                    "Unable to find method to retrieve resources attached to the current transaction, we will not try to reattempts to find the methode until next restart of the server ",
                    e);
            return Optional.empty();
        }
    }

    protected Set<XAResource> getResources(Transaction transaction, Method getResources) {
        try {
            return ((Map) getResources.invoke(transaction)).keySet();
        } catch (Exception e) {
            log.warn(
                    "Unable to retrieve resources attached to the current transaction, we will not try to reattempts to retrieve those resources until next restart of the server ",
                    e);
            cache.put(transaction.getClass().getName(), Optional.empty());
            return Collections.emptySet();

        }

    }
}
