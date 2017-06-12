package org.gcube.portlets.user.simulfishgrowth.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {
	// http://stackoverflow.com/a/5283123/874502
	public <T> List<T> union(List<T> list1, List<T> list2) {
		Set<T> set = new HashSet<T>();

		set.addAll(list1);
		set.addAll(list2);

		return new ArrayList<T>(set);
	}

	// http://stackoverflow.com/a/5283123/874502
	public <T> List<T> intersection(List<T> list1, List<T> list2) {
		List<T> list = new ArrayList<T>();

		for (T t : list1) {
			if (list2.contains(t)) {
				list.add(t);
			}
		}

		return list;
	}

	public <T> List<T> complementOfRightInLeft(List<T> list1, List<T> list2) {
		List<T> list = new ArrayList<T>();

		for (T t : list1) {
			if (!list2.contains(t)) {
				list.add(t);
			}
		}

		return list;
	}

	public int median(Integer... numbers) {
		Arrays.sort(numbers);
		Integer toRet;
		if (numbers.length % 2 == 0)
			toRet =  (int)((numbers[numbers.length / 2] + numbers[numbers.length / 2 - 1]) / 2);
		else
			toRet = numbers[numbers.length / 2];
		return toRet;
	}
}
