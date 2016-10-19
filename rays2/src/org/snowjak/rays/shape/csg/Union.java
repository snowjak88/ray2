package org.snowjak.rays.shape.csg;

import java.util.Arrays;
import java.util.Collection;
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
import org.snowjak.rays.shape.Group;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.world.World;

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
		setDiffuseColorScheme((ColorScheme) null);
		setSpecularColorScheme((ColorScheme) null);
		setEmissiveColorScheme((ColorScheme) null);
		setMaterial(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray, boolean includeBehindRayOrigin,
			boolean onlyIncludeClosest) {

		Ray transformedRay = worldToLocal(ray);

		//
		// Get the intersections reported by each child Shape,
		// flatten that list of lists into a single list of intersections,
		// and sort it by distance.
		List<Intersection<Shape>> intersections = children.parallelStream()
				.map(s -> s.getIntersections(transformedRay, includeBehindRayOrigin))
				.flatMap(l -> l.parallelStream())
				.map(i -> localToWorld(i))
				.sorted((i1, i2) -> Double.compare(i1.getDistanceFromRayOrigin(), i2.getDistanceFromRayOrigin()))
				.collect(LinkedList::new, LinkedList::add, LinkedList::addAll);

		if (intersections.isEmpty())
			return (List<Intersection<Shape>>) intersections;

		//
		//
		//
		Material unionOverrideMaterial = getMaterial();
		//
		//
		//
		List<Intersection<Shape>> results = new LinkedList<>();
		Set<Shape> currentlyIn = new HashSet<>();
		int intersectCounter = 0;
		for (Intersection<Shape> currentIntersect : intersections) {

			intersectCounter++;
			//
			//
			// We want to ensure that each Material can blend smoothly with the
			// next Material on this ray-path.
			// So -- will this Intersection's Material blend into another?
			Material newMaterial = null;
			Material oldMaterial = null;
			if (unionOverrideMaterial == null) {
				//
				// This Union does not have a Material of its own.
				// Therefore, this Intersection marks the start of a blend from
				// one Material to another.
				//
				if (intersectCounter < intersections.size()) {
					Material nextMaterial = intersections.get(intersectCounter).getIntersected().getMaterial();
					Vector3D nextPoint = intersections.get(intersectCounter).getPoint();
					newMaterial = Material.blend(currentIntersect.getIntersected().getMaterial(),
							currentIntersect.getPoint(), nextMaterial, nextPoint);
				} else {
					newMaterial = Material.AIR;
				}

				if (intersectCounter > 1) {
					Material previousMaterial = intersections.get(intersectCounter - 2).getIntersected().getMaterial();
					Vector3D previousPoint = intersections.get(intersectCounter - 2).getPoint();
					oldMaterial = Material.blend(previousMaterial, previousPoint,
							currentIntersect.getIntersected().getMaterial(), currentIntersect.getPoint());
				} else {
					oldMaterial = Material.AIR;
				}
			} else {
				//
				// Because this Union has a Material of its own, that Material
				// will override other Materials.
				newMaterial = getMaterial();
				oldMaterial = getMaterial();
			}

			//
			// Each Intersection marks the point at which we cross a shape
			// boundary.
			// With the current intersection, are we entering or leaving the
			// reported shape?
			if (currentlyIn.contains(currentIntersect.getIntersected())) {
				//
				// Leaving a shape!
				currentlyIn.remove(currentIntersect.getIntersected());
				//
				// Now -- only if this Union has an overriding Material do we
				// care about culling interior Intersections.
				//
				if (unionOverrideMaterial != null) {
					//
					// Since we're culling interior Intersections -- is this
					// Intersection completely interior? Are we still inside of
					// other child-shapes?
					if (!currentlyIn.isEmpty()) {
						// No! We're transitioning out of all child-shapes!
						// Record this Intersection
						currentIntersect.setLeavingMaterial(oldMaterial);
						currentIntersect.setEnteringMaterial(newMaterial);
						results.add(currentIntersect);
					}
				} else {
					//
					// We are *not* culling interior Intersections.
					// So add this Intersection to the list!
					currentIntersect.setLeavingMaterial(oldMaterial);
					currentIntersect.setEnteringMaterial(newMaterial);
					results.add(currentIntersect);
				}

			} else {
				//
				// Entering a shape!
				//
				// Are we culling interior Intersections?
				if (unionOverrideMaterial != null) {
					//
					// Yes!
					// Is this intersection interior?
					if (currentlyIn.isEmpty()) {
						// This intersection is *not* interior.
						// So add it to the list!
						currentIntersect.setLeavingMaterial(oldMaterial);
						currentIntersect.setEnteringMaterial(newMaterial);
						results.add(currentIntersect);
					}
				} else {
					//
					// We're not culling interior Intersections.
					// So add this to the list.
					currentIntersect.setLeavingMaterial(oldMaterial);
					currentIntersect.setEnteringMaterial(newMaterial);
					results.add(currentIntersect);
				}

				currentlyIn.add(currentIntersect.getIntersected());
			}

		}

		//
		//

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
					// Has this Union been given its own definitive
					// ColorSchemes, which
					// will override those of its children?
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
				.collect(LinkedList::new, LinkedList::add, LinkedList::addAll);
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
				.findFirst()
				.get()
				.getNormal();
	}
}
