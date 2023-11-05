package com.wiredi.resources;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public interface WritableResource extends Resource {

	boolean isWritable();

	@NotNull
	OutputStream getOutputStream() throws IOException;
}
