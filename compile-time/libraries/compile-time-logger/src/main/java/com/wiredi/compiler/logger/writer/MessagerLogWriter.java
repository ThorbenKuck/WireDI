package com.wiredi.compiler.logger.writer;

import com.wiredi.compiler.logger.LogEntry;
import com.wiredi.compiler.logger.messager.MessagerRegistration;
import com.wiredi.compiler.logger.messager.MessagerAware;
import com.wiredi.lang.values.SafeReference;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;

public class MessagerLogWriter implements LogWriter, MessagerAware {

	@NotNull
	private final List<LogEntry> buffer = new ArrayList<>();

	@NotNull
	private final SafeReference<Messager> messager = new SafeReference<>();

	public MessagerLogWriter() {
		MessagerRegistration.register(this);
	}

	@Override
	public void write(LogEntry logEntry) {
		synchronized (this) {
			this.messager.ifPresent(messager -> flush(logEntry, messager))
					.orElse(() -> buffer.add(logEntry));
		}
	}

	@Override
	public void setMessager(Messager messager) {
		synchronized (this) {
			if (messager != null) {
				this.messager.ifEmpty(() -> flushBufferTo(messager));
			}
			this.messager.set(messager);
		}
	}

	private void flushBufferTo(Messager messager) {
		buffer.forEach(logEntry -> {
			flush(logEntry, messager);
		});
	}

	private void flush(LogEntry logEntry, Messager messager) {
		messager.printMessage(
				logEntry.logLevel().diagnosticKind(),
				logEntry.message(),
				logEntry.targetElement(),
				logEntry.annotationMirror(),
				logEntry.annotationValue()
		);

		if (logEntry.rootElement() != null &&
				logEntry.targetElement() != null &&
				!logEntry.rootElement().equals(logEntry.targetElement())) {
			String rootElementWarning = "[" + logEntry.logLevel().name() + "@" + logEntry.targetElement().getSimpleName() + "]: " + logEntry.message();
			messager.printMessage(
					Diagnostic.Kind.NOTE,
					rootElementWarning,
					logEntry.rootElement(),
					logEntry.annotationMirror(),
					logEntry.annotationValue()
			);
		}
	}
}
