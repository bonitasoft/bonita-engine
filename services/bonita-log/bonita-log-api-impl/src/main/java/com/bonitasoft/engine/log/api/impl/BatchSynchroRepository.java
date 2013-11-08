package com.bonitasoft.engine.log.api.impl;

public class BatchSynchroRepository {
    
    private final ThreadLocal<BatchLogSynchronization> synchronizations = new ThreadLocal<BatchLogSynchronization>();
    
    private static final BatchSynchroRepository INSTANCE = new BatchSynchroRepository();
    
    public static BatchSynchroRepository getInstance() {
        return INSTANCE;
    }
    
    public BatchLogSynchronization getSynchro() {
        return synchronizations.get();
    }
    
    public void addSynchro(final BatchLogSynchronization synchro) {
        synchronizations.set(synchro);
    }
    
    public void removeSynchro() {
        synchronizations.remove();
    }
    
}
