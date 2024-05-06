package com.wiredi.processor.tck.domain.transactional;

import com.wiredi.annotations.Wire;

@Wire
public class TestService {

	@Transactional
	public String execute(String input) {
		return "Processed: " + input;
	}

	@Transactional
	public String handler(String input) {
		return execute(input);
	}

	@Transactional(preventNested = true)
	public String executeNotNested(String input) {
		return "Processed: " + input;
	}

	@Transactional(preventNested = true)
	public String handlerNotNested(String input) {
		return executeNotNested(input);
	}
}
