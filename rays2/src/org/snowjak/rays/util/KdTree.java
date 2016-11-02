package org.snowjak.rays.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.util.KdTree.HasCoordinates;

/**
 * Implements a kd-tree
 * 
 * @author snowjak88
 *
 * @param <P>
 *            the k-dimensional point type -- implements {@link HasCoordinates}
 * @param <N>
 *            the {@link Number} type that all coordinates are specified in
 */
public class KdTree<P extends HasCoordinates<N>, N extends Number & Comparable<N>> implements Iterable<P> {

	private int k;

	private KdNode<P, N> root = null;

	/**
	 * Construct a new Kd-tree of dimensionality {@code k}.
	 * 
	 * <p>
	 * <strong>Note</strong> that {@code k} is clamped to {@code [1, )}
	 * </p>
	 * 
	 * @param k
	 */
	public KdTree(int k) {
		this.k = FastMath.max(1, k);
	}

	/**
	 * Add a single point to this kd-tree
	 * 
	 * @param newPoint
	 */
	public void addPoint(P newPoint) {

		if (root == null)
			root = new KdNode<P, N>(k, 0, newPoint);

		else
			root.addPoint(newPoint);
	}

	/**
	 * Add a collection of points to this kd-tree
	 * 
	 * @param newPoints
	 */
	public void addPoints(Collection<P> newPoints) {

		if (newPoints.isEmpty())
			return;

		List<P> pointsSortedAlongDimension = newPoints.stream()
				.sorted((p1, p2) -> p1.getCoordinate(0).compareTo(p2.getCoordinate(0)))
				.collect(Collectors.toCollection(LinkedList::new));

		int medianIndex = FastMath.min(pointsSortedAlongDimension.size() / 2, pointsSortedAlongDimension.size() - 1);

		this.addPoint(pointsSortedAlongDimension.get(medianIndex));
		if (medianIndex > 0)
			root.addPoints(pointsSortedAlongDimension.subList(0, medianIndex));
		if (medianIndex < pointsSortedAlongDimension.size() - 1)
			root.addPoints(pointsSortedAlongDimension.subList(medianIndex + 1, pointsSortedAlongDimension.size()));
	}

	/**
	 * Get the {@code n} points stored in the kd-tree that are closest to the
	 * given {@code point}. If the kd-tree contains fewer than {@code n} points,
	 * then this method returns all points currently stored in this tree.
	 * 
	 * @param point
	 * @param n
	 * @param predicate
	 * @return a collection of points that are closest to the given point
	 */
	public Collection<P> getNClosestPointsTo(P point, int n) {

		return getNClosestPointsTo(point, n, (p) -> true);
	}

	/**
	 * Get the {@code n} points stored in the kd-tree that are closest to the
	 * given {@code point} and fulfill {@code additionalPredicate}. If the
	 * kd-tree contains fewer than {@code n} such points, then this method
	 * returns all points currently stored in this tree.
	 * 
	 * @param point
	 * @param n
	 * @param additionalPredicate
	 * @param predicate
	 * @return a collection of points that are closest to the given point
	 */
	public Collection<P> getNClosestPointsTo(P point, int n, Predicate<P> additionalPredicate) {

		if (root == null)
			return Collections.emptyList();

		return root.getNClosestPointsTo(point, n, additionalPredicate);
	}

	/**
	 * @return the root-level {@link KdNode} instance
	 */
	public KdNode<P, N> getRoot() {

		return root;
	}

	/**
	 * Create a new {@link KdTree} of dimensionality {@code k} from the given
	 * collection of points.
	 * 
	 * @param points
	 * @param k
	 * @return a new KdTree containing the given points
	 */
	public static <P extends HasCoordinates<N>, N extends Number & Comparable<N>> KdTree<P, N> createFromPoints(
			Collection<P> points, int k) {

		KdTree<P, N> newTree = new KdTree<>(k);

		return newTree;
	}

	/**
	 * Represents a single node within a {@link KdTree}. A node marks the
	 * location of a hyperplane that splits the k-dimensional space.
	 * 
	 * @author snowjak88
	 *
	 * @param <P>
	 *            the point type -- implements {@link HasCoordinates}
	 * @param <N>
	 *            the {@link Number} type in which all coordinates are given
	 */
	public static class KdNode<P extends HasCoordinates<N>, N extends Number & Comparable<N>> {

		private P point = null;

		private int k;

		private int nodeLevel;

		private Optional<KdNode<P, N>> leftBranch = Optional.empty(), rightBranch = Optional.empty();

		/**
		 * Create a new KdNode of the given dimensionality {@code k}, at
		 * {@code nodeLevel} levels removed from the tree-root, and containing
		 * the given {@code point}.
		 * 
		 * @param k
		 * @param nodeLevel
		 * @param point
		 */
		public KdNode(int k, int nodeLevel, P point) {
			this.k = k;
			this.nodeLevel = nodeLevel;
			this.point = point;
		}

