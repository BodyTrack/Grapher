package org.bodytrack.client;

public class StringUtil {
	public static String join(String[] a, String delim) {
		if (a.length==0) return "";
		String ret = a[0];
		for (int i=1; i<a.length; i++) {
			ret += delim + a[i];
		}
		return ret;
	}
}
