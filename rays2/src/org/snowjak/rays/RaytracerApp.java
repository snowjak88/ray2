package org.snowjak.rays;

import java.util.concurrent.Executors;

import org.snowjak.rays.Renderer.Settings;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.camera.DepthOfFieldCamera;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.color.SimpleColorScheme;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.PointLight;
import org.snowjak.rays.light.model.AdditiveCompositingLightingModel;
import org.snowjak.rays.light.model.AmbientLightingModel;
import org.snowjak.rays.light.model.EnvironmentMapDecoratingLightingModel;
import org.snowjak.rays.light.model.FresnelLightingModel;
import org.snowjak.rays.light.model.LambertianDiffuseLightingModel;
import org.snowjak.rays.light.model.PhongSpecularLightingModel;
import org.snowjak.rays.light.model.SphericalEnvironmentMap;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.shape.Plane;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.shape.Sphere;
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
			world.shutdown();
			primaryStage.close();
		});

		Executors.newSingleThreadExecutor().submit(() -> {
			System.out.println("Rendering ...");
			renderer.render(world.getCamera());
		});
	}

	private World buildWorld() {

		World world = World.getSingleton();

		Shape sphere = new Sphere();
		sphere.setMaterial(
				new Material(Functions.constant(Color.WHITE), Functions.constant(0d), Functions.constant(1d)));
		sphere.getTransformers().add(new Scale(1.5, 1.5, 1.5));
		sphere.getTransformers().add(new Translation(-3d, 0d, -3d));
		sphere.setDiffuseColorScheme(new SimpleColorScheme(Color.RED));
		world.getShapes().add(sphere);

		sphere = new Sphere();
		sphere.setMaterial(
				new Material(Functions.constant(Color.WHITE), Functions.constant(0d), Functions.constant(1d)));
		sphere.getTransformers().add(new Scale(1.5, 1.5, 1.5));
		sphere.getTransformers().add(new Translation(0d, 0d, 0d));
		sphere.setDiffuseColorScheme(new SimpleColorScheme(Color.GREEN));
		world.getShapes().add(sphere);

		sphere = new Sphere();
		sphere.setMaterial(
				new Material(Functions.constant(Color.WHITE), Functions.constant(0d), Functions.constant(1d)));
		sphere.getTransformers().add(new Scale(1.5, 1.5, 1.5));
		sphere.getTransformers().add(new Translation(3d, 0d, 3d));
		sphere.setDiffuseColorScheme(new SimpleColorScheme(Color.BLUE));
		world.getShapes().add(sphere);

		Plane plane = new Plane();
		Material beneathPlaneMaterial = new Material(Functions.constant(Color.WHITE), Functions.constant(0d),
				Functions.constant(1d));
		plane.setMinusMaterial(beneathPlaneMaterial);
		plane.setDiffuseColorScheme(
				(v) -> new RawColor(Functions.lerp(Color.BLACK, Color.WHITE, Functions.checkerboard(v))));
		plane.getTransformers().add(new Translation(0d, -2d, 0d));
		world.getShapes().add(plane);

		Light light = new PointLight(new RawColor(Color.WHITE).multiplyScalar(0.05), new RawColor(Color.WHITE),
				new RawColor(Color.WHITE), 60d);
		light.getTransformers().add(new Translation(0, 6, 0));
		world.getLights().add(light);

		Camera camera = new DepthOfFieldCamera(4.0, 60.0, 10d, 16, 0.5);
		camera.getTransformers().add(new Translation(0d, 2d, -10d));
		world.setCamera(camera);

		world.setLightingModel(new EnvironmentMapDecoratingLightingModel(
				new SphericalEnvironmentMap(new Image("resources/images/spherical-map-field2.jpg")),
				new FresnelLightingModel(new AdditiveCompositingLightingModel(new AmbientLightingModel(),
						new LambertianDiffuseLightingModel(), new PhongSpecularLightingModel()))));

		return world;
	}

}
