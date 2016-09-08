package org.snowjak.rays;

import java.util.concurrent.Executors;

import org.snowjak.rays.camera.BasicCamera;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.CheckerboardColorScheme;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.color.SimpleColorScheme;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.PointLight;
import org.snowjak.rays.light.model.FogDecoratingLightingModel;
import org.snowjak.rays.light.model.PhongReflectionLightingModel;
import org.snowjak.rays.shape.Cylinder;
import org.snowjak.rays.shape.Plane;
import org.snowjak.rays.shape.csg.Union;
import org.snowjak.rays.transform.Rotation;
import org.snowjak.rays.transform.Scale;
import org.snowjak.rays.transform.Translation;
import org.snowjak.rays.ui.impl.JavaFxScreen;

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

		Renderer renderer = new Renderer(new JavaFxScreen(primaryStage));

		primaryStage.setOnCloseRequest((e) -> {
			renderer.shutdown();
			primaryStage.close();
		});

		Executors.newSingleThreadExecutor().submit(() -> renderer.render(world.getCamera()));
	}

	private World buildWorld() {

		World world = World.getSingleton();

		Cylinder cylinder1 = new Cylinder(false);
		ColorScheme cylinderColorScheme = new SimpleColorScheme(Color.YELLOW);
		cylinderColorScheme.setReflectivity(0.9);
		cylinderColorScheme.setShininess(0.3);
		cylinder1.setAmbientColorScheme(cylinderColorScheme);
		cylinder1.setDiffuseColorScheme(cylinderColorScheme);
		cylinder1.getTransformers().add(new Scale(0.4, 2d, 0.4));

		Cylinder cylinder2 = cylinder1.copy();
		cylinder2.getTransformers().add(new Rotation(0d, 0d, 90d));

		Cylinder cylinder3 = cylinder1.copy();
		cylinder3.getTransformers().add(new Rotation(90d, 0d, 0d));

		Union union = new Union(cylinder1, cylinder2, cylinder3);
		world.getShapes().add(union);

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
