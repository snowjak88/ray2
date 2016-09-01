package org.snowjak.rays.transform;

import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.toRadians;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.snowjak.rays.Ray;
import org.snowjak.rays.intersect.Intersectable;
import org.snowjak.rays.intersect.Intersection;

/**
 * Represents a rotation through the X, Y, and/or Z axes.
 * 
 * @author rr247200
 *
 */
public class Rotation implements Transformer {

	private RealMatrix matrix, inverseMatrix;

	/**
	 * Create a new Rotation using the given set of angles. Each angle
	 * represents a rotation about the associated axis.
	 * 
	 * @param angles
	 */
	public Rotation(Vector3D angles) {

		this(angles.getX(), angles.getY(), angles.getZ());
	}

	/**
	 * Create a new Rotation with the given set of angles.
	 * 
	 * @param pitch
	 *            about the X axis
	 * @param yaw
	 *            about the Y axis
	 * @param roll
	 *            about the Z axis
	 */
	public Rotation(double pitch, double yaw, double roll) {

		double xR = toRadians(pitch);
		double yR = toRadians(yaw);
		double zR = toRadians(roll);

		//@formatter:off
		RealMatrix xRotation = new BlockRealMatrix(new double[][]
				{	{ 1d, 0d, 0d, 0d },
					{ 0d, cos(xR), sin(xR), 0d },
					{ 0d, -sin(xR), cos(xR), 0d },
					{ 0d, 0d, 0d, 1d }	});
		RealMatrix yRotation = new BlockRealMatrix(new double[][]
				{	{ cos(yR), 0d, -sin(yR), 0d },
					{ 0d, 1d, 0d, 0d },
					{ sin(yR), 0d, cos(yR), 0d },
					{ 0d, 0d, 0d, 1d }	});
		RealMatrix zRotation = new BlockRealMatrix(new double[][]
				{	{ cos(zR), sin(zR), 0d, 0d },
					{ -sin(zR), cos(zR), 0d, 0d },
					{ 0d, 0d, 1d, 0d },
					{ 0d, 0d, 0d, 1d }	});
		//@formatter:on
		this.matrix = xRotation.multiply(yRotation).multiply(zRotation);
		this.inverseMatrix = matrix.transpose();
	}

	@Override
	public Ray localToWorld(Ray ray) {

		return new Ray(localToWorld(ray.getOrigin()), localToWorld(ray.getVector()), ray.getRecursiveLevel());
	}

	@Override
	public Ray worldToLocal(Ray ray) {

		return new Ray(worldToLocal(ray.getOrigin()), worldToLocal(ray.getVector()), ray.getRecursiveLevel());
	}

	@Override
	public <S extends Intersectable> Intersection<S> localToWorld(Intersection<S> intersection) {

		return new Intersection<S>(localToWorld(intersection.getPoint()), localToWorld(intersection.getNormal()),
				localToWorld(intersection.getRay()), intersection.getIntersected());
	}

	@Override
	public <S extends Intersectable> Intersection<S> worldToLocal(Intersection<S> intersection) {

		return new Intersection<S>(worldToLocal(intersection.getPoint()), worldToLocal(intersection.getNormal()),
				worldToLocal(intersection.getRay()), intersection.getIntersected());
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
