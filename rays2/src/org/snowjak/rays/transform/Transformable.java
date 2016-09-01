package org.snowjak.rays.transform;

import java.util.Deque;
import java.util.Iterator;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.World;
import org.snowjak.rays.intersect.Intersectable;
import org.snowjak.rays.intersect.Intersection;

/**
 * Indicates that something can be transformed by one or more
 * {@link Transformer}s. Allows you to transform local coordinates to world
 * coordinates, and vice versa.
 * 
 * @author rr247200
 *
 */
public interface Transformable {

	/**
	 * @return this object's set of {@link Transformer}s
	 */
	public Deque<Transformer> getTransformers();

	/**
	 * Transform the provided object-local coordinates into world coordinates.
	 * Accomplished by running the given local coordinates through the set of
	 * {@link Transformer}s in natural order.
	 * 
	 * @param localCoords
	 * @return the given local coordinates, translated into world coordinates
	 */
	public default Vector3D localToWorld(Vector3D localCoords) {

		Vector3D temporary = new Vector3D(localCoords.getX(), localCoords.getY(), localCoords.getZ());
		for (Transformer transformer : getTransformers())
			temporary = transformer.localToWorld(temporary);

		return temporary;
	}

	/**
	 * Transform the provided {@link Ray} from object-local coordinates into
	 * world coordinates.
	 * 
	 * @param ray
	 * @return the given Ray, translated into world coordinates
	 */
	public default Ray localToWorld(Ray ray) {

		Ray temporary = ray;
		for (Transformer transformer : getTransformers())
			temporary = transformer.localToWorld(temporary);

		return temporary;
	}

	/**
	 * Transform the provided {@link Intersection} from object-local coordinates
	 * into world coordinates.
	 * 
	 * @param intersection
	 * @return the given Intersection, translated into world coordinates
	 */
	public default

	<S extends Intersectable> Intersection<S> localToWorld(Intersection<S> intersection) {

		Intersection<S> temporary = intersection;
		for (Transformer transformer : getTransformers())
			temporary = transformer.localToWorld(temporary);
		return temporary;
	}

	/**
	 * Transform the provided world coordinates into object-local coordinates.
	 * Accomplished by running the given world coordinates through the set of
	 * {@link Transformer}s in reverse order.
	 * 
	 * @param worldCoords
	 * @return the given world coordinates, translated into object-local
	 *         coordinates
	 */
	public default Vector3D worldToLocal(Vector3D worldCoords) {

		Vector3D temporary = new Vector3D(worldCoords.getX(), worldCoords.getY(), worldCoords.getZ());
		Iterator<Transformer> iterator = getTransformers().descendingIterator();
		while (iterator.hasNext())
			temporary = iterator.next().worldToLocal(temporary);

		return temporary;
	}

	/**
	 * Transform the provided {@link Ray} from world coordinates into
	 * object-local coordinates.
	 * 
	 * @param ray
	 * @return the given Ray, translated into object-local coordinates
	 */
	public default Ray worldToLocal(Ray ray) {

		Ray temporary = ray;
		Iterator<Transformer> iterator = getTransformers().descendingIterator();
		while (iterator.hasNext())
			temporary = iterator.next().worldToLocal(temporary);

		return temporary;
	}

	/**
	 * Transform the provided {@link Intersection} from world coordinates into
	 * object-local coordinates.
	 * 
	 * @param intersection
	 * @return the given Intersection, translated into object-local coordinates
	 */
	public default <S extends Intersectable> Intersection<S> worldToLocal(Intersection<S> intersection) {

		Intersection<S> temporary = intersection;
		Iterator<Transformer> iterator = getTransformers().descendingIterator();
		while (iterator.hasNext())
			temporary = iterator.next().worldToLocal(temporary);

		return temporary;
	}

	/**
	 * Transform the provided camera coordinates to global coordinates.
	 * 
	 * @param cameraCoords
	 * @return the given camera-centric coordinates translated to global
	 *         coordinates
	 */
	public default Vector3D cameraToWorld(Vector3D cameraCoords) {

		return World.getSingleton().getCamera().localToWorld(cameraCoords);
	}

