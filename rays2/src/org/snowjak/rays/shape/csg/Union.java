package org.snowjak.rays.shape.csg;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays.Ray;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Group;
import org.snowjak.rays.shape.Shape;

/**
 * Implements the CSG operation "union".
 * <p>
 * A Union differs from a {@link Group} in terms of how many Intersections it
 * gives you when its constituent {@link Shape}s happen to overlap along the
 * path of the Ray you test:
 * <ul>
 * <li>a Union will give you only the start- and end-Intersections for all the
 * overlapping Shapes considered together</li>
 * <li>a Group will give you start- and end-Intersections for each Shape in
 * turn</li>
 * </ul>
 * </p>
 * <p>
 * Also, a Union will differ in how it reports those Intersections:
 * <ul>
 * <li>a Union will return Intersections that point to itself (the Union) as the
 * intersected shape (see {@link Intersection#getIntersected()})</li>
 * <li>a Group will return Intersections that point to its constituent child
 * Shapes</li>
 * </ul>
 * </p>
 * 
 * @author rr247200
 *
 */
public class Union extends Shape {

	private Collection<Shape> children = new LinkedList<>();

	/**
	 * Create a new Union with no initial child Shapes.
	 * 
	 * @param children
	 */
	public Union() {
		this(Collections.emptyList());
	}

	/**
	 * Create a new Union of the given child Shapes.
	 * 
	 * @param children
	 */
	public Union(Shape... children) {
		this(Arrays.asList(children));
	}

	/**
	 * Create a new Union of the given child Shapes.
	 * 
	 * @param children
	 */
	public Union(Collection<Shape> children) {
		super();
		this.children.addAll(children);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray) {

		Ray transformedRay = worldToLocal(ray);

		Deque<Intersection<Shape>> intersections = children.parallelStream()
				.map(s -> s.getIntersections(transformedRay))
				.flatMap(l -> l.parallelStream())
				.map(i -> localToWorld(i))
				.map(i -> new Intersection<Shape>(i.getPoint(), i.getNormal(), i.getRay(), this))
				.sequential()
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.collect(LinkedList::new, LinkedList::add, LinkedList::addAll);

		if (intersections.isEmpty())
			return (List<Intersection<Shape>>) intersections;

		return Arrays.asList(intersections.getFirst(), intersections.getLast());
	}

	/**
	 * @return this Union's set of child Shapes
	 */
	public Collection<Shape> getChildren() {

		return children;
	}
}
