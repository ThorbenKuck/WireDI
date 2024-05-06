package com.wiredi.compiler.logger.dispatcher;

import com.wiredi.compiler.logger.LogEntry;
import com.wiredi.compiler.logger.writer.LogWriter;
import com.wiredi.runtime.async.DataAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class WorkerThreadLogDispatcher implements LogDispatcher {

	private final Worker runnable;
	private final LinkedBlockingDeque<LogEntry> entries = new LinkedBlockingDeque<>();
	private final Executor executor = Executors.newSingleThreadExecutor();
	private boolean active = true;

	public WorkerThreadLogDispatcher(LogWriter... writerList) {
		runnable = new Worker(Arrays.asList(writerList));
		executor.execute(runnable);
	}

	public void addWriter(LogWriter logWriter) {
		runnable.dataAccess.write(() -> runnable.writerList.add(logWriter));
	}

	@Override
	public void dispatch(LogEntry logEntry) {
		if (active) {
			entries.add(logEntry);
		} else {
			System.err.println("Tried to log " + logEntry + " but the worker thread is already stopped!");
		}
	}

	class Worker implements Runnable {

		private final List<LogWriter> writerList;
		private final DataAccess dataAccess = new DataAccess();

		Worker(List<LogWriter> writerList) {
			this.writerList = new ArrayList<>(writerList);
		}

		@Override
		public void run() {
			while (!Thread.interrupted() && active) {
				try {
					write(entries.takeFirst());
				} catch (InterruptedException e) {
					active = false;
				}
			}
			active = false;
			while (entries.peek() != null) {
				write(entries.poll());
			}

			Thread.currentThread().interrupt();
		}

		private void write(LogEntry logEntry) {
			dataAccess.read(() -> writerList.forEach(writer -> writer.write(logEntry)));
		}
	}
}
