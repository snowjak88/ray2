package org.snowjak.rays.shape.csg;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

/**
 * Implements the CSG-operation "intersection".
 * 
 * @author snowak
 *
 */
public class Intersect extends Shape {

	private Collection<Shape> children = new LinkedList<>();

	public Intersect() {
		this(Collections.emptyList());
	}

	public Intersect(Shape... children) {
		this(Arrays.asList(children));
	}

	public Intersect(Collection<Shape> children) {
		super();
		this.children.addAll(children);
		this.setAmbientColorScheme(null);
		this.setDiffuseColorScheme(null);
		this.setSpecularColorScheme(null);
		this.setEmissiveColorScheme(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersectionsIncludingBehind(Ray ray) {

		//
		//
		// Find the intersections between the ray and all child Shapes, sorted
		// by distance.
		//
		// Literally: get the list of Intersections for each child Shape,
		// flat-map those lists into a single list of Intersections, sort the
		// Intersections by distance, and collect into a new list.
		Ray localRay = worldToLocal(ray);
		List<Intersection<Shape>> childIntersections = children.parallelStream()
				.map(s -> s.getIntersectionsIncludingBehind(localRay))
				.flatMap(li -> li.stream())
				.map(i -> localToWorld(i))
				.filter(i -> Double.compare(i.getDistanceFromRayOrigin(), World.DOUBLE_ERROR) >= 0)
				.sequential()
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.collect(Collectors.toCollection(LinkedList::new));

		//
		//
		// Now examine each Intersection in order. Each Intersection marks the
		// point where the ray crosses the boundary of a Shape.
		//
		// With this particular CSG operation, we're interested in those
		// boundary-crossings where we're currently inside every single child
		// Shape at the same time.
		//
		Set<Shape> currentlyIn = new HashSet<>();
		//
		// Test to see if the given Ray starts inside of any of our
		// child Shapes.
		children.parallelStream().filter(s -> s.isInside(ray.getOrigin())).forEach(s -> currentlyIn.add(s));

		List<Intersection<Shape>> results = new LinkedList<>();
		//
		for (Intersection<Shape> currentIntersect : childIntersections) {

			Shape intersectedShape = currentIntersect.getIntersected();
			//
			// We're crossing a boundary. Are we currently inside and coming
			// out?
			if (currentlyIn.contains(intersectedShape)) {

				//
				//
				if (currentlyIn.containsAll(children))
					results.add(currentIntersect);

				currentlyIn.remove(intersectedShape);

			} else {

				//
				//
				// We're crossing into a Shape.
				currentlyIn.add(intersectedShape);

				// If we are now inside all child-Shapes -- after
				// crossing into one of them -- then we're entering the
				// Intersect.
				if (currentlyIn.containsAll(children))
					results.add(currentIntersect);
			}
		}

		//
		//
		// Finally, we need to convert each result Intersection so that
		// the reported intersected-Shape is this Union, not the child Shape.
		return results.stream().sequential().map(i -> {
			//
			// Has this Intersect been given its own definitive ColorSchemes,
			// which will override those of its children?
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
		}).collect(Collectors.toCollection(LinkedList::new));

	}

}
