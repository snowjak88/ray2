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
import org.snowjak.rays.shape.Plane;
import org.snowjak.rays.shape.Sphere;
import org.snowjak.rays.transform.Rotation;
import org.snowjak.rays.transform.Translation;
import org.snowjak.rays.ui.BasicScreen;
import org.snowjak.rays.ui.Screen;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
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

		WritableImage image = new WritableImage(800, 800);
		Screen screen = new BasicScreen(image, world.getCamera());

		ImageView imageView = new ImageView(image);
		Group root = new Group(imageView);
		Scene scene = new Scene(root, Color.BLACK);

		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(e -> {
			Platform.exit();
		});
		primaryStage.show();

		Executors.newSingleThreadExecutor().submit(() -> screen.draw());
	}

	private World buildWorld() {

		World world = World.getSingleton();

		for (int x = -6; x <= 6; x += 3) {
			for (int y = 0; y <= 0; y += 2) {
				for (int z = -6; z <= 6; z +=3) {
					Sphere sphere = new Sphere(1d);
					sphere.getTransformers().add(new Translation(x, y, z));
					ColorScheme sphereColoring = new SimpleColorScheme(Color.hsb(360d * (x + z + 10d) / 20d, 1d, 1d));
					sphere.setAmbientColorScheme(sphereColoring);
					sphere.setDiffuseColorScheme(sphereColoring);
					sphere.setSpecularColorScheme(new SimpleColorScheme(Color.WHITE));
					sphere.setShininess(10d);
					sphere.setReflectivity(0.9);
					world.getShapes().add(sphere);
				}
			}
		}

		Plane plane = new Plane();
		ColorScheme planeColoring = new CheckerboardColorScheme(1d, Color.GREEN, Color.SADDLEBROWN);
		plane.setAmbientColorScheme(planeColoring);
		plane.setDiffuseColorScheme(planeColoring);
		// world.getShapes().add(plane);

		Light pointLight;
		pointLight = new PointLight(new RawColor(0.02, 0.02, 0.02), new RawColor(1d, 1d, 1d), new RawColor(1d, 1d, 1d));
		pointLight.getTransformers().add(new Translation(0d, 5d, -6d));
		world.getLights().add(pointLight);

		pointLight = new PointLight(new RawColor(0.02, 0.02, 0.02), new RawColor(1d, 1d, 1d), new RawColor(1d, 1d, 1d));
		pointLight.getTransformers().add(new Translation(0d, -5d, -6d));
		world.getLights().add(pointLight);

		Camera camera = new BasicCamera(2.0, 2.0, 45.0);
		camera.getTransformers().add(new Translation(0d, 0d, -12d));
		camera.getTransformers().add(new Rotation(-15d, 0d, 0d));
		world.setCamera(camera);

		world.setLightingModel(
				new FogDecoratingLightingModel(50d, new RawColor(Color.GREY), new PhongReflectionLightingModel()));

		return world;
	}

}
