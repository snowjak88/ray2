package org.snowjak.rays;

import java.util.concurrent.Executors;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.camera.BasicCamera;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.BlendColorScheme;
import org.snowjak.rays.color.CheckerboardColorScheme;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.color.SimpleColorScheme;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.PointLight;
import org.snowjak.rays.light.model.FogDecoratingLightingModel;
import org.snowjak.rays.light.model.PhongReflectionLightingModel;
import org.snowjak.rays.shape.Plane;
import org.snowjak.rays.shape.Sphere;
import org.snowjak.rays.shape.perturb.NormalPerturber;
import org.snowjak.rays.transform.Rotation;
import org.snowjak.rays.transform.Scale;
import org.snowjak.rays.transform.Translation;
import org.snowjak.rays.ui.impl.JavaFxPixelDrawer;

import javafx.application.Application;
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

		Renderer renderer = new Renderer(new JavaFxPixelDrawer(primaryStage));

		primaryStage.setOnCloseRequest((e) -> {
			renderer.shutdown();
			primaryStage.close();
		});

		Executors.newSingleThreadExecutor().submit(() -> renderer.render(world.getCamera()));
	}

	private World buildWorld() {

		World world = World.getSingleton();

		Sphere sphere = new Sphere(1.25);
		ColorScheme sphereColors = new BlendColorScheme(new SimpleColorScheme(Color.WHITE),
				new SimpleColorScheme(Color.RED), (v) -> FastMath.abs(Functions.getPerlinNoise(v)));
		// sphereColors.setReflectivity(0.5);
		sphere.setAmbientColorScheme(sphereColors);
		sphere.setDiffuseColorScheme(sphereColors);

		NormalPerturber normalPerturber = new NormalPerturber((v) -> v, sphere);
		world.getShapes().add(normalPerturber);

		Plane plane = new Plane();
		ColorScheme planeColorScheme = new CheckerboardColorScheme(Color.WHITE, Color.NAVY);
		planeColorScheme.setReflectivity(0.75);
		plane.setAmbientColorScheme(planeColorScheme);
		plane.setDiffuseColorScheme(planeColorScheme);
		plane.getTransformers().add(new Translation(0d, -2d, 0d));
		world.getShapes().add(plane);

		Light light = new PointLight(new RawColor(0.1, 0.1, 0.1), new RawColor(Color.WHITE), new RawColor(Color.WHITE));
		light.getTransformers().add(new Translation(4d, 4d, -2d));
		world.getLights().add(light);

		Camera camera = new BasicCamera(4.0, 35.0);
		camera.getTransformers().add(new Translation(0d, 0.75d, -6d));
		camera.getTransformers().add(new Rotation(-13d, 0d, 0d));
		camera.getTransformers().add(new Rotation(0d, 30d, 0d));
		world.setCamera(camera);

		world.setLightingModel(
				new FogDecoratingLightingModel(25d, new RawColor(Color.GRAY), new PhongReflectionLightingModel()));

		return world;
	}

}
