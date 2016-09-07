package org.snowjak.rays.shape.csg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

/**
 * Implements the CSG operation "minus".
 * 
 * @author rr247200
 *
 */
public class Minus extends Shape {

	private Shape minuend;

	private Collection<Shape> subtrahends = new LinkedList<>();

	public Minus(Shape minuend, Shape... children) {
		this(minuend, Arrays.asList(children));
	}

	public Minus(Shape minuend, Collection<Shape> children) {
		super();
		this.minuend = minuend;
		this.subtrahends.addAll(children);
		this.setAmbientColorScheme(null);
		this.setDiffuseColorScheme(null);
		this.setSpecularColorScheme(null);
		this.setEmissiveColorScheme(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersectionsIncludingBehind(Ray ray) {

		Ray localRay = worldToLocal(ray);

		//
		//
		// First of all, test if this ray intersects the Minuend.
		// If it doesn't intersect the Minuend, then there's no point in testing
		// all our subtrahends as well.
		List<Intersection<Shape>> childIntersections = minuend.getIntersectionsIncludingBehind(localRay)
				.parallelStream()
				.filter(i -> Double.compare(i.getDistanceFromRayOrigin(), World.DOUBLE_ERROR) >= 0)
				.collect(Collectors.toCollection(LinkedList::new));

		if (childIntersections.isEmpty())
			return Collections.emptyList();

		//
		//
		// Find the intersections between the ray and all subtrahend Shapes.
		//
		// Literally: get the list of Intersections for each subtrahend,
		// flat-map those lists into a single list of Intersections.

		childIntersections.addAll(subtrahends.parallelStream()
				.map(s -> s.getIntersectionsIncludingBehind(localRay))
				.flatMap(li -> li.stream())
				.filter(i -> Double.compare(i.getDistanceFromRayOrigin(), World.DOUBLE_ERROR) >= 0)
				.collect(Collectors.toCollection(ArrayList::new)));

		//
		// Sort everything by distance!
		Collections.sort(childIntersections,
				(i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()));

		//
		//
		// Now examine each Intersection in order. Each Intersection marks the
		// point where the ray crosses the boundary of a Shape.
		//
		// With this particular CSG operation, we're interested in those
		// boundary-crossings where we're currently inside the Minuend and *not*
		// inside any of the subtrahends.
		//
		Set<Shape> currentlyInSubtrahends = new HashSet<>();
		boolean currentlyInMinuend = false;

		if (!childIntersections.isEmpty())
			this.getClass();

		//
		// Test to see if the given Ray starts inside of any of our
		// subtrahends.
		// subtrahends.parallelStream()
		// .filter(s -> s.isInside(localRay.getOrigin()))
		// .forEach(s -> currentlyInSubtrahends.add(s));
		//
		// And test again to see if the Ray starts within the minuend.
		// currentlyInMinuend = minuend.isInside(localRay.getOrigin());

		List<Intersection<Shape>> results = new LinkedList<>();
		//
		for (Intersection<Shape> currentIntersect : childIntersections) {

			Shape intersectedShape = currentIntersect.getIntersected();
			//
			// We're crossing a boundary. Which boundary?
			// Does it belong to the minuend?
			if (intersectedShape == minuend) {

				//
				// Crossing a minuend boundary.
				//
				// Are we currently in any of the subtrahends?
				if (!currentlyInAnySubtrahend(currentIntersect.getPoint())) {
					//
					// No -- not in any subtrahend. So record this minuend
					// transition!
					results.add(currentIntersect);
				} else {
					//
					// Yes -- we're already in at least one subtrahend.
					// So this minuend-transition doesn't "count" -- we've
					// already recorded transitioning into the subtrahend.
					// So don't record the current intersection.
				}

				currentlyInMinuend = !currentlyInMinuend;

			} else {

				//
				// Crossing a subtrahend boundary!
				//
				// Are we crossing in, or out?
				if (getContainingSubtrahends(currentIntersect.getPoint()).contains(intersectedShape)) {
					//
					// Crossing out of this subtrahend.
					currentlyInSubtrahends.remove(intersectedShape);
					//
					// Are we currently in the minuend, and did we just exit all
					// subtrahends?
					if (currentlyInMinuend(currentIntersect.getPoint()) && currentlyInSubtrahends.isEmpty()) {
						//
						// Yes! So record this transition.
						// HOWEVER -- we need to flip the reported normal.
						results.add(new Intersection<Shape>(currentIntersect.getPoint(),
								currentIntersect.getNormal().negate(), currentIntersect.getRay(),
								currentIntersect.getIntersected(), currentIntersect.getAmbientColorScheme(),
								currentIntersect.getDiffuseColorScheme(), currentIntersect.getSpecularColorScheme(),
								currentIntersect.getEmissiveColorScheme()));
					} else {
						//
						// No! either we're still in at least one subtrahend, or
						// we're not inside the minuend.
						// So do nothing for this transition.
					}

				} else {
					//
					// Crossing into this subtrahend.
					// Are we currently inside the minuend, and up to now
					// outside all subtrahends?
					// If so, then record this transition.
					if (currentlyInMinuend(currentIntersect.getPoint()) && currentlyInSubtrahends.isEmpty()) {
						results.add(currentIntersect);
					} else {
						//
						// No! either we're already in at least one subtrahend,
						// or we're not inside the minuend.
						// Either way, do nothing for this transition.
					}

					currentlyInSubtrahends.add(intersectedShape);
				}

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
		}).map(i -> localToWorld(i)).collect(Collectors.toCollection(LinkedList::new));

	}

	private boolean currentlyInMinuend(Vector3D point) {

		return minuend.isInside(point);
	}

	private boolean currentlyInAnySubtrahend(Vector3D point) {

		return subtrahends.parallelStream().anyMatch(s -> s.isInside(point));
	}

	private List<Shape> getContainingSubtrahends(Vector3D point) {

		return subtrahends.parallelStream()
				.filter(s -> s.isInside(point))
				.collect(Collectors.toCollection(LinkedList::new));
	}

}
