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
import org.snowjak.rays.light.model.PhongReflectionLightingModel;
import org.snowjak.rays.light.model.SphericalEnvironmentMap;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.shape.Sphere;
import org.snowjak.rays.transform.Rotation;
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

		Shape sphere = new Sphere(1d);
		ColorScheme sphereColors = new SimpleColorScheme(Color.WHITE);
		sphereColors.setReflectivity(0.95);
		sphere.setAmbientColorScheme(sphereColors);
		sphere.setDiffuseColorScheme(sphereColors);
		world.getShapes().add(sphere);

		Light light = new PointLight(new RawColor(Color.WHITE).multiplyScalar(0.05), new RawColor(Color.WHITE),
				new RawColor(Color.WHITE));
		light.getTransformers().add(new Translation(4d, 2.5d, 0d));
		world.getLights().add(light);

		Camera camera = new Camera(4.0, 30.0);
		camera.getTransformers().add(new Translation(0d, 0.5d, -6d));
		camera.getTransformers().add(new Rotation(-5d, 0d, 0d));
		camera.getTransformers().add(new Rotation(0d, 30d, 0d));
		world.setCamera(camera);

		world.setLightingModel(new EnvironmentMapDecoratingLightingModel(
				new SphericalEnvironmentMap(new Image("resources/images/spherical-map-field2.jpg")),
				new PhongReflectionLightingModel()));

		return world;
	}

}
