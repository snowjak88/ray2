package org.snowjak.rays.function.builder;

import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.builder.Builder;
import org.snowjak.rays.color.RawColor;

/**
 * Denotes that a Builder specializes in color-functions -- i.e.,
 * {@link Function}s of the form
 * 
 * <pre>
 *   Function<Vector3D, RawColor>
 * </pre>
 * 
 * @author snowjak88
 *
 */
public interface ColorFunctionBuilder extends Builder<Function<Vector3D, RawColor>> {

}
