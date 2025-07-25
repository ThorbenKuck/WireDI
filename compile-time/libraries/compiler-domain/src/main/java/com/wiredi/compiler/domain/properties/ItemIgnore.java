/*
 * Copyright 2012-2025 the original author or authors.
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

import com.wiredi.compiler.domain.properties.ItemMetadata.ItemType;
import org.jetbrains.annotations.NotNull;

/**
 * Ignored item.
 *
 * @author Moritz Halbritter
 * @since 3.5.0
 */
public record ItemIgnore(
        @NotNull ItemType type,
        @NotNull String name
) implements Comparable<ItemIgnore> {

    /**
     * Create an ignore for a property with the given name.
     *
     * @param name the name
     * @return the item ignore
     */
    public static ItemIgnore forProperty(String name) {
        return new ItemIgnore(ItemType.PROPERTY, name);
    }

    @Override
    public int compareTo(ItemIgnore other) {
        return name().compareTo(other.name());
    }
}
