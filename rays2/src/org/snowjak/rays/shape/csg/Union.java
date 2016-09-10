package org.snowjak.rays.shape.csg;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.ColorScheme;
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
 * <li>a Group will give you start- and end-Intersections for each Shape in turn
 * </li>
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
 * @author snowjak88
 *
 */
public class Union extends Shape {

	private Collection<Shape> children = new LinkedList<>();

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
		setDiffuseColorScheme(null);
		setSpecularColorScheme(null);
		setEmissiveColorScheme(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersectionsIncludingBehind(Ray ray) {

		Ray transformedRay = worldToLocal(ray);

		//
		// Get the intersections reported by each child Shape,
		// flatten that list of lists into a single list of intersections,
		// and sort it by distance.
		List<Intersection<Shape>> intersections = children.parallelStream()
				.map(s -> s.getIntersectionsIncludingBehind(transformedRay)).flatMap(l -> l.parallelStream())
				.map(i -> localToWorld(i))
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.collect(LinkedList::new, LinkedList::add, LinkedList::addAll);

		if (intersections.isEmpty())
			return (List<Intersection<Shape>>) intersections;

		//
		//
		// Scan through the list of Intersections.
		// We are interested in those corresponding to overlapping Shapes --
		// specifically, we want to get the Intersections at the beginning and
		// end of each overlapping group.
		List<Intersection<Shape>> results = new LinkedList<>();
		Set<Shape> currentlyIn = new HashSet<>();
		for (Intersection<Shape> currentIntersect : intersections) {

			//
			// Each Intersection represents a point at which we cross a Shape
			// boundary.
			//
			// At each point: are we crossing *into* or *out of* that Shape?
			if (currentlyIn.contains(currentIntersect.getIntersected())) {
				//
				// We are in this Shape, so we're leaving it now.
				currentlyIn.remove(currentIntersect.getIntersected());
				//
				// Does this mean that we've left all Shapes behind? So we're
				// out of the overlapping group?
				if (currentlyIn.isEmpty())
					results.add(currentIntersect);

			} else {
				//
				// We are not yet in this Shape, crossing into it now.
				//
				// Are we just starting an overlapping group?
				if (currentlyIn.isEmpty())
					results.add(currentIntersect);
				currentlyIn.add(currentIntersect.getIntersected());
			}
		}

		//
		//

		//
		//
		// Finally, we need to convert each result Intersection so that
		// the reported intersected-Shape is this Union, not the child Shape.
		return results.stream().sequential().map(i -> {
			//
			// Has this Union been given its own definitive ColorSchemes, which
			// will override those of its children?
			ColorScheme diffuse = (this.getDiffuseColorScheme() != null) ? this.getDiffuseColorScheme()
					: i.getDiffuseColorScheme();
			ColorScheme specular = (this.getSpecularColorScheme() != null) ? this.getSpecularColorScheme()
					: i.getSpecularColorScheme();
			ColorScheme emissive = (this.getEmissiveColorScheme() != null) ? this.getEmissiveColorScheme()
					: i.getEmissiveColorScheme();

			return new Intersection<Shape>(i.getPoint(), i.getNormal(), i.getRay(), this, diffuse, specular, emissive);
		}).collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
	}

	@Override
	public boolean isInside(Vector3D point) {

		return children.parallelStream().anyMatch(s -> s.isInside(point));
	}

	/**
	 * @return this Union's set of child Shapes
	 */
	public Collection<Shape> getChildren() {

		return children;
	}

	@Override
	public Union copy() {

		Union newUnion = new Union();
		newUnion = configureCopy(newUnion);
		newUnion.getChildren().addAll(
				this.getChildren().stream().map(s -> s.copy()).collect(Collectors.toCollection(LinkedList::new)));

		return newUnion;
	}

	@Override
	public Vector3D getNormalRelativeTo(Vector3D localPoint) {

		return children.parallelStream()
				.map(s -> s.getIntersections(new Ray(localPoint, s.getLocation().subtract(localPoint).normalize())))
				.flatMap(li -> li.stream())
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.findFirst().get().getNormal();
	}
}
