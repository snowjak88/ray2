package org.snowjak.rays.shape.perturb;

import java.util.function.BiFunction;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.shape.NullShape;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.shape.ShapeBuilder;

/**
 * A convenient interface for building {@link NormalPerturber} instances
 * 
 * @author snowjak88
 *
 */
public class NormalPerturberBuilder extends ShapeBuilder<NormalPerturber> {

	private Shape perturbedShape = new NullShape();

	private BiFunction<Vector3D, Intersection<Shape>, Vector3D> normalPerturbationFunction = NormalPerturber.DEFAULT_PERTURBATION_FUNCTION;

	/**
	 * @return a new NormalPerturberBuilder instance
	 */
	public NormalPerturberBuilder builder() {

		return new NormalPerturberBuilder();
	}

	protected NormalPerturberBuilder() {

	}

	/**
	 * Set the Shape whose normals should be perturbed
	 * 
	 * @param perturbedShape
	 * @return this Builder, for method-chaining
	 */
	public NormalPerturberBuilder perturb(Shape perturbedShape) {

		this.perturbedShape = perturbedShape;
		return this;
	}

	/**
	 * Set the {@link BiFunction} to be used when perturbing normals.
	 * 
	 * @param normalPerturbationFunction
	 * @return this Builder, for method-chaining
	 */
	public NormalPerturberBuilder function(
			BiFunction<Vector3D, Intersection<Shape>, Vector3D> normalPerturbationFunction) {

		this.normalPerturbationFunction = normalPerturbationFunction;
		return this;
	}

	@Override
	protected NormalPerturber createNewShapeInstance() {

		return new NormalPerturber(NormalPerturber.DEFAULT_PERTURBATION_FUNCTION, new NullShape());
	}

	@Override
	protected NormalPerturber performTypeSpecificInitialization(NormalPerturber newShapeInstance) {

		newShapeInstance.setChild(perturbedShape);
		newShapeInstance.setNormalPerturbationFunction(normalPerturbationFunction);

		return newShapeInstance;
	}

}
