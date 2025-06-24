/*
 * Copyright 2012-2023 the original author or authors.
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

/**
 * Describe an item deprecation.
 *
 * @author Stephane Nicoll
 * @author Scott Frederick
 * @since 1.3.0
 */
public class ItemDeprecation {

	private String reason;

	private String replacement;

	private String since;

	private String level;

	public ItemDeprecation() {
		this(null, null, null);
	}

	public ItemDeprecation(String reason, String replacement, String since) {
		this(reason, replacement, since, null);
	}

	public ItemDeprecation(String reason, String replacement, String since, String level) {
		this.reason = reason;
		this.replacement = replacement;
		this.since = since;
		this.level = level;
	}

	public String getReason() {
		return this.reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getReplacement() {
		return this.replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	public String getSince() {
		return this.since;
	}

	public void setSince(String since) {
		this.since = since;
	}

	public String getLevel() {
		return this.level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

}
