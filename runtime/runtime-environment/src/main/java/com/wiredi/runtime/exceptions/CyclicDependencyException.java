package com.wiredi.runtime.exceptions;

import com.wiredi.runtime.domain.provider.IdentifiableProvider;

import java.util.Collection;
import java.util.List;

public class CyclicDependencyException extends RuntimeException {
    public CyclicDependencyException(Collection<Object> cycleChain) {
        super(formatMessage(cycleChain));
    }

    private static String formatMessage(Collection<Object> cycleChain) {
        StringBuilder builder = new StringBuilder("Circular dependency detected!\n");
        List<String> names = cycleChain.stream()
                .map(CyclicDependencyException::prettyName)
                .toList();

        for (int i = 0; i < names.size(); i++) {
            if (i == 0) {
                builder.append("  ┌──> ");
            } else if (i == names.size() - 1) {
                builder.append("  └─── ");
            } else {
                builder.append("  │    ");
            }
            builder.append(names.get(i));
            if (i < names.size() - 1) {
                builder.append("\n  │      ↓\n");
            }
        }

        builder.append("\nSuggestion: Remove the cyclic dependency by utilizing field or setter injection, or by using an (Identifiable)Provider<T>.");

        return builder.toString();
    }

    private static String prettyName(Object o) {
        if (o instanceof IdentifiableProvider<?> i) {
            return i.type().toString();
        } else if (o instanceof Class<?> c) {
            return c.getSimpleName();
        } else {
            return o.toString();
        }
    }
}
