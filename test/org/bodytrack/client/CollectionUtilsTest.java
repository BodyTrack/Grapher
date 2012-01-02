package org.bodytrack.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CollectionUtilsTest {

	@Test(expected=NullPointerException.class)
	public void testNullList() {
		CollectionUtils.insertInOrder(null, 0);
	}

	@Test(expected=NullPointerException.class)
	public void testNullElement() {
		CollectionUtils.insertInOrder(new ArrayList<Integer>(), null);
	}

	@Test
	public void testEmptyList() {
		final List<Integer> lst = new ArrayList<Integer>();

		CollectionUtils.insertInOrder(lst, 0);

		assertEquals(lst.size(), 1);

		assertEquals(lst.get(0).intValue(), 0);
	}

	@Test
	public void testShortList() {
		final List<Integer> lst = new ArrayList<Integer>();

		lst.add(5);
		lst.add(42);

		CollectionUtils.insertInOrder(lst, 7);

		assertEquals(lst.size(), 3);

		assertEquals(lst.get(0).intValue(), 5);
		assertEquals(lst.get(1).intValue(), 7);
		assertEquals(lst.get(2).intValue(), 42);
	}
}
