package com.wiredi.compiler.logger.messager;

import com.wiredi.runtime.async.DataAccess;
import com.wiredi.runtime.values.Value;

import javax.annotation.processing.Messager;
import java.util.ArrayList;
import java.util.List;

public class MessagerRegistration {

	private static final List<MessagerAware> listeners = new ArrayList<>();
	private static final DataAccess dataAccess = new DataAccess();
	private static final Value<Messager> messager = Value.empty();

	public static boolean register(MessagerAware messagerAware) {
		return dataAccess.writeValue(() -> {
			listeners.add(messagerAware);
			messager.ifPresent(messagerAware::setMessager);
			return true;
		});
	}

	public static boolean unregister(MessagerAware messagerAware) {
		return dataAccess.writeValue(() -> listeners.remove(messagerAware));
	}

	public static void announce(Messager newMessager) {
		dataAccess.readValue(() -> new ArrayList<>(listeners)).forEach(it -> it.setMessager(newMessager));
		dataAccess.write(() -> messager.set(newMessager));
	}
}
