package com.wiredi.lang.time;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.*;
import static org.assertj.core.api.Assertions.assertThat;

class TimedTest {

	@Test
	public void test() {
		var timed = Timed.of(Duration.of(3, DAYS))
				.plus(Duration.of(4, HOURS))
				.plus(Duration.of(7, MINUTES))
				.plus(Duration.of(100, SECONDS))
				.plus(Duration.of(25, NANOS));

		assertThat(timed.toString()).isEqualTo("3d, 4h, 8m, 40s");
	}
}
