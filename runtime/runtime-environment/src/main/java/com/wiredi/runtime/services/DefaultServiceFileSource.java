package com.wiredi.runtime.services;

public class DefaultServiceFileSource implements ServiceFileSource {

    public static final DefaultServiceFileSource INSTANCE = new DefaultServiceFileSource();

    @Override
    public <T> ServiceFiles<T> getServiceFiles(Class<T> type) {
        return ServiceFiles.getInstance(type);
    }
}
