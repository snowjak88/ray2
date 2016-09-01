package org.snowjak.rays.transform;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.snowjak.rays.Ray;
import org.snowjak.rays.intersect.Intersectable;
import org.snowjak.rays.intersect.Intersection;

/**
 * Represents a transformation of a set of coordinates from an object-local
 * reference-frame to a global reference-frame, and vice versa.
 * 
 * @author rr247200
 *
 */
public interface Transformer {

	/**
	 * Translate the given object-local coordinates into world coordinates.
	 * 
	 * @param localPoint
	 * @return the given object-local coordinates, translated into world
	 *         coordinates
	 */
	public default Vector3D localToWorld(Vector3D localPoint) {

		RealVector transformed = getLocalToWorldMatrix().operate(
				new ArrayRealVector(new double[] { localPoint.getX(), localPoint.getY(), localPoint.getZ(), 1d }));
		return new Vector3D(transformed.getEntry(0) / transformed.getEntry(3),
				transformed.getEntry(1) / transformed.getEntry(3), transformed.getEntry(2) / transformed.getEntry(3));
	}

	/**
	 * Translate the given {@link Ray} from an object-local reference-frame to a
	 * global reference-frame.
	 * 
	 * @param ray
	 * @return the given Ray translated to a global reference-frame
	 */
	public Ray localToWorld(Ray ray);

	/**
	 * Translate the given {@link Intersection} from an object-local
	 * reference-frame to a global reference-frame.
	 * 
	 * @param intersection
	 * @return the given Intersection translated to a global reference-frame
	 */
	public <S extends Intersectable> Intersection<S> localToWorld(Intersection<S> intersection);

	/**
	 * Translate the given world coordinates into object-local coordinates.
	 * 
	 * @param worldPoint
	 * @return the given world coordinates, translated into object-local
	 *         coordinates
	 */
	public default Vector3D worldToLocal(Vector3D worldPoint) {

		RealVector transformed = getWorldToLocalMatrix().operate(
				new ArrayRealVector(new double[] { worldPoint.getX(), worldPoint.getY(), worldPoint.getZ(), 1d }));
		return new Vector3D(transformed.getEntry(0) / transformed.getEntry(3),
				transformed.getEntry(1) / transformed.getEntry(3), transformed.getEntry(2) / transformed.getEntry(3));
	}

	/**
	 * Translate the given {@link Ray} from a local reference-frame to an
	 * object-local reference-frame.
	 * 
	 * @param ray
	 * @return the given Ray translated to an object-local reference-frame
	 */
	public Ray worldToLocal(Ray ray);

	/**
	 * Translate the given {@link Intersection} from a local reference-frame to
	 * an object-local reference-frame.
	 * 
	 * @param intersection
	 * @return the given Intersection translated to an object-local
	 *         reference-frame
	 */
	public <S extends Intersectable> Intersection<S> worldToLocal(Intersection<S> intersection);

	/**
	 * @return the 4x4 matrix used to transform world- to local-coordinates
	 */
	public RealMatrix getWorldToLocalMatrix();

	/**
	 * @return the 4x4 matrix used to transform local- to world-coordinates
	 */
	public RealMatrix getLocalToWorldMatrix();
}
