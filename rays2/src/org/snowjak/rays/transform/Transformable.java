package org.snowjak.rays.transform;

import java.util.Deque;
import java.util.Iterator;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
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
}
