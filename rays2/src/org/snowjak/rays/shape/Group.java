package org.snowjak.rays.shape;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Ray;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.world.World;

/**
 * Allows you to group several Shapes together under a single set of
 * transformations.
 * 
 * @author snowjak88
 *
 */
public class Group extends Shape {

	private static final Random RND = new Random();

	private List<Shape> children = new LinkedList<>();

	/**
	 * Create a new (empty) Group
	 */
	public Group() {
		this(Collections.emptyList());
	}

	/**
	 * Create a Group with a given set of children.
	 * 
	 * @param children
	 */
	public Group(Shape... children) {
		this(Arrays.asList(children));
	}

	/**
	 * Create a Group with a given set of children.
	 * 
	 * @param children
	 */
	public Group(Collection<Shape> children) {
		super();
		this.children.addAll(children);
	}

	/**
	 * Get this Group's set of children.
	 * 
	 * @return this Group's children
	 */
	public Collection<Shape> getChildren() {

		return children;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray, boolean includeBehindRayOrigin,
			boolean onlyIncludeClosest) {

		Ray transformedRay = worldToLocal(ray);

		List<Intersection<Shape>> results = new LinkedList<>();
		for (Shape child : children)
			results.addAll(child.getIntersections(transformedRay, includeBehindRayOrigin, onlyIncludeClosest));

		results = results.stream().map(i -> localToWorld(i)).collect(LinkedList::new, LinkedList::add,
				LinkedList::addAll);

		if (onlyIncludeClosest)
			results = results.stream()
					.filter(i -> Double.compare(FastMath.abs(i.getDistanceFromRayOrigin()), World.NEARLY_ZERO) >= 0)
					.sorted((s1, s2) -> Double.compare(s1.getDistanceFromRayOrigin(), s2.getDistanceFromRayOrigin()))
					.limit(1)
					.collect(Collectors.toCollection(LinkedList::new));

		return results;
	}

	@Override
	public Group copy() {

		Group newGroup = new Group(
				this.getChildren().stream().map(s -> s.copy()).collect(Collectors.toCollection(LinkedList::new)));
		newGroup = configureCopy(newGroup);

		return newGroup;
	}

	@Override
	public Vector3D getNormalRelativeTo(Vector3D localPoint) {

		Optional<Pair<Shape, Double>> nearestChild = children.parallelStream()
				.map(s -> new Pair<>(s, localPoint.distance(s.getLocation())))
				.sorted((p1, p2) -> Double.compare(p1.getValue(), p2.getValue()))
				.findFirst();
		if (nearestChild.isPresent()) {
			Shape child = nearestChild.get().getKey();
			return child.getNormalRelativeTo(child.worldToLocal(localPoint));
		}

		return new Sphere().getNormalRelativeTo(localPoint);
	}

	@Override
	public Vector3D selectPointWithin() {

		return localToWorld(children.get(RND.nextInt(children.size())).selectPointWithin());
	}

}
