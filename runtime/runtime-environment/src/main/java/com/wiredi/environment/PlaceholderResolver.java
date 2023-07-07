package com.wiredi.environment;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class PlaceholderResolver {

	private final String start;
	private final String stop;

	public PlaceholderResolver(String start, String stop) {
		this.start = start;
		this.stop = stop;
	}

	public List<Placeholder> resolveAllIn(String input) {
		final Set<Placeholder> placeholders = new HashSet<>();
		String workPiece = input;
		while(workPiece.contains(stop)) {
			if(workPiece.contains(start)) {
				final int end = workPiece.indexOf(stop);

				if (workPiece.indexOf(start) > end) {
					workPiece = workPiece.replaceFirst(Pattern.quote(stop), "");
					continue;
				} else {
					final int beginning = findClosestStartToEnd(workPiece, end);
					final int identifierCharIndex = beginning - 2;
					final char identifierChar = workPiece.charAt(identifierCharIndex);
					final String placeHolderValue = workPiece.substring(beginning, end);
					final Placeholder.Default defaultValue = defaultValue(placeHolderValue);
					placeholders.add(new Placeholder(start, stop, input, placeHolderValue, defaultValue, identifierChar));

					workPiece = workPiece.replaceFirst(Pattern.quote(start), "");
				}
			}
			workPiece = workPiece.replaceFirst(Pattern.quote(stop), "");
		}
		final List<Placeholder> result = new ArrayList<>(placeholders);
		placeholders.clear();
		return result;
	}

	@Nullable
	private Placeholder.Default defaultValue(String placeHolderValue) {
		int index = placeHolderValue.indexOf(":");
		if (index == -1) {
			return null;
		}

		return new Placeholder.Default(
				placeHolderValue.substring(index + 1),
				":"
		);
	}

	private int findClosestStartToEnd(String value, int end) {
		String workPiece = value;

		int increment = 0;
		int beginning = workPiece.indexOf(start);
		int pointer = beginning;
		String startPattern = Pattern.quote(start);

		while (pointer != -1 && pointer < end) {
			workPiece = workPiece.replaceFirst(startPattern, "");
			beginning = pointer;
			pointer = workPiece.indexOf(start);
			++increment;
		}

		return beginning + increment;
	}
}
