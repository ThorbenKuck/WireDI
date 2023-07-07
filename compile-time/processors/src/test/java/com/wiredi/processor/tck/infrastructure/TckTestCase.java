package com.wiredi.processor.tck.infrastructure;

import org.junit.jupiter.api.DynamicNode;

import java.util.Collection;

public interface TckTestCase {

	Collection<DynamicNode> dynamicTests();

}
