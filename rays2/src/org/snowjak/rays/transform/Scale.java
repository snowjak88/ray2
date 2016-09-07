package org.snowjak.rays.transform;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.snowjak.rays.Ray;
import org.snowjak.rays.intersect.Intersectable;
import org.snowjak.rays.intersect.Intersection;

/**
 * Represents a scaling {@link Transformer}.
 * 
 * @author rr247200
 *
 */
public class Scale implements Transformer {

	private RealMatrix matrix, inverseMatrix;

	/**
	 * Create a new Scale with the given scaling-factors for each axis.
	 * 
	 * @param scale
	 */
	public Scale(Vector3D scale) {
		this(scale.getX(), scale.getY(), scale.getZ());
	}

	/**
	 * Create a new Scale with the given scaling-factors for each axis.
	 * 
	 * @param scaleX
	 * @param scaleY
	 * @param scaleZ
	 * 
	 */
	public Scale(double scaleX, double scaleY, double scaleZ) {
		// @formatter:off
		this.matrix = new BlockRealMatrix(new double[][] { { scaleX, 0d, 0d, 0d }, { 0d, scaleY, 0d, 0d },
				{ 0d, 0d, scaleZ, 0d }, { 0d, 0d, 0d, 1d } });
		this.inverseMatrix = new BlockRealMatrix(new double[][] { { 1d / scaleX, 0d, 0d, 0d },
				{ 0d, 1d / scaleY, 0d, 0d }, { 0d, 0d, 1d / scaleZ, 0d }, { 0d, 0d, 0d, 1d } });
		// @formatter:on
	}

	@Override
	public Ray localToWorld(Ray ray) {

		return new Ray(localToWorld(ray.getOrigin()), localToWorld(ray.getVector()),
				ray.getRecursiveLevel());
	}

	@Override
	public Ray worldToLocal(Ray ray) {

		return new Ray(worldToLocal(ray.getOrigin()), worldToLocal(ray.getVector()),
				ray.getRecursiveLevel());
	}

	@Override
	public <S extends Intersectable> Intersection<S> localToWorld(Intersection<S> intersection) {

		return new Intersection<S>(localToWorld(intersection.getPoint()),
				localToWorld(intersection.getNormal()).normalize(), localToWorld(intersection.getRay()),
				intersection.getIntersected(), intersection.getAmbientColorScheme(),
				intersection.getDiffuseColorScheme(), intersection.getSpecularColorScheme(),
				intersection.getEmissiveColorScheme());
	}

	@Override
	public <S extends Intersectable> Intersection<S> worldToLocal(Intersection<S> intersection) {

		return new Intersection<S>(worldToLocal(intersection.getPoint()),
				worldToLocal(intersection.getNormal()).normalize(), worldToLocal(intersection.getRay()),
				intersection.getIntersected(),
				intersection.getAmbientColorScheme(), intersection.getDiffuseColorScheme(),
				intersection.getSpecularColorScheme(), intersection.getEmissiveColorScheme());
	}

	@Override
	public RealMatrix getWorldToLocalMatrix() {

		return inverseMatrix;
	}

	@Override
	public RealMatrix getLocalToWorldMatrix() {

		return matrix;
	}

}
