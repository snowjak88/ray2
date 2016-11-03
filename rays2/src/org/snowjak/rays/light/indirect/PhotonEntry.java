package org.snowjak.rays.light.indirect;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.util.KdTree;
import org.snowjak.rays.util.KdTree.HasCoordinates;

/**
 * Describes the entries to be stored in the photon-map.
 * 
 * @author snowjak88
 *
 */
public class PhotonEntry implements KdTree.HasCoordinates<Double> {

	private Vector3D intersectPoint, arrivalFromDirection;

	private RawColor color;

	/**
	 * Create a new photon-map entry.
	 * 
	 * @param intersectPoint
	 * @param arrivalFromDirection
	 * @param color
	 */
	public PhotonEntry(Vector3D intersectPoint, Vector3D arrivalFromDirection, RawColor color) {
		this.intersectPoint = intersectPoint;
		this.arrivalFromDirection = arrivalFromDirection;
		this.color = color;
	}

	/**
	 * @return this photon's intersect-point
	 */
	public Vector3D getIntersectPoint() {

		return intersectPoint;
	}

	/**
	 * @return the direction from which this photon arrived at its
	 *         intersect-point
	 */
	public Vector3D getArrivalFromDirection() {

		return arrivalFromDirection;
	}

	/**
	 * @return this photon's color (i.e., radiance)
	 */
	public RawColor getColor() {

		return color;
	}

	@Override
	public List<Double> getCoordinates() {

		return Arrays.asList(intersectPoint.getX(), intersectPoint.getY(), intersectPoint.getZ());
	}

	@Override
	public double getDistance(HasCoordinates<Double> other) {

		if (other.getDimensionality() != 3)
			throw new ArrayIndexOutOfBoundsException(
					"Dimensionality mismatch between 3d and " + other.getDimensionality() + "d photon-map entries!");

		return this.intersectPoint
				.distance(new Vector3D(other.getCoordinate(0), other.getCoordinate(1), other.getCoordinate(2)));
	}

}