		/**
		 * Add a point to this KdNode. If this node already directly contains a
		 * point, then this point is added to this node's left or right branch
		 * (depending on the point's relation to this node's splitting
		 * hyperplane.
		 * 
		 * @param newPoint
		 */
		public void addPoint(P newPoint) {

			N thisPointCoordinate = point.getCoordinate(getNodeDimension());
			N newPointCoordinate = newPoint.getCoordinate(getNodeDimension());

			if (thisPointCoordinate.compareTo(newPointCoordinate) < 0) {
				if (leftBranch.isPresent())
					leftBranch.get().addPoint(newPoint);
				else
					setLeftBranch(new KdNode<>(getK(), getNodeLevel() + 1, newPoint));

			} else {
				if (rightBranch.isPresent())
					rightBranch.get().addPoint(newPoint);
				else
					setRightBranch(new KdNode<>(getK(), getNodeLevel() + 1, newPoint));
			}
		}

		/**
		 * Add a collection of points to this KdNode. Similar to
		 * {@link #addPoint(HasCoordinates)}, each entry in the given collection
		 * is added to either this node's left or right branch (depending on the
		 * point's relation to this node's splitting hyperplane).
		 * 
		 * @param newPoints
		 */
		public void addPoints(Collection<P> newPoints) {

			List<P> pointsSortedAlongDimension = newPoints.stream()
					.sorted((p1, p2) -> p1.getCoordinate(getNodeDimension())
							.compareTo(p2.getCoordinate(getNodeDimension())))
					.collect(Collectors.toCollection(LinkedList::new));

			int medianIndex = FastMath
					.max(FastMath.min(pointsSortedAlongDimension.size() / 2, pointsSortedAlongDimension.size() - 1), 0);

			this.addPoint(pointsSortedAlongDimension.get(medianIndex));
			if (medianIndex > 0) {
				List<P> leftPoints = pointsSortedAlongDimension.subList(0, medianIndex);
				leftBranch.orElse(this).addPoints(leftPoints);
			}

			if (medianIndex < pointsSortedAlongDimension.size() - 1) {
				List<P> rightPoints = pointsSortedAlongDimension.subList(medianIndex + 1,
						pointsSortedAlongDimension.size());
				rightBranch.orElse(this).addPoints(rightPoints);
			}
		}

		/**
		 * @param point
		 * @param n
		 * @return the {@code n} points in the tree which are closest to the
		 *         given {@code point}
		 */
		public Collection<P> getNClosestPointsTo(P point, int n) {

			return getNClosestPointsTo(point, n, (p) -> true);
		}

		/**
		 * @param point
		 * @param n
		 * @param additionalPredicate
		 * @return the {@code n} points in the tree which are closest to the
		 *         given {@code point} and which satisfy the given
		 *         {@code additionalPredicate}
		 */
		public Collection<P> getNClosestPointsTo(P point, int n, Predicate<P> additionalPredicate) {

			if (!leftBranch.isPresent() && !rightBranch.isPresent())
				return Arrays.asList(this.point);

			double distance_best;
			Collection<P> points_best = new LinkedList<>();

			N coordinate_thisNode = this.point.getCoordinate(getNodeDimension());
			N coordinate_point = point.getCoordinate(getNodeDimension());

			// the distance between the given point and this KdNode's point
			double distance_thisNode_point = this.point.getDistance(point);

			if (additionalPredicate.test(this.point)) {
				distance_best = distance_thisNode_point;
				points_best.add(this.point);
			} else {
				distance_best = Double.MAX_VALUE;
			}

			//
			// Now -- calculate the minimum distance between the point and this
			// node's children.
			//
			// Which branch we proceed down depends on which side of the
			// hyperplane the point resides.
			Collection<P> children = Collections.emptyList();
			boolean pointIsLeftOfHyperplane = false, pointIsRightOfHyperplane = false;

			if (coordinate_point.compareTo(coordinate_thisNode) < 0 && leftBranch.isPresent()) {
				children = leftBranch.get().getNClosestPointsTo(point, n, additionalPredicate);
				pointIsLeftOfHyperplane = true;

			} else if (rightBranch.isPresent()) {
				children = rightBranch.get().getNClosestPointsTo(point, n, additionalPredicate);
				pointIsRightOfHyperplane = true;
			}

			double distance_child_point = children.parallelStream()
					.map(p -> point.getDistance(p))
					.max(Double::compare)
					.orElse(Double.MAX_VALUE);

			if (distance_child_point < distance_best) {
				distance_best = distance_child_point;
				points_best.addAll(children);
			}

			points_best = points_best.parallelStream()
					.sorted((p1, p2) -> Double.compare(point.getDistance(p1), point.getDistance(p2)))
					.limit(n)
					.collect(Collectors.toCollection(LinkedList::new));

			//
			// But now -- do we need to check down the other branch of the tree?
			//
			// First -- *can* we check the other branch of the tree?
			if ((pointIsLeftOfHyperplane && !rightBranch.isPresent())
					|| (pointIsRightOfHyperplane && !leftBranch.isPresent()))
				return points_best;

			// the distance between the given point and this KdNode's
			// dimensional axis
			// (which is defined by the clipping hyperplane that passes through
			// this node's point).
			//
			// i.e., calculate the "orthogonal distance" from the point to the
			// hyperplane
			double distance_axis_point = FastMath.abs(new BigDecimal(coordinate_point.toString())
					.subtract(new BigDecimal(coordinate_thisNode.toString())).doubleValue());

			//
			// Now *need* we check the other branch of the tree?
			if (distance_best < distance_axis_point)
				return points_best;

			//
			// the best-yet point is still far enough away from the
			// hyperplane that a better distance might be found on the other
			// side (the side we've not yet explored) of the hyperplane.
			Collection<P> otherChildren = new LinkedList<>();
			if (pointIsLeftOfHyperplane)
				otherChildren = rightBranch.get().getNClosestPointsTo(point, n, additionalPredicate);

			else if (pointIsRightOfHyperplane)
				otherChildren = leftBranch.get().getNClosestPointsTo(point, n, additionalPredicate);

			double distance_otherChild_point = otherChildren.parallelStream()
					.map(p -> point.getDistance(p))
					.max(Double::compare)
					.orElse(Double.MAX_VALUE);

			if (distance_best > distance_otherChild_point) {
				distance_best = distance_otherChild_point;
			}
			points_best.addAll(otherChildren);

			return points_best.stream()
					.sorted((p1, p2) -> Double.compare(point.getDistance(p1), point.getDistance(p2)))
					.limit(n)
					.collect(Collectors.toCollection(LinkedList::new));
		}

