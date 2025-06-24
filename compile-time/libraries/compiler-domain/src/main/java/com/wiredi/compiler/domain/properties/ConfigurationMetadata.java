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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Configuration meta-data.
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @author Moritz Halbritter
 * @since 1.2.0
 * @see ItemMetadata
 */
public class ConfigurationMetadata {

	private final Map<String, List<ItemMetadata>> items;
	private final Map<String, List<ItemHint>> hints;
	private final Map<String, List<ItemIgnore>> ignored;

	public ConfigurationMetadata() {
		this.items = new ConcurrentHashMap<>();
		this.hints = new ConcurrentHashMap<>();
		this.ignored = new ConcurrentHashMap<>();
	}

	public ConfigurationMetadata(ConfigurationMetadata metadata) {
		this.items = new ConcurrentHashMap<>(metadata.items);
		this.hints = new ConcurrentHashMap<>(metadata.hints);
		this.ignored = new ConcurrentHashMap<>(metadata.ignored);
	}

	public ConfigurationMetadata addItem(ItemMetadata.ItemType itemType, String property, Consumer<ItemMetadata> itemConsumer) {
		ItemMetadata itemMetadata = new ItemMetadata(itemType, property);
		itemConsumer.accept(itemMetadata);
		this.items.computeIfAbsent(itemMetadata.getName(), k -> new ArrayList<>()).add(itemMetadata);
		return this;
	}

	public ConfigurationMetadata addHint(String name, Consumer<ItemHint> itemConsumer) {
		ItemHint itemMetadata = new ItemHint(name);
		itemConsumer.accept(itemMetadata);
		this.hints.computeIfAbsent(name, k -> new ArrayList<>()).add(itemMetadata);
		return this;
	}

	public ConfigurationMetadata addIgnore(ItemIgnore itemIgnore) {
		ignored.computeIfAbsent(itemIgnore.name(), k -> new ArrayList<>()).add(itemIgnore);
		return this;
	}

	/**
	 * Remove item meta-data for the given item type and name.
	 * @param itemType the item type
	 * @param name the name
	 * @since 3.5.0
	 */
	public void removeMetadata(ItemMetadata.ItemType itemType, String name) {
		List<ItemMetadata> metadata = this.items.get(name);
		if (metadata == null) {
			return;
		}
		metadata.removeIf((item) -> item.isOfItemType(itemType));
		if (metadata.isEmpty()) {
			this.items.remove(name);
		}
	}

	/**
	 * Return item meta-data.
	 * @return the items
	 */
	public List<ItemMetadata> getItems() {
		return flattenValues(this.items);
	}

	public void setItems(List<ItemMetadata> items) {
		items.forEach(item -> {
			this.items.computeIfAbsent(item.getName(), k -> new ArrayList<>()).add(item);
		});
	}

	public void setHints(List<ItemHint> hints) {
		hints.forEach(hint -> {
			this.hints.computeIfAbsent(hint.name, k -> new ArrayList<>()).add(hint);
		});
	}

	public void setIgnored(List<ItemIgnore> ignored) {
		ignored.forEach(ignore -> {
			this.ignored.computeIfAbsent(ignore.name(), k -> new ArrayList<>()).add(ignore);
		});
	}

	/**
	 * Return hint meta-data.
	 * @return the hints
	 */
	public List<ItemHint> getHints() {
		return flattenValues(this.hints);
	}

	/**
	 * Return ignore meta-data.
	 * @return the ignores
	 */
	public List<ItemIgnore> getIgnored() {
		return flattenValues(this.ignored);
	}

	private static <T extends Comparable<T>> List<T> flattenValues(Map<?, List<T>> map) {
		List<T> content = new ArrayList<>();
		for (List<T> values : map.values()) {
			content.addAll(values);
		}
		Collections.sort(content);
		return content;
	}

	public boolean isEmpty() {
		return items.isEmpty() && hints.isEmpty() && ignored.isEmpty();
	}
}
