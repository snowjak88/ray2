package org.snowjak.rays.world.importfile;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.builder.Builder;

/**
 * The BuilderInvoker is responsible for identifying and invoking appropriate
 * {@link Builder}s, based on provided {@link WorldFileObjectDefinition}s.
 * 
 * @author snowjak88
 *
 */
public class BuilderInvoker {

	private static BuilderInvoker INSTANCE = null;

	/**
	 * @return the singleton BuilderInvoker instance
	 */
	public static BuilderInvoker getSingleton() {

		if (INSTANCE == null)
			INSTANCE = new BuilderInvoker();

		return INSTANCE;
	}

	protected BuilderInvoker() {

	}

	/**
	 * Given a {@link WorldFileObjectDefinition} (and any child
	 * WorldFileObjectDefinitions), invoke any and all necessary Builders to
	 * create the represented object-tree.
	 * 
	 * @param objectDefinition
	 * @return a tree of objects represented by the tree of
	 *         WorldFileObjectDefinitions, or nothing if the tree cannot be
	 *         created
	 */
	public Optional<Object> invokeBuilders(WorldFileObjectDefinition objectDefinition) {

		Optional<Object> result = Optional.empty();

		NamedBuilderRegistrar registrar = NamedBuilderRegistrar.getSingleton();
		Optional<? extends Builder<?>> builder = registrar.getBuilderByName(objectDefinition.getObjectName());

		if (builder.isPresent()) {

			for (Entry<String, Collection<String>> literals : objectDefinition.getAllLiteralValues().entrySet())
				for (String literal : literals.getValue())
					invokeLiteralMethod(builder.get(), new Pair<>(literals.getKey(), literal));

			for (Entry<String, Collection<WorldFileObjectDefinition>> childObjects : objectDefinition
					.getAllChildObjects().entrySet())
				for (WorldFileObjectDefinition childObject : childObjects.getValue())
					invokeChildObjectMethod(builder.get(), new Pair<>(childObjects.getKey(), childObject));

			result = Optional.of(builder.get().build());
		}

		return result;

	}

	private void invokeLiteralMethod(Builder<?> builder, Pair<String, String> literalEntry) {

		NamedBuilderRegistrar registrar = NamedBuilderRegistrar.getSingleton();

		@SuppressWarnings("unchecked")
		Class<? extends Builder<?>> builderClass = (Class<? extends Builder<?>>) builder.getClass();

		Optional<Method> builderMethod = registrar.getBuilderMethodByName(builderClass, literalEntry.getKey(),
				Optional.empty());
		if (builderMethod.isPresent()) {

			Class<?> parameterType = builderMethod.get().getParameterTypes()[0];
			try {
				if (String.class.isAssignableFrom(parameterType))
					builderMethod.get().invoke(builder, literalEntry.getValue());
				else if (Boolean.class.isAssignableFrom(parameterType) || boolean.class.isAssignableFrom(parameterType))
					builderMethod.get().invoke(builder, Boolean.parseBoolean(literalEntry.getValue()));
				else if (Integer.class.isAssignableFrom(parameterType) || int.class.isAssignableFrom(parameterType))
					builderMethod.get().invoke(builder, Integer.parseInt(literalEntry.getValue()));
				else if (Long.class.isAssignableFrom(parameterType) || long.class.isAssignableFrom(parameterType))
					builderMethod.get().invoke(builder, Long.parseLong(literalEntry.getValue()));
				else if (Float.class.isAssignableFrom(parameterType) || float.class.isAssignableFrom(parameterType))
					builderMethod.get().invoke(builder, Float.parseFloat(literalEntry.getValue()));
				else if (Double.class.isAssignableFrom(parameterType) || double.class.isAssignableFrom(parameterType))
					builderMethod.get().invoke(builder, Double.parseDouble(literalEntry.getValue()));

			} catch (Exception e) {
				System.err.println("\nUnable to populate field '" + literalEntry.getKey()
						+ "' on Builder -- unexpected exception!");
				System.err.println("Builder: '" + builderClass.getName() + "'");
				System.err.println("Parameter type: '" + parameterType.getName() + "'");
				System.err.println("Provided literal value: '" + literalEntry.getValue() + "'");
				System.err.println("Exception message: " + e.getMessage());
			}
		} else {
			System.err.println("\nUnable to populate literal value for Builder -- unknown field!");
			System.err.println("Builder: '" + builderClass.getName() + "'");
			System.err.println("Field-name: '" + literalEntry.getKey() + "'");
			return;
		}
	}

