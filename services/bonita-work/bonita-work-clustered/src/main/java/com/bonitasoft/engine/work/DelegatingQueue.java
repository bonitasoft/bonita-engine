/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.work;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ItemListener;
import com.hazelcast.monitor.LocalQueueStats;

/**
 * 
 * Delegate call to an IQueue
 * it's only purpose is to avoid a lot of stack traces when killing the engine
 * It thorw interrupted exceptions when the hazelcast instance is not active anymore
 * 
 * @author Baptiste Mesta
 * 
 */
public class DelegatingQueue implements BlockingQueue<Runnable> {

    private final IQueue<Runnable> queue;

    public Object getId() {
        return queue.getId();
    }

    public String addItemListener(final ItemListener<Runnable> listener, final boolean includeValue) {
        return queue.addItemListener(listener, includeValue);
    }

    public String getName() {
        return queue.getName();
    }

    @Override
    public boolean add(final Runnable e) {
        return queue.add(e);
    }

    public String getServiceName() {
        return queue.getServiceName();
    }

    @Override
    public boolean offer(final Runnable e) {
        try {
            return queue.offer(e);
        } catch (HazelcastInstanceNotActiveException ex) {
            return false;
        }
    }

    @Override
    public void put(final Runnable e) throws InterruptedException {
        try {
            queue.put(e);
        } catch (HazelcastInstanceNotActiveException ex) {
            throw new InterruptedException();
        }
    }

    public void destroy() {
        queue.destroy();
    }

    @Override
    public boolean offer(final Runnable e, final long timeout, final TimeUnit unit) throws InterruptedException {
        try {
            return queue.offer(e, timeout, unit);
        } catch (HazelcastInstanceNotActiveException ex) {
            throw new InterruptedException();
        }
    }

    @Override
    public Runnable take() throws InterruptedException {
        try {
            return queue.take();
        } catch (HazelcastInstanceNotActiveException e) {
            throw new InterruptedException();
        }
    }

    public boolean removeItemListener(final String registrationId) {
        return queue.removeItemListener(registrationId);
    }

    @Override
    public Runnable poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        try {
            return queue.poll(timeout, unit);
        } catch (HazelcastInstanceNotActiveException e) {
            throw new InterruptedException();
        }
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public boolean remove(final Object o) {
        try {
            return queue.remove(o);
        } catch (HazelcastInstanceNotActiveException e) {
            return false;
        }
    }

    @Override
    public boolean contains(final Object o) {
        return queue.contains(o);
    }

    @Override
    public int drainTo(final Collection<? super Runnable> c) {
        return queue.drainTo(c);
    }

    @Override
    public int drainTo(final Collection<? super Runnable> c, final int maxElements) {
        return queue.drainTo(c, maxElements);
    }

    @Override
    public Runnable remove() {
        try {
            return queue.remove();
        } catch (HazelcastInstanceNotActiveException e) {
            return null;
        }
    }

    @Override
    public Runnable poll() {
        try {
            return queue.poll();
        } catch (HazelcastInstanceNotActiveException e) {
            return null;
        }
    }

    @Override
    public Runnable element() {
        return queue.element();
    }

    @Override
    public Runnable peek() {
        return queue.peek();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        try {
            return queue.isEmpty();
        } catch (HazelcastInstanceNotActiveException e) {
            return true;
        }
    }

    @Override
    public Iterator<Runnable> iterator() {
        return queue.iterator();
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return queue.toArray(a);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends Runnable> c) {
        return queue.addAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return queue.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return queue.retainAll(c);
    }

    @Override
    public void clear() {
        queue.clear();
    }

    public LocalQueueStats getLocalQueueStats() {
        return queue.getLocalQueueStats();
    }

    @Override
    public boolean equals(final Object o) {
        return queue.equals(o);
    }

    @Override
    public int hashCode() {
        return queue.hashCode();
    }

    public DelegatingQueue(final IQueue<Runnable> queue) {
        this.queue = queue;
    }

}
