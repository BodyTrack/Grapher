package org.bodytrack.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class CollectionUtilsTest {
	private static final int SEED = 424242;

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

	@Test
	public void testLongList() {
		final List<Integer> lst = new ArrayList<Integer>();
		final List<Integer> copy = new ArrayList<Integer>();
		final Random rg = new Random(SEED);

		for (int i = 0; i < 10000; i++) {
			int n = rg.nextInt();
			lst.add(n);
			CollectionUtils.insertInOrder(copy, n);
		}

		Collections.sort(lst);

		assertEquals(lst.size(), copy.size());

		for (int i = 0; i < lst.size(); i++)
			assertEquals(lst.get(i), copy.get(i));
	}
}
