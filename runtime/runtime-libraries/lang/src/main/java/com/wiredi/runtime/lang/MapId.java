package com.wiredi.runtime.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MapId {

    private final List<Object> args = new ArrayList<>();

    public static MapId of(Object arg) {
        return new MapId().add(arg);
    }

    public MapId add(Object arg) {
        this.args.add(arg);
        return this;
    }

    @Override
    public String toString() {
        return "MapId" + args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapId key)) return false;
        return Objects.equals(args, key.args);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(args);
    }
}
