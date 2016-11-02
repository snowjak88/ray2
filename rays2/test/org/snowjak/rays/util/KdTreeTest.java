package org.snowjak.rays.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.util.KdTree.HasCoordinates;

import junit.framework.TestCase;

@SuppressWarnings("javadoc")
public class KdTreeTest extends TestCase {

	public void testAddPoint() {

		KdTree<TestPoint, Integer> tree = new KdTree<>(2);

		tree.addPoint(new TestPoint(1, 1));

		assertEquals(Arrays.asList(1, 1), tree.getRoot().getPoint().getCoordinates());
	}

	public void testAddPoints() {

		KdTree<TestPoint, Integer> tree = new KdTree<>(2);
		tree.addPoints(Arrays.asList(new TestPoint(0, 0), new TestPoint(4, 0), new TestPoint(0, 4), new TestPoint(4, 4),
				new TestPoint(2, 2)));

		assertEquals(Arrays.asList(2, 2), tree.getRoot().getPoint().getCoordinates());
	}

	public void testGetNClosestPoints() {

		List<TestPoint> testPoints = Arrays.asList(new TestPoint(0, 0), new TestPoint(4, 0), new TestPoint(0, 4), new TestPoint(4, 4),
				new TestPoint(2, 2));
		KdTree<TestPoint, Integer> tree = new KdTree<>(2);
		tree.addPoints(testPoints);

		List<TestPoint> closePoints = new LinkedList<>();
		closePoints.addAll(tree.getNClosestPointsTo(new TestPoint(1, 1), 2, (p) -> true));
		assertFalse(closePoints.isEmpty());
		assertEquals(2, closePoints.size());

		assertTrue(closePoints.contains(testPoints.get(0)));
		assertTrue(closePoints.contains(testPoints.get(4)));

		assertFalse(closePoints.contains(testPoints.get(1)));
		assertFalse(closePoints.contains(testPoints.get(2)));
		assertFalse(closePoints.contains(testPoints.get(3)));
	}

	public static class TestPoint implements HasCoordinates<Integer> {

		private List<Integer> coordinates = new LinkedList<>();

		public TestPoint(Integer... coordinates) {
			this.coordinates = Arrays.asList(coordinates);
		}

		@Override
		public List<Integer> getCoordinates() {

			return coordinates;
		}

		@Override
		public double getDistance(HasCoordinates<Integer> other) {

			if (other.getDimensionality() != this.getDimensionality())
				throw new IllegalArgumentException("Cannot get distance when dimensionality is unmatched.");

			double totalSquare = 0d;
			for (int i = 0; i < this.getDimensionality(); i++)
				totalSquare += FastMath.pow(this.getCoordinate(i) - other.getCoordinate(i), 2.0);

			return FastMath.sqrt(totalSquare);
		}

		@Override
		public String toString() {

			return "[" + coordinates + "]";
		}

	}

}
