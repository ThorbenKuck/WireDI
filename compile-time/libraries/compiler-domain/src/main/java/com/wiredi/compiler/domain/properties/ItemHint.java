/*
 * Copyright 2012-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wiredi.compiler.domain.properties;

import java.util.*;

public class ItemHint implements Comparable<ItemHint> {

    final String name;
    private final List<ValueHint> values = new ArrayList<>();
    private final List<ValueProvider> providers = new ArrayList<>();

    public ItemHint(String name) {
        this.name = name;
    }

    public List<ValueHint> values() {
        return Collections.unmodifiableList(this.values);
    }

    public List<ValueProvider> providers() {
        return Collections.unmodifiableList(this.providers);
    }

    public void addValue(ValueHint value) {
        this.values.add(value);
    }

    public void addProvider(ValueProvider provider) {
        this.providers.add(provider);
    }

    @Override
    public int compareTo(ItemHint other) {
        return name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return "ItemHint{name='" + this.name + "', values=" + this.values + ", providers=" + this.providers + '}';
    }

    public record ValueHint(Object value, String description) {
    }

    public record ValueProvider(String name, Map<String, Object> parameters) {
    }

}
