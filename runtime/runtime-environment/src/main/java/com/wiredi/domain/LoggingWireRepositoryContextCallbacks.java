package com.wiredi.domain;

import com.google.auto.service.AutoService;
import com.wiredi.aspects.AspectRepository;
import com.wiredi.environment.Environment;
import com.wiredi.lang.time.Timed;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.beans.BeanContainer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@AutoService(WireRepositoryContextCallbacks.class)
public class LoggingWireRepositoryContextCallbacks implements WireRepositoryContextCallbacks {

	private static final Logger logger = LoggerFactory.getLogger(WireRepository.class);

	@Override
	public void loadingStarted(@NotNull WireRepository wireRepository) {
		logger.info("Starting to load the WireRepository");
	}

	@Override
	public void loadedEnvironment(@NotNull Timed timed, @NotNull Environment environment) {
		logger.info("Environment loaded in {}", timed);
	}

	@Override
	public void loadedBeanContainer(@NotNull Timed timed, @NotNull BeanContainer beanContainer) {
		logger.info("BeanContainer loaded in {}", timed);
	}

	@Override
	public void loadedAspectRepository(@NotNull Timed timed, @NotNull AspectRepository aspectRepository) {
		logger.info("AspectRepository loaded in {}", timed);
	}

	@Override
	public void loadedEagerClasses(@NotNull Timed timed, @NotNull List<? extends Eager> eagerInstances) {
		logger.info("Eager instances loaded in {}", timed);
	}

	@Override
	public void loadingFinished(@NotNull Timed timed, @NotNull WireRepository wireRepository) {
		logger.info("WireRepository was completely loaded in {}", timed);
	}

	@Override
	public int getOrder() {
		return Ordered.LAST;
	}
}
