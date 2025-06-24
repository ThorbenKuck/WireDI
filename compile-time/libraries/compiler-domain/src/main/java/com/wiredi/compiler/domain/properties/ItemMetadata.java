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

import org.jetbrains.annotations.Nullable;

public final class ItemMetadata implements Comparable<ItemMetadata> {

    private ItemType itemType;
    private String name;
    @Nullable
    private String type;
    @Nullable
    private String description;
    @Nullable
    private String sourceType;
    @Nullable
    private String sourceMethod;
    @Nullable
    private Object defaultValue;
    @Nullable
    private ItemDeprecation deprecation;

    ItemMetadata() {
    }

    public ItemMetadata(ItemType itemType, String name) {
        this.itemType = itemType;
        this.name = name;
    }

    public static ItemMetadata newGroup(String name) {
        return new ItemMetadata(ItemType.GROUP, name);
    }

    public static ItemMetadata newProperty(String name) {
        return new ItemMetadata(ItemType.PROPERTY, name);
    }

    public ItemMetadata withType(String type) {
        this.type = type;
        return this;
    }

    public ItemMetadata withDescription(String description) {
        this.description = description;
        return this;
    }

    public ItemMetadata withSourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public ItemMetadata withSourceMethod(String sourceMethod) {
        this.sourceMethod = sourceMethod;
        return this;
    }

    public ItemMetadata withDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ItemMetadata withDeprecation(ItemDeprecation deprecation) {
        this.deprecation = deprecation;
        return this;
    }

    public boolean isOfItemType(ItemType itemType) {
        return this.itemType == itemType;
    }

    public boolean hasSameType(ItemMetadata metadata) {
        return this.itemType == metadata.itemType;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceType() {
        return this.sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceMethod() {
        return this.sourceMethod;
    }

    public void setSourceMethod(String sourceMethod) {
        this.sourceMethod = sourceMethod;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ItemDeprecation getDeprecation() {
        return this.deprecation;
    }

    public void setDeprecation(ItemDeprecation deprecation) {
        this.deprecation = deprecation;
    }

    @Override
    public int compareTo(ItemMetadata o) {
        return getName().compareTo(o.getName());
    }

    /**
     * The item type.
     */
    public enum ItemType {

        /**
         * Group item type.
         */
        GROUP,

        /**
         * Property item type.
         */
        PROPERTY

    }

}
