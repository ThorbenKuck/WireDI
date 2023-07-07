package com.wiredi.processor.tck.domain.override;

import com.wiredi.annotations.Wire;
import jakarta.inject.Named;

@Wire
@Named("qualified")
public class QualifiedDependency implements IDependency {
}
