package com.wiredi.processor.lang;

import org.mockito.MockSettings;
import org.mockito.Mockito;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class ProcessingEnvironmentMock implements ProcessingEnvironment {
	@Override
	public Map<String, String> getOptions() {
		return (Map<String, String>) Collections.EMPTY_MAP;
	}

	@Override
	public Messager getMessager() {
		return Mockito.mock(Messager.class);
	}

	@Override
	public Filer getFiler() {
		return Mockito.mock(Filer.class);
	}

	@Override
	public Elements getElementUtils() {
		return Mockito.mock(Elements.class);
	}

	@Override
	public Types getTypeUtils() {
		return Mockito.mock(Types.class);
	}

	@Override
	public SourceVersion getSourceVersion() {
		return SourceVersion.RELEASE_17;
	}

	@Override
	public Locale getLocale() {
		return Locale.getDefault();
	}
}
