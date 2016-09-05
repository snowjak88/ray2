package org.snowjak.rays.shape.csg;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
		setAmbientColorScheme(null);
		setDiffuseColorScheme(null);
		setSpecularColorScheme(null);
		setEmissiveColorScheme(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray) {

		Ray transformedRay = worldToLocal(ray);

		//
		// Get the intersections reported by each child Shape,
		// flatten that list of lists into a single list of intersections,
		// and sort it by distance.
		List<Intersection<Shape>> intersections = children.parallelStream().map(s -> s.getIntersections(transformedRay))
				.flatMap(l -> l.parallelStream()).map(i -> localToWorld(i)).sequential()
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
			ColorScheme ambient = (this.getAmbientColorScheme() != null) ? this.getAmbientColorScheme()
					: i.getAmbientColorScheme();
			ColorScheme diffuse = (this.getDiffuseColorScheme() != null) ? this.getDiffuseColorScheme()
					: i.getDiffuseColorScheme();
			ColorScheme specular = (this.getSpecularColorScheme() != null) ? this.getSpecularColorScheme()
					: i.getSpecularColorScheme();
			ColorScheme emissive = (this.getEmissiveColorScheme() != null) ? this.getEmissiveColorScheme()
					: i.getEmissiveColorScheme();

			return new Intersection<Shape>(i.getPoint(), i.getNormal(), i.getRay(), this, ambient, diffuse, specular,
					emissive);
		}).collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
	}

	/**
	 * @return this Union's set of child Shapes
	 */
	public Collection<Shape> getChildren() {

		return children;
	}
}
