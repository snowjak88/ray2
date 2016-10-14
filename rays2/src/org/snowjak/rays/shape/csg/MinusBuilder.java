package org.snowjak.rays.shape.csg;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.snowjak.rays.shape.NullShape;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.shape.ShapeBuilder;

/**
 * A convenient interface for building {@link Minus} instances
 * 
 * @author snowjak88
 *
 */
public class MinusBuilder extends ShapeBuilder<Minus> {

	private Shape minuend = new NullShape();

	private Set<Shape> subtrahends = new HashSet<>();

	/**
	 * @return a new MinusBuilder instance
	 */
	public MinusBuilder builder() {

		return new MinusBuilder();
	}

	protected MinusBuilder() {

	}

	/**
	 * Set the "minuend" of this in-progress Minus.
	 * 
	 * @param minuend
	 * @see Minus#setMinuend(Shape)
	 * @return this Builder, for method-chaining
	 */
	public MinusBuilder minuend(Shape minuend) {

		this.minuend = minuend;
		return this;
	}

	/**
	 * Add a "subtrahend" shape to this in-progress Minus
	 * 
	 * @param subtrahend
	 * @see Minus#getSubtrahends()
	 * @return this Builder, for method-chaining
	 */
	public MinusBuilder subtrahend(Shape subtrahend) {

		this.subtrahends.add(subtrahend);
		return this;
	}

	@Override
	protected Minus createNewShapeInstance() {

		return new Minus(new NullShape(), Collections.emptyList());
	}

	@Override
	protected Minus performTypeSpecificInitialization(Minus newShapeInstance) {

		newShapeInstance.setMinuend(minuend);
		newShapeInstance.getSubtrahends().addAll(subtrahends);

		return newShapeInstance;
	}

}
