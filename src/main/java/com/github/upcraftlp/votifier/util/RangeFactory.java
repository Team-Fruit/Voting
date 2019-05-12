package com.github.upcraftlp.votifier.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Range;

public class RangeFactory {
	private static final String f = "[+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][+-]?[0-9]+)?";
	private static final Pattern p = Pattern.compile("^[\\(|\\[]("+f+"),("+f+")[\\)|\\]]$");

	public static Range<Float> from(String range) throws IllegalArgumentException {
		Matcher m = p.matcher(range);
		if (m.matches()) {
			int length = range.length();

			float lowerBound = Float.parseFloat(m.group(1));
			float upperBound = Float.parseFloat(m.group(2));

			if (range.charAt(0)=='(') {
				if (range.charAt(length-1)==')') {
					return Range.open(lowerBound, upperBound);
				}
				return Range.openClosed(lowerBound, upperBound);
			} else {
				if (range.charAt(length-1)==')') {
					return Range.closedOpen(lowerBound, upperBound);
				}
				return Range.closed(lowerBound, upperBound);
			}
		}
		throw new IllegalArgumentException("Range "+range+" is not valid.");
	}
}