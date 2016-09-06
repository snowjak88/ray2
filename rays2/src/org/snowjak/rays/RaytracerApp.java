package org.snowjak.rays;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.camera.BasicCamera;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.CheckerboardColorScheme;
import org.snowjak.rays.color.ColorScheme;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.color.SimpleColorScheme;
import org.snowjak.rays.light.DirectionalLight;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.model.PhongReflectionLightingModel;
import org.snowjak.rays.shape.Cube;
import org.snowjak.rays.shape.Plane;
import org.snowjak.rays.shape.Sphere;
import org.snowjak.rays.shape.csg.Intersect;
import org.snowjak.rays.transform.Rotation;
import org.snowjak.rays.transform.Scale;
import org.snowjak.rays.transform.Translation;
import org.snowjak.rays.ui.AntialiasingScreenDecorator;
import org.snowjak.rays.ui.BasicScreen;
import org.snowjak.rays.ui.DrawsEntireScreen;
import org.snowjak.rays.ui.MultithreadedScreenDecorator;
import org.snowjak.rays.ui.TotalTimeElapsedScreenDecorator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

@SuppressWarnings("javadoc")
public class RaytracerApp extends Application {

	public static void main(String[] args) {

		RaytracerApp.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		World world = buildWorld();

		WritableImage image = new WritableImage(400, 300);
		DrawsEntireScreen screen = new TotalTimeElapsedScreenDecorator(primaryStage, new MultithreadedScreenDecorator(
				new AntialiasingScreenDecorator(new BasicScreen(primaryStage, image, world.getCamera()), 3)));

		ImageView imageView = new ImageView(image);
		Group root = new Group(imageView);
		Scene scene = new Scene(root, Color.BLACK);

		ContextMenu imageContextMenu = new ContextMenu();
		MenuItem saveImageMenuItem = new MenuItem("Save...");
		saveImageMenuItem.setOnAction(e -> {
			imageContextMenu.hide();

			String[] knownImageFormatSuffixes = ImageIO.getWriterFileSuffixes();

			int selectedIndex = 0;

			FileChooser saveFileChooser = new FileChooser();
			saveFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			saveFileChooser.setInitialFileName("image");

			saveFileChooser.getExtensionFilters().add(new ExtensionFilter("png", "*.png"));
			saveFileChooser.getExtensionFilters().add(new ExtensionFilter("jpeg", "*.jpg"));
			saveFileChooser.getExtensionFilters().add(new ExtensionFilter("bmp", "*.bmp"));
			saveFileChooser.setSelectedExtensionFilter(saveFileChooser.getExtensionFilters().get(selectedIndex));

			File saveFile = saveFileChooser.showSaveDialog(primaryStage);

			if (saveFile != null) {
				String selectedImageFormatName = saveFileChooser.getSelectedExtensionFilter().getDescription();

				BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
				try {
					ImageIO.write(bufferedImage, selectedImageFormatName, saveFile);

				} catch (IOException e1) {
					System.err.println("\nUnexpected problem saving the image to the file ["
							+ saveFile.getAbsolutePath() + "]\nMessage: ");
					System.err.println(e1.getMessage());
					e1.printStackTrace(System.err);

					Alert alertDialog = new Alert(AlertType.ERROR,
							"Unexpected problem saving the image: " + e1.getMessage(), ButtonType.OK);

					alertDialog.showAndWait();
				}
			}
		});
		imageContextMenu.getItems().add(saveImageMenuItem);

		root.setOnMouseClicked(e -> {
			switch (e.getButton()) {
			case SECONDARY:
				imageContextMenu.show(imageView, e.getScreenX(), e.getScreenY());
				break;
			default:
				imageContextMenu.hide();
				break;
			}
		});

		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(e -> {
			screen.shutdown();
			Platform.exit();
		});
		primaryStage.show();

		Executors.newSingleThreadExecutor().submit(() -> screen.draw());
	}

	private World buildWorld() {

		World world = World.getSingleton();

		ColorScheme colorScheme1 = new SimpleColorScheme(Color.WHITE);
		colorScheme1.setReflectivity(-1d);
		ColorScheme colorScheme2 = new SimpleColorScheme(Color.CORAL);
		colorScheme2.setReflectivity(0.95);
		ColorScheme sphereColorScheme = new CheckerboardColorScheme(0.5, colorScheme1, colorScheme2);
		sphereColorScheme.getTransformers().add(new Rotation(0d, 0d, 45d));

		Sphere sphere1 = new Sphere();
		sphere1.setAmbientColorScheme(new SimpleColorScheme(Color.YELLOW));
		sphere1.setDiffuseColorScheme(new SimpleColorScheme(Color.YELLOW));

		Cube cube1 = new Cube();
		cube1.setAmbientColorScheme(new SimpleColorScheme(Color.BLUE));
		cube1.setDiffuseColorScheme(new SimpleColorScheme(Color.BLUE));
		cube1.getTransformers().add(new Scale(2d, 2d, 2d));
		cube1.getTransformers().add(new Translation(-1d, -1d, -1d));

		Intersect intersect = new Intersect(sphere1, cube1);
//		intersect.getTransformers().add(new Translation(0d, 0d, 0d));
		intersect.getTransformers().add(new Rotation(0d, -75d, 0d));
		world.getShapes().add(intersect);

		ColorScheme planeColorScheme = new SimpleColorScheme(new RawColor(Color.BROWN).multiplyScalar(0.2));
		Plane plane = new Plane();
		plane.setAmbientColorScheme(planeColorScheme);
		plane.setDiffuseColorScheme(planeColorScheme);
		plane.getTransformers().add(new Translation(0d, -1d, 0d));
		world.getShapes().add(plane);

		Light light = new DirectionalLight(new Vector3D(0d, -1d, 0d), new RawColor(0.05, 0.05, 0.05),
				new RawColor(Color.WHITE), new RawColor(Color.WHITE));
		world.getLights().add(light);

		Camera camera = new BasicCamera(4.0, 35.0);
		camera.getTransformers().add(new Translation(0d, 1d, -6d));
		camera.getTransformers().add(new Rotation(-10d, 0d, 0d));
		world.setCamera(camera);

		world.setLightingModel(new PhongReflectionLightingModel());

		return world;
	}

}
