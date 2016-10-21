package org.snowjak.rays.shape;

import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Ray;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.intersect.Intersection;
import org.snowjak.rays.transform.Transformer;

/**
 * A "null shape" is nothing. It exists nowhere and is invisible. While you can
 * assign it {@link ColorScheme}s and {@link Transformer}s and things, these
 * assignments have no effect.
 * 
 * @author snowjak88
 *
 */
public class NullShape extends Shape {

	@Override
	public Vector3D getNormalRelativeTo(Vector3D localPoint) {

		return Vector3D.PLUS_J;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Intersection<Shape>> getIntersections(Ray ray, boolean includeBehindRayOrigin,
			boolean onlyReturnClosest) {

		return Collections.emptyList();
	}

	@Override
	public Shape copy() {

		return new NullShape();
	}

	@Override
	public Vector3D selectPointWithin() {
		return selectPointWithin(true);
	}

	@Override
	public Vector3D selectPointWithin(boolean selectSurfaceOnly) {

		return localToWorld(Vector3D.ZERO);
	}

}
