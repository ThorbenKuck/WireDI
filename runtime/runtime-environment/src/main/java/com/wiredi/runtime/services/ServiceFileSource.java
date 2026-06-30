package com.wiredi.runtime.services;

import java.util.List;

public interface ServiceFileSource {

    <T> ServiceFiles<T> getServiceFiles(Class<T> type);

    default <T> List<T> loadServiceFiles(Class<T> type) {
        return getServiceFiles(type).instances();
    }

}
