package org.snowjak.rays.shape.csg;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.world.World;

/**
 * Implements the CSG-operation "intersection".
 * 
 * @author snowak
 *
 */
public class Intersect extends Shape {

	private Collection<Shape> children = new LinkedList<>();

	/**
	 * Construct a new (empty) Intersection.
	 */
	public Intersect() {
		this(Collections.emptyList());
	}

	/**
	 * Construct a new Intersection between the specified Shapes.
	 * 
	 * @param children
	 */
	public Intersect(Shape... children) {
		this(Arrays.asList(children));
	}

	/**
	 * Construct a new Intersection between the specified Shapes.
	 * 
	 * @param children
	 */
	public Intersect(Collection<Shape> children) {
		super();
		this.children.addAll(children);
		this.setDiffuseColorScheme((ColorScheme) null);
		this.setSpecularColorScheme((ColorScheme) null);
		this.setMaterial(null);
	}

	/**
	 * @return the list of child Shapes which are included in this Intersect
	 */
	public Collection<Shape> getChildren() {

		return children;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray, boolean includeBehindRayOrigin,
			boolean onlyIncludeClosest) {

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
				.map(s -> s.getIntersections(localRay, includeBehindRayOrigin))
				.flatMap(li -> li.stream())
				.filter(i -> Double.compare(i.getDistanceFromRayOrigin(), World.NEARLY_ZERO) >= 0)
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
		// children.parallelStream().filter(s ->
		// s.isInside(localRay.getOrigin())).forEach(s -> currentlyIn.add(s));

		List<Intersection<Shape>> results = new LinkedList<>();
		//
		int intersectCounter = 0;
		for (Intersection<Shape> currentIntersect : childIntersections) {

			intersectCounter++;
			Shape intersectedShape = currentIntersect.getIntersected();

			Material intersectOverrideMaterial = getMaterial();
			Material oldMaterial = null;
			Material newMaterial = null;

			if (intersectOverrideMaterial == null) {

				//
				// This Intersect does not have a Material of its own.
				// Therefore, each Intersection marks a boundary between two
				// blends, each fading from one Material to another.
				if (intersectCounter < childIntersections.size()) {
					Material nextMaterial = childIntersections.get(intersectCounter).getIntersected().getMaterial();
					Vector3D nextPoint = childIntersections.get(intersectCounter).getPoint();
					newMaterial = Material.blend(currentIntersect.getIntersected().getMaterial(),
							currentIntersect.getPoint(), nextMaterial, nextPoint);
				} else {
					newMaterial = Material.AIR;
				}

				if (intersectCounter > 1) {
					Material previousMaterial = childIntersections.get(intersectCounter - 2)
							.getIntersected()
							.getMaterial();
					Vector3D previousPoint = childIntersections.get(intersectCounter - 2).getPoint();
					oldMaterial = Material.blend(previousMaterial, previousPoint,
							currentIntersect.getIntersected().getMaterial(), currentIntersect.getPoint());
				} else {
					oldMaterial = Material.AIR;
				}

			} else {
				oldMaterial = intersectOverrideMaterial;
				newMaterial = intersectOverrideMaterial;
			}

			//
			//
			//

			//
			// We're crossing a boundary. Are we currently inside and coming
			// out?
			if (currentlyIn.contains(intersectedShape)) {

				//
				//
				if (currentlyIn.containsAll(children)) {
					currentIntersect.setLeavingMaterial(oldMaterial);
					currentIntersect.setEnteringMaterial(Material.AIR);
					results.add(currentIntersect);
				}

				currentlyIn.remove(intersectedShape);

			} else {

				//
				//
				// We're crossing into a Shape.
				currentlyIn.add(intersectedShape);

				// If we are now inside all child-Shapes -- after
				// crossing into one of them -- then we're entering the
				// Intersect.
				if (currentlyIn.containsAll(children)) {
					currentIntersect.setLeavingMaterial(Material.AIR);
					currentIntersect.setEnteringMaterial(newMaterial);
					results.add(currentIntersect);
				}
			}
		}

		//
		//
		// Finally, we need to convert each result Intersection so that
		// the reported intersected-Shape is this Union, not the child Shape.
		return results.stream()
				.sequential()
				.limit(onlyIncludeClosest ? 1 : results.size())
				.filter(i -> Double.compare(FastMath.abs(i.getDistanceFromRayOrigin()), World.NEARLY_ZERO) >= 0)
				.peek(i -> {
					//
					// Has this Intersect been given its own definitive
					// ColorSchemes,
					// which will override those of its children?
					ColorScheme diffuse = (this.getDiffuseColorScheme() != null) ? this.getDiffuseColorScheme()
							: i.getDiffuseColorScheme();
					ColorScheme specular = (this.getSpecularColorScheme() != null) ? this.getSpecularColorScheme()
							: i.getSpecularColorScheme();
					Optional<ColorScheme> emissive = (this.isEmissive()) ? this.getEmissiveColorScheme()
							: i.getEmissiveColorScheme();

					i.setIntersected(this);
					i.setDiffuseColorScheme(diffuse);
					i.setSpecularColorScheme(specular);
					i.setEmissiveColorScheme(emissive);
				})
				.map(i -> localToWorld(i))
				.collect(Collectors.toCollection(LinkedList::new));

	}

	@Override
	public Intersect copy() {

		Intersect newIntersect = new Intersect(
				this.children.stream().map(s -> s.copy()).collect(Collectors.toCollection(LinkedList::new)));
		newIntersect = configureCopy(newIntersect);

		return newIntersect;
	}

	@Override
	public Vector3D getNormalRelativeTo(Vector3D localPoint) {

		return children.parallelStream()
				.map(s -> s.getIntersections(new Ray(localPoint, s.getLocation().subtract(localPoint).normalize())))
				.flatMap(li -> li.stream())
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.findFirst()
				.get()
				.getNormal();
	}

	@Override
	public boolean isInside(Vector3D point) {

		return children.parallelStream().allMatch(s -> s.isInside(worldToLocal(point)));
	}

	@Override
	public Vector3D selectPointWithin(boolean selectSurfaceOnly) {

		Vector3D result;
		do {
			result = children.parallelStream().map(s -> s.selectPointWithin(selectSurfaceOnly)).reduce(Vector3D.ZERO,
					(v1, v2) -> v1.add(v2).scalarMultiply(0.5));
		} while (!isInside(result));

		return localToWorld(result);
	}
}
