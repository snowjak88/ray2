package org.snowjak.rays;

import java.util.concurrent.Executors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.Renderer.Settings;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.color.SimpleColorScheme;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.light.DirectionalLight;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.PointLight;
import org.snowjak.rays.light.indirect.PhotonMap;
import org.snowjak.rays.light.model.AdditiveCompositingLightingModel;
import org.snowjak.rays.light.model.AmbientLightingModel;
import org.snowjak.rays.light.model.EnvironmentMapDecoratingLightingModel;
import org.snowjak.rays.light.model.FresnelLightingModel;
import org.snowjak.rays.light.model.LambertianDiffuseLightingModel;
import org.snowjak.rays.light.model.PhongSpecularLightingModel;
import org.snowjak.rays.light.model.PhotonMapDecoratingLightingModel;
import org.snowjak.rays.light.model.SphericalEnvironmentMap;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.shape.Cylinder;
import org.snowjak.rays.shape.Plane;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.shape.Sphere;
import org.snowjak.rays.shape.csg.Intersect;
import org.snowjak.rays.shape.perturb.NormalPerturber;
import org.snowjak.rays.transform.Rotation;
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
//		Settings.presetDetailed();
		Settings.getSingleton().setAntialiasing(AA.x2);
		Renderer renderer = new Renderer(new JavaFxPixelDrawer(primaryStage));

		primaryStage.setOnCloseRequest((e) -> {
			renderer.shutdown();
			primaryStage.close();
		});

		Executors.newSingleThreadExecutor().submit(() -> {
			System.out.println("Building photon-map ...");
			PhotonMap.getSingleton().add(30000);
			System.out.println("Rendering ...");
			renderer.render(world.getCamera());
		});
	}

	private World buildWorld() {

		World world = World.getSingleton();

		Material sphereMaterial = new Material(Functions.constant(Color.WHITE), Functions.constant(1d),
				Functions.constant(0d), Functions.constant(1.3));

		Shape sphere = new Sphere();
		sphere.setMaterial(sphereMaterial);
		sphere.getTransformers().add(new Scale(2d, 2d, 2d));
		sphere.setDiffuseColorScheme(new SimpleColorScheme(Color.GREEN));
		sphere.getSpecularColorScheme().setShininess(1d);
		world.getShapes().add(sphere);

		Plane plane = new Plane();
		Material beneathPlaneMaterial = new Material(Functions.constant(Color.WHITE), Functions.constant(0d),
				Functions.constant(1d), Functions.constant(1d));
		plane.setMinusMaterial(beneathPlaneMaterial);
		plane.setDiffuseColorScheme(
				(v) -> new RawColor(Functions.lerp(Color.BLACK, Color.WHITE, Functions.checkerboard(v))));
//				new SimpleColorScheme(Color.SADDLEBROWN));
		plane.getTransformers().add(new Translation(0d, -2.01d, 0d));
		world.getShapes().add(plane);

		Light light = new PointLight(new RawColor(Color.WHITE).multiplyScalar(0.01), new RawColor(Color.WHITE),
				new RawColor(Color.WHITE), 10d);
		light.getTransformers().add(new Translation(-10d, 4d, 0d));
//		Light light = new DirectionalLight(new Vector3D(2, -1, -2), new RawColor(Color.WHITE).multiplyScalar(0.01),
//				new RawColor(Color.WHITE), new RawColor(Color.WHITE), 1d);
		world.getLights().add(light);

		Camera camera = new Camera(4.0, 60.0);
		camera.getTransformers().add(new Translation(0d, 1d, -10d));
		camera.getTransformers().add(new Rotation(-5d, 0d, 0d));
		camera.getTransformers().add(new Rotation(0d, -30d, 0d));
		world.setCamera(camera);

		world.setLightingModel(new EnvironmentMapDecoratingLightingModel(
				new SphericalEnvironmentMap(new Image("resources/images/spherical-map-field2.jpg")),
				new FresnelLightingModel(new PhotonMapDecoratingLightingModel(
						new AdditiveCompositingLightingModel(new AmbientLightingModel(),
								new LambertianDiffuseLightingModel(), new PhongSpecularLightingModel())))));

		PhotonMap.getSingleton().getAimShapes().add(sphere);

		return world;
	}

}
