package com.wiredi.processor;

import com.wiredi.lang.time.Timed;
import com.wiredi.processor.model.WireCandidate1;
import com.wiredi.processor.model.WireCandidate2;
import com.wiredi.runtime.WireRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class WireRepositoryContractTest {

	@Test
	public void testThatTheWireRepositoryCanLoadWireCandidate1Correctly() {
		SoftAssertions softAssertions = new SoftAssertions();
		Timed timed = Timed.of(() -> {
			WireRepository wireRepository = WireRepository.open();

			WireCandidate1 wireCandidate1 = wireRepository.get(WireCandidate1.class);
			WireCandidate2 wireCandidate2 = wireRepository.get(WireCandidate2.class);

			softAssertions.assertThat(wireCandidate1).withFailMessage("missing: field public").hasFieldOrPropertyWithValue("publicWireCandidate2", wireCandidate2);
			softAssertions.assertThat(wireCandidate1).withFailMessage("missing: field package private").hasFieldOrPropertyWithValue("packagePrivateWireCandidate2", wireCandidate2);
			softAssertions.assertThat(wireCandidate1).withFailMessage("missing: field private").hasFieldOrPropertyWithValue("privateWireCandidate2", wireCandidate2);
			softAssertions.assertThat(wireCandidate1).withFailMessage("missing: constructor Parameter").hasFieldOrPropertyWithValue("constructorParameter", wireCandidate2);
			softAssertions.assertThat(wireCandidate1).withFailMessage("missing: inherited field public").hasFieldOrPropertyWithValue("inheritedPublicWireCandidate2", wireCandidate2);
			softAssertions.assertThat(wireCandidate1).withFailMessage("missing: inherited field package private").hasFieldOrPropertyWithValue("inheritedPackagePrivateWireCandidate2", wireCandidate2);
			softAssertions.assertThat(wireCandidate1).withFailMessage("missing: inherited field private").hasFieldOrPropertyWithValue("inheritedPrivateWireCandidate2", wireCandidate2);
		});

		assertThat(timed.get(TimeUnit.MILLISECONDS)).isLessThan(300);
		softAssertions.assertAll();
	}

}
