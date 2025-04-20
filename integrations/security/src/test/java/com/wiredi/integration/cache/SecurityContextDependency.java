package com.wiredi.integration.cache;

import com.wiredi.annotations.Wire;
import com.wiredi.runtime.security.SecurityContext;

@Wire
record SecurityContextDependency(SecurityContext securityContext) {
}