		/**
		 * @return the point stored within this node
		 */
		public P getPoint() {

			return point;
		}

		/**
		 * Set this node's left-branch to point to some KdNode
		 * 
		 * @param leftBranch
		 */
		public void setLeftBranch(KdNode<P, N> leftBranch) {

			this.leftBranch = Optional.ofNullable(leftBranch);
		}

		/**
		 * @return this node's left-branch KdNode
		 */
		public Optional<KdNode<P, N>> getLeftBranch() {

			return leftBranch;
		}

		/**
		 * Set this node's right-branch to point to some KdNode
		 * 
		 * @param rightBranch
		 */
		public void setRightBranch(KdNode<P, N> rightBranch) {

			this.rightBranch = Optional.ofNullable(rightBranch);
		}

		/**
		 * @return this node's right-branch KdNode
		 */
		public Optional<KdNode<P, N>> getRightBranch() {

			return rightBranch;
		}

		/**
		 * @return this node's dimensionality
		 */
		public int getK() {

			return k;
		}

		/**
		 * @return this node's level -- i.e., the count of nodes that lie
		 *         between it and the tree's root
		 */
		public int getNodeLevel() {

			return nodeLevel;
		}

		/**
		 * @return this tree's "dimension" -- i.e., the axis with which this
		 *         node's splitting-hyperplane is aligned
		 */
		public int getNodeDimension() {

			return nodeLevel % k;
		}
	}

	/**
	 * Denotes that a type has coordinates of a certain dimension.
	 * 
	 * @author snowjak88
	 *
	 * @param <N>
	 *            the {@link Number} type used to store coordinate values
	 */
	public static interface HasCoordinates<N extends Number & Comparable<N>> {

		/**
		 * @return the complete coordinate-list
		 */
		public List<N> getCoordinates();

		/**
		 * @param dimension
		 * @return the coordinate associated with dimension #{@code dimension}
		 *         (0-based)
		 * @throws IndexOutOfBoundsException
		 *             if the given {@code dimension} does not represent a valid
		 *             dimension for this object
		 */
		public default N getCoordinate(int dimension) throws IndexOutOfBoundsException {

			if (dimension < 0 || dimension >= getDimensionality())
				throw new IndexOutOfBoundsException();

			return getCoordinates().get(dimension);
		}

		/**
		 * @return the dimensionality of the coordinates attached to this object
		 */
		public default int getDimensionality() {

			return getCoordinates().size();
		}

		/**
		 * @param other
		 * @return the distance (i.e., Euclidean distance) between this type and
		 *         another
		 */
		public double getDistance(HasCoordinates<N> other);
	}

	/**
	 * @return an Iterator across all points within this tree, from left- to
	 *         right-most nodes
	 */
	@Override
	public Iterator<P> iterator() {

		return buildNodePointList(root).iterator();
	}

	/**
	 * @return a list containing every point stored in this tree
	 */
	public List<P> getAllPoints() {

		return buildNodePointList(root);
	}

	private List<P> buildNodePointList(KdNode<P, N> currentNode) {

		List<P> results = new LinkedList<>();
		if (currentNode.leftBranch.isPresent())
			results.addAll(buildNodePointList(currentNode.leftBranch.get()));
		results.add(currentNode.point);
		if (currentNode.rightBranch.isPresent())
			results.addAll(buildNodePointList(currentNode.rightBranch.get()));
		return results;
	}
}
