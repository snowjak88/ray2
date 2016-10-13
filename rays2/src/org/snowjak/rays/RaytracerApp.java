package org.snowjak.rays;

import java.util.concurrent.Executors;

import org.snowjak.rays.camera.Camera;
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
import org.snowjak.rays.shape.Sphere;
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
		Renderer renderer = new Renderer(new JavaFxPixelDrawer(primaryStage), RendererSettings.presetDetailed());

		RaytracerContext.getSingleton().setCurrentRenderer(renderer);
		RaytracerContext.getSingleton().setCurrentWorld(world);

		primaryStage.setOnCloseRequest((e) -> {
			RaytracerContext.getSingleton().getCurrentRenderer().shutdown();
			RaytracerContext.getSingleton().getCurrentWorld().shutdown();

			primaryStage.close();
		});

		Executors.newSingleThreadExecutor().submit(() -> {
			System.out.println("Rendering ...");
			RaytracerContext.getSingleton().getCurrentRenderer().render(world.getCamera());
		});
	}

	private World buildWorld() {

		World world = World.getSingleton();

		Sphere sphere1 = new Sphere();
		sphere1.setMaterial(
				new Material(Functions.constant(Color.WHITE), Functions.constant(1d), Functions.constant(1.3d)));
		sphere1.getTransformers().add(new Scale(2, 2, 2));
		sphere1.getTransformers().add(new Translation(-4d, 0.1d, 0d));
		sphere1.setDiffuseColorScheme(new SimpleColorScheme(Color.WHITE));
		world.getShapes().add(sphere1);

		Sphere sphere2 = new Sphere();
		sphere2.setMaterial(
				new Material(Functions.constant(Color.WHITE), Functions.constant(0d), Functions.constant(180d)));
		sphere2.getTransformers().add(new Scale(2, 2, 2));
		sphere2.getTransformers().add(new Translation(4d, 0.1d, 0d));
		sphere2.setDiffuseColorScheme(new SimpleColorScheme(Color.WHITE));
		world.getShapes().add(sphere2);

		Plane plane = new Plane();
		plane.setMinusMaterial(
				new Material(Functions.constant(Color.GHOSTWHITE), Functions.constant(0d), Functions.constant(1d)));
		plane.getTransformers().add(new Translation(0d, -2d, 0d));
		plane.setDiffuseColorScheme(new SimpleColorScheme(Color.GHOSTWHITE));
		world.getShapes().add(plane);

		plane = new Plane();
		plane.setPlusMaterial(
				new Material(Functions.constant(Color.GHOSTWHITE), Functions.constant(0d), Functions.constant(1d)));
		plane.getTransformers().add(new Translation(0d, 10.5d, 0d));
		plane.setDiffuseColorScheme(new SimpleColorScheme(Color.GHOSTWHITE));
		world.getShapes().add(plane);

		plane = new Plane();
		plane.setPlusMaterial(
				new Material(Functions.constant(Color.GHOSTWHITE), Functions.constant(0d), Functions.constant(1d)));
		plane.getTransformers().add(new Rotation(-90d, 0d, 0d));
		plane.getTransformers().add(new Translation(0d, 0d, 5d));
		plane.setDiffuseColorScheme(new SimpleColorScheme(Color.GHOSTWHITE));
		world.getShapes().add(plane);

		plane = new Plane();
		plane.setPlusMaterial(
				new Material(Functions.constant(Color.ROYALBLUE), Functions.constant(0d), Functions.constant(1d)));
		plane.getTransformers().add(new Rotation(0d, 0d, -90d));
		plane.getTransformers().add(new Translation(-10d, 0d, 0d));
		plane.setDiffuseColorScheme(new SimpleColorScheme(Color.ROYALBLUE));
		world.getShapes().add(plane);

		plane = new Plane();
		plane.setPlusMaterial(
				new Material(Functions.constant(Color.INDIANRED), Functions.constant(0d), Functions.constant(1d)));
		plane.getTransformers().add(new Rotation(0d, 0d, 90d));
		plane.getTransformers().add(new Translation(10d, 0d, 0d));
		plane.setDiffuseColorScheme(new SimpleColorScheme(Color.INDIANRED));
		world.getShapes().add(plane);

		Light light = new PointLight(new RawColor(Color.WHITE).multiplyScalar(0.02), new RawColor(Color.WHITE),
				new RawColor(Color.WHITE), 100d, 0.5);
		light.getTransformers().add(new Translation(0d, 10d, 0d));
		world.getLights().add(light);

		Camera camera = new Camera(4.0, 60.0);
		camera.getTransformers().add(new Translation(0d, 2.5d, -10d));
		camera.getTransformers().add(new Rotation(-10d, 0d, 0d));
		world.setCamera(camera);

		world.setLightingModel(new EnvironmentMapDecoratingLightingModel(
				new SphericalEnvironmentMap(new Image("resources/images/spherical-map-field2.jpg")),
				new FresnelLightingModel(new AdditiveCompositingLightingModel(new AmbientLightingModel(),
						new LambertianDiffuseLightingModel(), new PhongSpecularLightingModel()))));

		return world;
	}

}
