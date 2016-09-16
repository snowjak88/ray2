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
import org.snowjak.rays.light.model.AdditiveCompositingLightingModel;
import org.snowjak.rays.light.model.AmbientLightingModel;
import org.snowjak.rays.light.model.EnvironmentMapDecoratingLightingModel;
import org.snowjak.rays.light.model.FresnelLightingModel;
import org.snowjak.rays.light.model.LambertianDiffuseLightingModel;
import org.snowjak.rays.light.model.PhongSpecularLightingModel;
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

		Material shapeMaterial = new Material(Functions.constant(Color.WHITE), Functions.constant(1d),
				Functions.constant(0d), Functions.constant(1.8d));
		Material sphereMaterial = new Material(Functions.constant(Color.GREEN), Functions.constant(0.825d),
				Functions.constant(0.125), Functions.constant(1.3));

		Shape cylinder = new Cylinder();
		cylinder.getTransformers().add(new Scale(0.5, 2d, 0.5));

		Shape thing = new Intersect(cylinder, new Sphere());
		thing.setMaterial(shapeMaterial);
		thing.setDiffuseColorScheme(new SimpleColorScheme(Color.SILVER));
		thing.getTransformers().add(new Scale(4d, 4d, 4d));
		world.getShapes().add(thing);

		Shape sphere = new Sphere();
		sphere.setMaterial(sphereMaterial);
		sphere.setDiffuseColorScheme(Functions.constant(Color.GREEN));
		sphere.getTransformers().add(new Scale(2d, 2d, 2d));
		sphere.getTransformers().add(new Translation(6d, 0d, 0d));
		world.getShapes().add(sphere);

		sphere = new Sphere();
		sphere.setMaterial(sphereMaterial);
		sphere.setDiffuseColorScheme(Functions.constant(Color.GREEN));
		sphere.getTransformers().add(new Scale(2d, 2d, 2d));
		sphere.getTransformers().add(new Translation(-6d, 0d, 0d));
		world.getShapes().add(sphere);

		sphere = new Sphere();
		sphere.setMaterial(sphereMaterial);
		sphere.setDiffuseColorScheme(Functions.constant(Color.GREEN));
		sphere.getTransformers().add(new Scale(2d, 2d, 2d));
		sphere.getTransformers().add(new Translation(0d, 0d, 6d));
		world.getShapes().add(sphere);

		Plane plane = new Plane();
		Material beneathPlaneMaterial = new Material(Functions.constant(Color.LIGHTSKYBLUE), Functions.constant(1d),
				Functions.constant(1d / 3d), Functions.constant(1.3d)), abovePlaneMaterial = Material.AIR;
		plane.setMinusMaterial(beneathPlaneMaterial);
		plane.setPlusMaterial(abovePlaneMaterial);
		plane.getTransformers().add(new Translation(0d, -2.01d, 0d));
		world.getShapes().add(new NormalPerturber(
				(n, i) -> n.add(n.orthogonal().scalarMultiply(Functions.turbulence(i.getPoint(), 8) / 10d)), plane));

		Light light = new DirectionalLight(new Vector3D(1, -10, 1).normalize(),
				new RawColor(Color.WHITE).multiplyScalar(0.01), new RawColor(Color.BISQUE), new RawColor(Color.BISQUE),
				6d);
		world.getLights().add(light);

		Camera camera = new Camera(4.0, 60.0);
		camera.getTransformers().add(new Translation(0d, 1d, -10d));
		camera.getTransformers().add(new Rotation(-5d, 0d, 0d));
		camera.getTransformers().add(new Rotation(0d, -30d, 0d));
		world.setCamera(camera);

		world.setLightingModel(new EnvironmentMapDecoratingLightingModel(
				new SphericalEnvironmentMap(new Image("resources/images/spherical-map-field2.jpg")),
				new FresnelLightingModel(new AdditiveCompositingLightingModel(new AmbientLightingModel(),
						new LambertianDiffuseLightingModel(), new PhongSpecularLightingModel()))));

		return world;
	}

}
