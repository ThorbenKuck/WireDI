package com.github.thorbenkuck.di;

import javax.inject.Provider;
import java.util.List;

public interface Repository {
    boolean isLoaded();

    <T> T getInstance(Class<T> type);

    <T> T requireInstance(Class<T> type);

    <T> List<T> getAll(Class<T> type);

    <T> Provider<T> getProvider(Class<T> type);
}
