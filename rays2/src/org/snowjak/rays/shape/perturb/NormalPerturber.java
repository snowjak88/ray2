package org.snowjak.rays.shape.perturb;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

/**
 * A decorator Shape that enhances another Shape with normal-perturbation --
 * i.e., bump-mapping. This is an uncomplicated way to add "bumpiness" to
 * objects.
 * <p>
 * This Shape encapsulates a child Shape. When intersections are detected the
 * child-Shape, this NormalPerturber updates the each intersection's
 * normal-vector with the results of its <strong>normal perturbation
 * function</strong>.
 * </p>
 * <p>
 * This normal perturbation function is of the form:
 * 
 * <pre>
 *   original-normal, original-intersection --> updated-normal
 * </pre>
 * 
 * where {@code original-normal} and {@code original-intersection} are expressed
 * in global coordinates (relative to the NormalPerturber instance).
 * 
 * See {@link #DEFAULT_PERTURBATION_FUNCTION}
 * </p>
 * 
 * @author snowjak88
 *
 */
public class NormalPerturber extends Shape {

	/**
	 * The default perturbation function. Implements {@code (v, i) -> v} --
	 * i.e., leaves the original normal unchanged.
	 */
	public static final BiFunction<Vector3D, Intersection<Shape>, Vector3D> DEFAULT_PERTURBATION_FUNCTION = (v, i) -> v;

	private BiFunction<Vector3D, Intersection<Shape>, Vector3D> normalPerturbationFunction = DEFAULT_PERTURBATION_FUNCTION;

	private Shape child;

	/**
	 * Create a new {@link NormalPerturber} with the specified perturbation
	 * function.
	 * 
	 * @param normalPerturbationFunction
	 * @param child
	 */
	public NormalPerturber(BiFunction<Vector3D, Intersection<Shape>, Vector3D> normalPerturbationFunction,
			Shape child) {
		super();
		this.normalPerturbationFunction = normalPerturbationFunction;
		this.child = child;
	}

	@Override
	public Vector3D getNormalRelativeTo(Vector3D localPoint) {

		return child.getNormalRelativeTo(localPoint);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersectionsIncludingBehind(Ray ray) {

		return child.getIntersectionsIncludingBehind(worldToLocal(ray))
				.parallelStream()
				.map(i -> localToWorld(i))
				.map(i -> new Intersection<>(i.getPoint(), normalPerturbationFunction.apply(i.getNormal(), i),
						i.getRay(), i.getIntersected(), i.getDistanceFromRayOrigin(), i.getDiffuseColorScheme(),
						i.getSpecularColorScheme(), i.getEmissiveColorScheme(), i.getLeavingMaterial(),
						i.getEnteringMaterial()))
				.collect(Collectors.toCollection(LinkedList::new));
	}

	@Override
	public NormalPerturber copy() {

		NormalPerturber perturber = new NormalPerturber(normalPerturbationFunction, child.copy());
		return configureCopy(perturber);
	}

}
