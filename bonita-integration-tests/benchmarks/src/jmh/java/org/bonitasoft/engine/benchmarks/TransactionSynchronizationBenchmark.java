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
package org.bonitasoft.engine.benchmarks;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;
import org.bonitasoft.engine.test.TestEngine;
import org.bonitasoft.engine.test.TestEngineImpl;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.WorkDescriptor;
import org.bonitasoft.engine.work.WorkService;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class TransactionSynchronizationBenchmark {

    private TestEngine engine;
    private UserTransactionService userTransactionService;
    private WorkService workService;

    @Setup
    public void setup() throws Exception {
        engine = TestEngineImpl.getInstance();
        engine.start();
        final ServiceAccessor serviceAccessor = ServiceAccessorSingleton.getInstance();
        userTransactionService = serviceAccessor.getUserTransactionService();
        workService = serviceAccessor.getWorkService();
        BPMWorkFactory bpmWorkFactory = serviceAccessor.getBPMWorkFactory();
        bpmWorkFactory.addExtension("BENCHMARK_WORK", workDescriptor -> new BonitaWork() {

            @Override
            public String getDescription() {
                return "BENCHMARK_WORK";
            }

            @Override
            public CompletableFuture<Void> work(Map<String, Object> context) throws Exception {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void handleFailure(Throwable e, Map<String, Object> context) throws Exception {

            }
        });
    }

    @TearDown
    public void tearDown() throws Exception {
        engine.stop();
    }

    @Benchmark
    public void register1000Works() throws Exception {
        userTransactionService.executeInTransaction(() -> {
            for (int i = 0; i < 1000; i++) {
                workService.registerWork(WorkDescriptor.create("BENCHMARK_WORK"));
            }
            return null;
        });
    }

    @Benchmark
    public void register1Works() throws Exception {
        userTransactionService.executeInTransaction(() -> {
            for (int i = 0; i < 1; i++) {
                workService.registerWork(WorkDescriptor.create("BENCHMARK_WORK"));
            }
            return null;
        });
    }

    @Benchmark
    public void register2Works() throws Exception {
        userTransactionService.executeInTransaction(() -> {
            for (int i = 0; i < 2; i++) {
                workService.registerWork(WorkDescriptor.create("BENCHMARK_WORK"));
            }
            return null;
        });
    }
}