	private void invokeChildObjectMethod(Builder<?> builder, Pair<String, WorldFileObjectDefinition> childObjectEntry) {

		NamedBuilderRegistrar registrar = NamedBuilderRegistrar.getSingleton();
		@SuppressWarnings("unchecked")
		Class<? extends Builder<?>> builderClass = (Class<? extends Builder<?>>) builder.getClass();

		Optional<Builder<?>> childObjectBuilder = registrar
				.getBuilderByName(childObjectEntry.getValue().getObjectName());

		if (childObjectBuilder.isPresent()) {

			@SuppressWarnings("unchecked")
			Class<? extends Builder<?>> childObjectBuilderClass = (Class<? extends Builder<?>>) childObjectBuilder.get()
					.getClass();
			Optional<Class<?>> childObjectBuilderProductType = registrar
					.getBuilderProductClass(childObjectBuilderClass);

			Optional<Method> builderMethod = registrar.getBuilderMethodByName(builderClass, childObjectEntry.getKey(),
					childObjectBuilderProductType);
			if (builderMethod.isPresent()) {

				Class<?> parameterType = builderMethod.get().getParameterTypes()[0];

				if (childObjectBuilderProductType.isPresent()) {

					if (!parameterType.isAssignableFrom(childObjectBuilderProductType.get())) {

						System.err.println("\nUnable to populate field '" + childObjectEntry.getKey()
								+ "' on Builder -- type mismatch!");
						System.err.println("Builder: '" + builderClass.getName() + "'");
						System.err.println("Parameter type: '" + parameterType.getName() + "'");
						System.err.println("Child-object Builder: '" + childObjectBuilderClass.getName() + "'");
						System.err.println("Child-object Builder product: '"
								+ childObjectBuilderProductType.get().getName() + "'");
						return;
					}

					Optional<Object> childObject = invokeBuilders(childObjectEntry.getValue());
					if (childObject.isPresent()) {
						try {
							builderMethod.get().invoke(builder, parameterType.cast(childObject.get()));

						} catch (Exception e) {
							System.err.println("\nUnable to populate field '" + childObjectEntry.getKey()
									+ "' on Builder -- unexpected exception!");
							System.err.println("Builder: '" + builderClass.getName() + "'");
							System.err.println("Parameter type: '" + parameterType.getName() + "'");
							System.err.println("Child-object Builder: '" + childObjectBuilderClass.getName() + "'");
							System.err.println("Child-object Builder product: '"
									+ childObjectBuilderProductType.get().getName() + "'");
							System.err.println("Exception message: " + e.getMessage());
							return;
						}
					} else {

						System.err.println("\nUnable to create child-object for Builder!");
						System.err.println("Builder: '" + builderClass.getName() + "'");
						System.err.println("Parameter type: '" + parameterType.getName() + "'");
						System.err.println("Child-object Builder: '" + childObjectBuilderClass.getName() + "'");
						System.err.println("Child-object Builder product: '"
								+ childObjectBuilderProductType.get().getName() + "'");
						return;
					}
				}
			} else {
				System.err.println(
						"\nUnable to create child-object for Builder -- cannot find Builder for child-object!");
				System.err.println("Builder: '" + builderClass.getName() + "'");
				System.err.println("Field-name: '" + childObjectEntry.getKey() + "'");
				System.err.println("Given child-object name: '" + childObjectEntry.getValue().getObjectName() + "'");
				return;
			}

		} else {
			System.err.println("\nUnable to populate child-object for Builder -- unknown field!");
			System.err.println("Builder: '" + builderClass.getName() + "'");
			System.err.println("Field-name: '" + childObjectEntry.getKey() + "'");
			return;
		}
	}
}
