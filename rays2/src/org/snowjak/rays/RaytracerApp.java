package org.snowjak.rays;

import java.util.concurrent.Executors;

import org.snowjak.rays.Renderer.Settings;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.color.SimpleColorScheme;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.PointLight;
import org.snowjak.rays.light.model.EnvironmentMapDecoratingLightingModel;
import org.snowjak.rays.light.model.PhongLightingModel;
import org.snowjak.rays.light.model.ReflectionsDecoratingLightingModel;
import org.snowjak.rays.light.model.SphericalEnvironmentMap;
import org.snowjak.rays.shape.Cube;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.shape.Sphere;
import org.snowjak.rays.shape.csg.Minus;
import org.snowjak.rays.transform.Rotation;
import org.snowjak.rays.transform.Scale;
import org.snowjak.rays.transform.Translation;
import org.snowjak.rays.ui.impl.JavaFxPixelDrawer;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

@SuppressWarnings("javadoc")
public class RaytracerApp extends Application {

	public static void main(String[] args) {

		RaytracerApp.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		World world = buildWorld();
		Settings.presetDetailed();
		Renderer renderer = new Renderer(new JavaFxPixelDrawer(primaryStage));

		primaryStage.setOnCloseRequest((e) -> {
			renderer.shutdown();
			primaryStage.close();
		});

		Executors.newSingleThreadExecutor().submit(() -> renderer.render(world.getCamera()));
	}

	private World buildWorld() {

		World world = World.getSingleton();

		Shape sphere = new Sphere();
		ColorScheme sphereColors = new SimpleColorScheme(Color.WHITE);
		sphere.setDiffuseColorScheme(sphereColors);

		Cube cube = new Cube();
		cube.setDiffuseColorScheme(new SimpleColorScheme(Color.GREEN));
		cube.getDiffuseColorScheme().setReflectivity(0.95);
		cube.getTransformers().add(new Scale(0.65, 0.65, 0.65));
		cube.getTransformers().add(new Translation(1d, 0d, 0d));

		world.getShapes().add(new Minus(sphere, cube));

		Light light = new PointLight(new RawColor(Color.WHITE).multiplyScalar(0.05), new RawColor(Color.WHITE),
				new RawColor(Color.WHITE));
		light.getTransformers().add(new Translation(4d, 2d, 4d));
		world.getLights().add(light);

		light = new PointLight(new RawColor(Color.WHITE).multiplyScalar(0.05), new RawColor(Color.WHITE),
				new RawColor(Color.WHITE));
		light.getTransformers().add(new Translation(-4d, 2d, 0d));
		world.getLights().add(light);

		Camera camera = new Camera(4.0, 60.0);
		camera.getTransformers().add(new Translation(0d, 0d, -6d));
		camera.getTransformers().add(new Rotation(0d, 75d, 0d));
		world.setCamera(camera);

		world.setLightingModel(new EnvironmentMapDecoratingLightingModel(
				new SphericalEnvironmentMap(new Image("resources/images/spherical-map-field2.jpg")),
				new ReflectionsDecoratingLightingModel(new PhongLightingModel())));

		return world;
	}

}
