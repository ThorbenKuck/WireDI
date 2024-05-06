package com.wiredi.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiredi.annotations.Wire;

@Wire
record Dependency(ObjectMapper objectMapper) {
}
