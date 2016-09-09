package org.snowjak.rays.light.model;

import static org.apache.commons.math3.util.FastMath.*;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.light.model.EnvironmentMapDecoratingLightingModel.EnvironmentMap;

import javafx.scene.image.Image;

/**
 * An implementation of {@link EnvironmentMap} that allows the use of
 * spherically-mapped images.
 * 
 * @author snowjak88
 *
 */
public class SphericalEnvironmentMap implements EnvironmentMap {

	private Image image;

	/**
	 * Construct a new {@link SphericalEnvironmentMap}, referencing the provided
	 * {@link Image} instance.
	 * 
	 * @param image
	 */
	public SphericalEnvironmentMap(Image image) {
		this.image = image;
	}

	@Override
	public Vector3D convert(Vector2D uv) {

		return new Vector3D(uv.getX(), uv.getY(), sqrt(1d - pow(uv.getX(), 2d) - pow(uv.getY(), 2d)));
	}

	@Override
	public Vector2D convert(Vector3D v) {

		double m = 2d * sqrt(pow(v.getX(), 2d) + pow(v.getY(), 2d) + pow(v.getZ(), 2d));
		return new Vector2D(v.getX() / m + 0.5, v.getY() / m + 0.5);
	}

	@Override
	public RawColor getColorAt(double u, double v) {

		int imageX = (int) (u * (image.getWidth() - 1d));
		int imageY = (int) (image.getHeight() - v * (image.getHeight()));

		return new RawColor(image.getPixelReader().getColor(imageX, imageY));
	}

}
