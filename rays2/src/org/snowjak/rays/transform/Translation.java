package org.snowjak.rays.transform;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.snowjak.rays.Ray;
import org.snowjak.rays.intersect.Intersectable;
import org.snowjak.rays.intersect.Intersection;

/**
 * Represents a translating {@link Transformer}.
 * 
 * @author snowjak88
 *
 */
public class Translation implements Transformer {

	private RealMatrix matrix, inverseMatrix;

	/**
	 * Create a new Translation with the given displacements along each axis.
	 * 
	 * @param translation
	 */
	public Translation(Vector3D translation) {

		this(translation.getX(), translation.getY(), translation.getZ());
	}

	/**
	 * Create a new Translation with the given displacements along each axis.
	 * 
	 * @param dX
	 * @param dY
	 * @param dZ
	 */
	public Translation(double dX, double dY, double dZ) {

		// @formatter:off
		matrix = new BlockRealMatrix(
				new double[][] { { 1d, 0d, 0d, dX }, { 0d, 1d, 0d, dY }, { 0d, 0d, 1d, dZ }, { 0d, 0d, 0d, 1d } });
		inverseMatrix = new BlockRealMatrix(
				new double[][] { { 1d, 0d, 0d, -dX }, { 0d, 1d, 0d, -dY }, { 0d, 0d, 1d, -dZ }, { 0d, 0d, 0d, 1d } });
		// @formatter:on
	}

	@Override
	public Ray localToWorld(Ray ray) {

		return new Ray(localToWorld(ray.getOrigin()), ray.getVector(), ray.getRecursiveLevel());
	}

	@Override
	public Ray worldToLocal(Ray ray) {

		return new Ray(worldToLocal(ray.getOrigin()), ray.getVector(), ray.getRecursiveLevel());
	}

	@Override
	public <S extends Intersectable> Intersection<S> localToWorld(Intersection<S> intersection) {

		return new Intersection<S>(localToWorld(intersection.getPoint()), intersection.getNormal(),
				localToWorld(intersection.getRay()), intersection.getIntersected(),
				intersection.getDiffuseColorScheme(), intersection.getSpecularColorScheme(),
				intersection.getEmissiveColorScheme(), intersection.getLeavingMaterial(),
				intersection.getEnteringMaterial());
	}

	@Override
	public <S extends Intersectable> Intersection<S> worldToLocal(Intersection<S> intersection) {

		return new Intersection<S>(worldToLocal(intersection.getPoint()), intersection.getNormal(),
				worldToLocal(intersection.getRay()), intersection.getIntersected(),
				intersection.getDiffuseColorScheme(), intersection.getSpecularColorScheme(),
				intersection.getEmissiveColorScheme(), intersection.getLeavingMaterial(),
				intersection.getEnteringMaterial());
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
