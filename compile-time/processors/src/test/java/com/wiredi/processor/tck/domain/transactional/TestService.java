package com.wiredi.processor.tck.domain.transactional;

import com.wiredi.annotations.Wire;

@Wire
public class TestService {

	@Transactional
	public String execute(String input) {
		return "Processed: " + input;
	}
}
