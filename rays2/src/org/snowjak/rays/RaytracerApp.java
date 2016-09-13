package org.snowjak.rays;

import java.util.concurrent.Executors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Renderer.Settings;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.light.DirectionalLight;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.model.EnvironmentMapDecoratingLightingModel;
import org.snowjak.rays.light.model.MaterialAwareLightingModel;
import org.snowjak.rays.light.model.SphericalEnvironmentMap;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.shape.Cube;
import org.snowjak.rays.shape.Cylinder;
import org.snowjak.rays.shape.Plane;
import org.snowjak.rays.shape.Shape;
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

		Material material = new Material(Functions.constant(Color.RED), Functions.constant(0.5),
				Functions.constant(0.5d), Functions.constant(4d));

		Shape spherePrototype = new Sphere();
		spherePrototype.setMaterial(material);

		Shape cubePrototype = new Cube();
		cubePrototype.setMaterial(material);

		Shape cylinderPrototype = new Cylinder(true);
		cylinderPrototype.setMaterial(material);

		for (int x = -14; x <= 14; x += 6) {
			for (int z = 0; z <= 28; z += 6) {

				Shape shapeCopy = null;
				switch (((x + 14) + z * 28) / 6 % 3) {
				case 0:
					shapeCopy = cylinderPrototype.copy();
					break;
				case 1:
					shapeCopy = cubePrototype.copy();
					break;
				case 2:
					shapeCopy = spherePrototype.copy();
				}

				shapeCopy.getTransformers().add(new Scale(2d, 2d, 2d));
				shapeCopy.getMaterial().setSurfaceColor(
						new RawColor(Color.hsb(((double) ((x + 14) + z) / (28d + 28d)) * 360d, 1d, 1d)));
				shapeCopy.getTransformers().add(new Translation(x, 0d, z));
				world.getShapes().add(shapeCopy);
			}
		}

		Shape plane = new Plane();
		plane.setMaterial(new Material(
				(v) -> Functions.blend(new Pair<>(0d, Color.BLACK), new Pair<>(1d, Color.WHITE))
						.apply(Functions.checkerboard(v)),
				Functions.constant(1d), Functions.constant(1d), Functions.constant(1.5d)));
		plane.getTransformers().add(new Translation(0d, -4d, 0d));
		world.getShapes().add(plane);

		Light light = new DirectionalLight(new Vector3D(-4, -1, 1).normalize(),
				new RawColor(Color.WHITE).multiplyScalar(0.05), new RawColor(Color.WHITE), new RawColor(Color.WHITE),
				6d);
		world.getLights().add(light);

		Camera camera = new Camera(4.0, 60.0);
		camera.getTransformers().add(new Translation(0d, 1d, -14d));
		camera.getTransformers().add(new Rotation(-12.5d, 0d, 0d));
		camera.getTransformers().add(new Rotation(0d, 15d, 0d));
		world.setCamera(camera);

		world.setLightingModel(new EnvironmentMapDecoratingLightingModel(
				new SphericalEnvironmentMap(new Image("resources/images/spherical-map-field2.jpg")),
				new MaterialAwareLightingModel()));

		return world;
	}

}
