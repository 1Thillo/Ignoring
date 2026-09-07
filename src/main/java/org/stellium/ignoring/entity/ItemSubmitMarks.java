package org.stellium.ignoring.entity;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/*
 * 26.2 splits rendering into a submit phase and an execute phase. An ignored
 * player is only marked during submit, so anything the execute phase draws no
 * longer knows which entity it belongs to. Items are drawn there, which is why
 * a held item stayed solid while its owner was already transparent.
 *
 * The submit node created for an item is the one thing both phases see, so it
 * carries the mark across.
 */
public final class ItemSubmitMarks {

	private static final Set<Object> MARKED = Collections.newSetFromMap(new IdentityHashMap<>());
	private static boolean current;

	private ItemSubmitMarks() {
	}

	public static void mark(Object submit) {
		MARKED.add(submit);
	}

	public static void beginSubmit(Object submit) {
		current = MARKED.contains(submit);
	}

	public static void endSubmit() {
		current = false;
	}

	public static boolean isCurrentMarked() {
		return current;
	}

	public static void clear() {
		MARKED.clear();
		current = false;
	}
}