	/**
	 * Transform the provided camera-based Ray to a global Ray.
	 * 
	 * @param cameraRay
	 * @return the given camera-centric Ray translated to a global Ray
	 */
	public default Ray cameraToWorld(Ray cameraRay) {

		return World.getSingleton().getCamera().localToWorld(cameraRay);
	}

	/**
	 * Transform the provided camera-based {@link Intersection} to a global
	 * Intersection.
	 * 
	 * @param intersection
	 * @return the given camera-centric Intersection translated to a global
	 *         Intersection
	 */
	public default <S extends Intersectable> Intersection<S> cameraToWorld(Intersection<S> intersection) {

		return World.getSingleton().getCamera().localToWorld(intersection);
	}

	/**
	 * Transform the provided global coordinates to camera coordinates.
	 * 
	 * @param worldCoords
	 * @return the given global coordinates translated to camera coordinates
	 */
	public default Vector3D worldToCamera(Vector3D worldCoords) {

		return World.getSingleton().getCamera().worldToLocal(worldCoords);
	}

	/**
	 * Transform the provided global Ray to a camera-centric Ray.
	 * 
	 * @param worldRay
	 * @return the given global Ray translated to a camera-centric Ray
	 */
	public default Ray worldToCamera(Ray worldRay) {

		return World.getSingleton().getCamera().worldToLocal(worldRay);
	}

	/**
	 * Transform the provided global {@link Intersection} to a camera-centric
	 * Intersection.
	 * 
	 * @param intersection
	 * @return the given global Intersection translated to a camera-centric
	 *         Intersection
	 */
	public default <S extends Intersectable> Intersection<S> worldToCamera(Intersection<S> intersection) {

		return World.getSingleton().getCamera().worldToLocal(intersection);
	}

	/**
	 * Transform the provided camera coordinates to local coordinates.
	 * 
	 * @param cameraCoords
	 * @return the given camera-centric coordinates translated to local
	 *         coordinates
	 */
	public default Vector3D cameraToLocal(Vector3D cameraCoords) {

		return worldToLocal(cameraToWorld(cameraCoords));
	}

	/**
	 * Transform the provided camera-based Ray to a local Ray.
	 * 
	 * @param cameraRay
	 * @return the given camera-centric Ray translated to a local Ray
	 */
	public default Ray cameraToLocal(Ray cameraRay) {

		return worldToLocal(cameraToWorld(cameraRay));
	}

	/**
	 * Transform the provided camera-based {@link Intersection} to a local
	 * Intersection.
	 * 
	 * @param intersection
	 * @return the given camera-centric Intersection translated to a local
	 *         Intersection
	 */
	public default <S extends Intersectable> Intersection<S> cameraToLocal(Intersection<S> intersection) {

		return worldToLocal(cameraToWorld(intersection));
	}

	/**
	 * Transform the provided local coordinates to camera coordinates.
	 * 
	 * @param localCoords
	 * @return the given local coordinates translated to camera coordinates
	 */
	public default Vector3D localToCamera(Vector3D localCoords) {

		return worldToCamera(localToWorld(localCoords));
	}

	/**
	 * Transform the provided local Ray to a camera-centric Ray.
	 * 
	 * @param localRay
	 * @return the given local Ray translated to a camera-centric Ray
	 */
	public default Ray localToCamera(Ray localRay) {

		return worldToCamera(localToWorld(localRay));
	}

	/**
	 * Transform the provided local {@link Intersection} to a camera-centric
	 * Intersection.
	 * 
	 * @param intersection
	 * @return the given local Intersection translated to a camera-centric
	 *         Intersection
	 */
	public default Intersection<? extends Intersectable> localToCamera(
			Intersection<? extends Intersectable> intersection) {

		return worldToCamera(localToWorld(intersection));
	}
}
