package org.yaxim.androidclient.util;

public final class ArrayUtils {
	private ArrayUtils() {}

	/**
	 * Checks whether a list contains an element
	 * 
	 * @param id
	 * @param list
	 * @return
	 */
	public static boolean contains(long list[], long element) {
		if (list == null) return false;
		for (long l : list)	if (l == element) return true;
		return false;
	}

}
