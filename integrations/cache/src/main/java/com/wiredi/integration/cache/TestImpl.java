package com.wiredi.integration.cache;

import com.wiredi.annotations.Primary;
import com.wiredi.annotations.Wire;
import jakarta.inject.Named;

@Wire
@Primary
@Named
public class TestImpl implements Test {
}
