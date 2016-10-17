package org.snowjak.rays.light.model;

import static org.apache.commons.math3.util.FastMath.floor;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.light.model.EnvironmentMapDecoratingLightingModel.EnvironmentMap;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

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
	public Vector2D convert(Vector3D v) {

		// double m = 2d * sqrt(pow(v.getX(), 2d) + pow(v.getY(), 2d) +
		// pow(v.getZ() + 1d, 2d));
		// return new Vector2D(v.getX() / m + 0.5, v.getY() / m + 0.5);

		double u = FastMath.atan2(v.getZ(), v.getX()) / (2d * FastMath.PI) + 0.5;

		return new Vector2D(u, v.getY() * 0.5 + 0.5);
	}

	@Override
	public RawColor getColorAt(double u, double v) {

		double width = image.getWidth() - 1d, height = image.getHeight() - 1d;

		int u0 = (int) (u * width), u1 = (u0 + 1) % ((int) width);
		int v0 = (int) (height - v * height), v1 = (v0 - 1 < 0 ? (int) height - 1 : v0 - 1);

		double fu = u - floor(u), fv = v - floor(v);

		PixelReader pixels = image.getPixelReader();
		RawColor color_v0 = new RawColor(pixels.getColor(u0, v0))
				.linearlyInterpolate(new RawColor(pixels.getColor(u1, v0)), fu);
		RawColor color_v1 = new RawColor(pixels.getColor(u0, v1))
				.linearlyInterpolate(new RawColor(pixels.getColor(u1, v1)), fu);

		return color_v0.linearlyInterpolate(color_v1, fv);
	}

}
