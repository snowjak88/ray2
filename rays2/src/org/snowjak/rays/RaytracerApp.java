package org.snowjak.rays;

import java.util.concurrent.Executors;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Renderer.Settings;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.color.SimpleColorScheme;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.PointLight;
import org.snowjak.rays.light.indirect.PhotonMap;
import org.snowjak.rays.light.model.AdditiveCompositingLightingModel;
import org.snowjak.rays.light.model.AmbientLightingModel;
import org.snowjak.rays.light.model.EnvironmentMapDecoratingLightingModel;
import org.snowjak.rays.light.model.FresnelLightingModel;
import org.snowjak.rays.light.model.LambertianDiffuseLightingModel;
import org.snowjak.rays.light.model.PhongSpecularLightingModel;
import org.snowjak.rays.light.model.PhotonMapLightingModel;
import org.snowjak.rays.light.model.SphericalEnvironmentMap;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.shape.Cube;
import org.snowjak.rays.shape.Plane;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.shape.Sphere;
import org.snowjak.rays.transform.Scale;
import org.snowjak.rays.transform.Translation;
import org.snowjak.rays.ui.AntialiasingScreenDecorator.AA;
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
		// Settings.presetDetailed();
		Settings.getSingleton().setAntialiasing(AA.x16);
		World.getSingleton().setMaxRayRecursion(8);
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

		Shape sphere1 = new Sphere();
		sphere1.setMaterial(
				new Material(Functions.constant(Color.WHITE), Functions.constant(1.0d), Functions.constant(1.3)));
		sphere1.getTransformers().add(new Scale(1.5, 1.5, 1.5));
		sphere1.getTransformers().add(new Translation(-2d, 0d, 0d));
		sphere1.setDiffuseColorScheme(new SimpleColorScheme(Color.WHITE));
		world.getShapes().add(sphere1);

		Shape sphere2 = new Sphere();
		sphere2.setMaterial(
				new Material(Functions.constant(Color.WHITE), Functions.constant(0.0d), Functions.constant(20d)));
		sphere2.getTransformers().add(new Scale(1.5, 1.5, 1.5));
		sphere2.getTransformers().add(new Translation(+2d, 0d, 0d));
		sphere2.setDiffuseColorScheme(new SimpleColorScheme(Color.WHITE));
		world.getShapes().add(sphere2);

		Shape cube = new Cube();
		cube.setMaterial(new Material(Functions.constant(Color.WHITE), Functions.constant(0d), Functions.constant(1d)));
		cube.setDiffuseColorScheme(new SimpleColorScheme(Color.RED));
		cube.getTransformers().add(new Scale(1d, 10d, 10d));
		cube.getTransformers().add(new Translation(-8d, 0d, 0d));
		world.getShapes().add(cube);

		cube = new Cube();
		cube.setMaterial(new Material(Functions.constant(Color.WHITE), Functions.constant(0d), Functions.constant(1d)));
		cube.setDiffuseColorScheme(new SimpleColorScheme(Color.BLUE));
		cube.getTransformers().add(new Scale(1d, 10d, 10d));
		cube.getTransformers().add(new Translation(8d, 0d, 0d));
		world.getShapes().add(cube);

		cube = new Cube();
		cube.setMaterial(new Material(Functions.constant(Color.WHITE), Functions.constant(0d), Functions.constant(1d)));
		cube.setDiffuseColorScheme(new SimpleColorScheme(Color.WHITE));
		cube.getTransformers().add(new Scale(20d, 20d, 1d));
		cube.getTransformers().add(new Translation(0d, 0d, 8d));
//		world.getShapes().add(cube);

		cube = new Cube();
		cube.setMaterial(new Material(Functions.constant(Color.WHITE), Functions.constant(0d), Functions.constant(1d)));
		cube.setDiffuseColorScheme(new SimpleColorScheme(Color.WHITE));
		cube.getTransformers().add(new Scale(20d, 1d, 20d));
		cube.getTransformers().add(new Translation(0d, 7.1d, 0d));
//		world.getShapes().add(cube);

		Plane plane = new Plane();
		Material beneathPlaneMaterial = new Material(Functions.constant(Color.WHITE), Functions.constant(0d),
				Functions.constant(1d));
		plane.setMinusMaterial(beneathPlaneMaterial);
		plane.setDiffuseColorScheme(new SimpleColorScheme(Color.WHITE));
		plane.getTransformers().add(new Translation(0d, -2d, 0d));
//		world.getShapes().add(plane);

		Light light = new PointLight(new RawColor(Color.WHITE).multiplyScalar(0.05), new RawColor(Color.WHITE),
				new RawColor(Color.WHITE), 60d);
		light.getTransformers().add(new Translation(0, 6, 0));
		world.getLights().add(light);

		Camera camera = new Camera(4.0, 60.0);
		camera.getTransformers().add(new Translation(0d, 2d, -10d));
		world.setCamera(camera);

		world.setLightingModel(new EnvironmentMapDecoratingLightingModel(
				new SphericalEnvironmentMap(new Image("resources/images/spherical-map-field2.jpg")),
				new FresnelLightingModel(new AdditiveCompositingLightingModel(new AmbientLightingModel(),
						new LambertianDiffuseLightingModel(), new PhongSpecularLightingModel()))));

		PhotonMap.getSingleton().getAimShapes().add(sphere1);
		PhotonMap.getSingleton().getAimShapes().add(sphere2);

		return world;
	}

}
