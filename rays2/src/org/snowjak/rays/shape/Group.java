package org.snowjak.rays.shape;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays.Ray;
import org.snowjak.rays.intersect.Intersection;

/**
 * Allows you to group several Shapes together under a single set of
 * transformations.
 * 
 * @author rr247200
 *
 */
public class Group extends Shape {

	private Collection<Shape> children = new LinkedList<>();

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
	public List<Intersection<Shape>> getIntersections(Ray ray) {

		Ray transformedRay = worldToLocal(ray);

		List<Intersection<Shape>> results = new LinkedList<>();
		for (Shape child : children)
			results.addAll(child.getIntersections(transformedRay));

		Collections.sort(results,
				(i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()));

		return results;
	}

}
