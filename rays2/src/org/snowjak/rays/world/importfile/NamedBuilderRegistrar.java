package org.snowjak.rays.world.importfile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.builder.Builder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * The NamedObjectRegistrar is responsible for discovering and registering
 * Builders and methods annotated with {@link HasName}.
 * 
 * @author snowjak88
 *
 */
public class NamedBuilderRegistrar {

	private static NamedBuilderRegistrar INSTANCE = null;

	private Map<String, Class<? extends Builder<?>>> discoveredBuilders = new HashMap<>();

	private Map<Class<? extends Builder<?>>, Set<Pair<String, Method>>> discoveredBuilderMethods = new HashMap<>();

	/**
	 * @return the singleton NamedBuilderRegistrar instance
	 */
	public static NamedBuilderRegistrar getSingleton() {

		if (INSTANCE == null)
			INSTANCE = new NamedBuilderRegistrar();

		return INSTANCE;
	}

	protected NamedBuilderRegistrar() {

	}

	/**
	 * Given a name, attempt to find a {@link Builder} class which has been
	 * annotated (with {@link HasName}) with that name.
	 * 
	 * @param name
	 * @return a Builder with a matching name, if any exists
	 */
	public Optional<Builder<?>> getBuilderByName(String name) {

		Optional<Class<? extends Builder<?>>> builderClass = Optional.empty();

		if (discoveredBuilders.containsKey(name))
			builderClass = Optional.of(discoveredBuilders.get(name));

		else {

			Optional<Class<? extends Builder<?>>> discoveredBuilder = scanClasspathForBuilder(name);
			if (discoveredBuilder.isPresent()) {
				discoveredBuilders.put(name, discoveredBuilder.get());
				scanClassForNamedMethodNames(discoveredBuilder.get());
			}

			builderClass = discoveredBuilder;
		}

		if (!builderClass.isPresent())
			return Optional.empty();

		try {
			Builder<?> builder = (Builder<?>) builderClass.get().getMethod("builder", (Class<?>[]) null).invoke(null,
					(Object[]) null);
			return Optional.of(builder);

		} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| SecurityException e) {

			System.err.println("Cannot instantiate Builder -- unexpected exception!");
			System.err.println("Given name: '" + name + "'");
			System.err.println("Discovered class: '" + builderClass.get().getName() + "'");
			System.err.println("Exception message: " + e.getMessage());
			return Optional.empty();
		}
	}

	/**
	 * Given a Builder type, return its product type (i.e., the class that
	 * {@link Builder#build()} will return).
	 * 
	 * @param builderClass
	 * @return the Builder's product type, if it can be determined
	 */
	public Optional<Class<?>> getBuilderProductClass(Class<? extends Builder<?>> builderClass) {

		try {
			return Optional.of(builderClass.getMethod("build", (Class<?>[]) null).getReturnType());

		} catch (NoSuchMethodException | SecurityException e) {
			System.err.println("Cannot query Builder for its product type -- unexpected exception!");
			System.err.println("Builder class: '" + builderClass.getName() + "'");
			System.err.println("Exception message: " + e.getMessage());

			return Optional.empty();
		}

	}

	/**
	 * Given a Builder class and a name, attempt to find a {@link Method}
	 * annotated (with {@link HasName}) with a matching name.
	 * 
	 * @param builderClass
	 * @param name
	 * @param givenArgumentClass
	 * @return a Method on the given class with a matching name, if any exists
	 */
	public Optional<Method> getBuilderMethodByName(Class<? extends Builder<?>> builderClass, String name,
			Optional<Class<?>> givenArgumentClass) {

		if (!discoveredBuilderMethods.containsKey(builderClass))
			return Optional.empty();

		return discoveredBuilderMethods.get(builderClass)
				.parallelStream()
				.filter(p -> p.getKey().equals(name))
				.map(p -> p.getValue())
				.filter(m -> Arrays.stream(m.getParameterTypes())
						.anyMatch(c -> c.isAssignableFrom(givenArgumentClass.orElse(c))))
				.findAny();

	}

	@SuppressWarnings("unchecked")
	private Optional<Class<? extends Builder<?>>> scanClasspathForBuilder(String name) {

		Optional<Class<? extends Builder<?>>> result = Optional.empty();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(HasName.class));

		for (BeanDefinition def : scanner.findCandidateComponents("org.snowjak.rays")) {

			Class<?> discoveredClass;
			try {
				discoveredClass = Class.forName(def.getBeanClassName());

			} catch (ClassNotFoundException e) {
				System.err.println("Unable to scan for Builders -- unexpected exception!");
				System.err.println("Exception message: " + e.getMessage());
				continue;
			}
			if (Builder.class.isAssignableFrom(discoveredClass)
					&& discoveredClass.getAnnotation(HasName.class).value().equals(name)) {

				result = Optional.of((Class<? extends Builder<?>>) discoveredClass);
				break;
			}
		}

		return result;
	}

	private void scanClassForNamedMethodNames(Class<? extends Builder<?>> classToScan) {

		Set<Pair<String, Method>> methods = new HashSet<>();

		for (Method method : classToScan.getMethods()) {

			if (method.isAnnotationPresent(HasName.class)) {

				String nameValue = method.getAnnotation(HasName.class).value();
				methods.add(new Pair<>(nameValue, method));
			}
		}

		discoveredBuilderMethods.put(classToScan, methods);
	}

}
