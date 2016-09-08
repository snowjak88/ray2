package org.snowjak.rays.shape.perturb;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.Shape;

public class NormalPerturber extends Shape {

	private Function<Vector3D, Vector3D> normalPerturbationFunction = (v) -> v;

	private Shape child;

	public NormalPerturber(Function<Vector3D, Vector3D> normalPerturbationFunction, Shape child) {
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

		return child.getIntersectionsIncludingBehind(ray)
				.parallelStream()
				.map(i -> new Intersection<>(i.getPoint(), normalPerturbationFunction.apply(i.getNormal()), i.getRay(),
						i.getIntersected(), i.getDistanceFromRayOrigin(), i.getAmbientColorScheme(),
						i.getDiffuseColorScheme(), i.getSpecularColorScheme(), i.getEmissiveColorScheme()))
				.collect(Collectors.toCollection(LinkedList::new));
	}

	@Override
	public Shape copy() {

		NormalPerturber perturber = new NormalPerturber(normalPerturbationFunction, child.copy());
		return configureCopy(perturber);
	}

}
