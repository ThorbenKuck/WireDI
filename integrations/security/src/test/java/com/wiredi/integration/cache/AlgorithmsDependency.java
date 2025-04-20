package com.wiredi.integration.cache;

import com.wiredi.annotations.Wire;
import com.wiredi.runtime.security.SecurityContext;
import com.wiredi.runtime.security.crypto.Algorithms;

@Wire
record AlgorithmsDependency(Algorithms algorithms) {
}
