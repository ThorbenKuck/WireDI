package com.wiredi.processor;

import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.processor.model.QualifiedCandidate;
import com.wiredi.processor.model.TestQualifier;
import com.wiredi.qualifier.QualifierType;
import com.wiredi.runtime.WireRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WireRepositoryTest {

	@Test
	public void testThatQualifierValuesAreFound() {
		WireRepository repository = WireRepository.open();
		var instance = repository.get(
				TypeIdentifier.of(QualifiedCandidate.class),
				QualifierType.newInstance(TestQualifier.class)
						.add("value", "1")
						.add("scope", "TEST")
						.build()
		);
		assertThat(instance).isNotNull();
	}
}
