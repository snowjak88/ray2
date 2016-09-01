package org.snowjak.rays;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

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
import org.snowjak.rays.shape.csg.Union;
import org.snowjak.rays.transform.Rotation;
import org.snowjak.rays.transform.Translation;
import org.snowjak.rays.ui.BasicScreen;
import org.snowjak.rays.ui.Screen;

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
		Screen screen = new BasicScreen(image, world.getCamera());

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

		org.snowjak.rays.shape.Group group = new org.snowjak.rays.shape.Group();
		Union union = new Union();

		Sphere sphere1, sphere2;
		sphere1 = new Sphere();
		sphere1.getTransformers().add(new Translation(-0.5, 0d, 0d));
		sphere1.setAmbientColorScheme(new SimpleColorScheme(Color.BLUE));
		sphere1.setDiffuseColorScheme(new SimpleColorScheme(Color.BLUE));

		sphere2 = new Sphere();
		sphere2.getTransformers().add(new Translation(0.5, 0d, 0d));
		sphere2.setAmbientColorScheme(new SimpleColorScheme(Color.RED));
		sphere2.setDiffuseColorScheme(new SimpleColorScheme(Color.RED));
		
		group.getChildren().add(sphere1);
		group.getChildren().add(sphere2);
		group.getTransformers().add(new Translation(-2d, 1d, 0d));
		world.getShapes().add(group);
		
		union.getChildren().add(sphere1);
		union.getChildren().add(sphere2);
		union.getTransformers().add(new Translation(2d, 1d, 0d));
		world.getShapes().add(union);

		Plane plane = new Plane();
		ColorScheme planeColoring = new CheckerboardColorScheme(1d, Color.GREEN, Color.SADDLEBROWN);
		plane.setAmbientColorScheme(planeColoring);
		plane.setDiffuseColorScheme(planeColoring);
		plane.setShininess(1e10);
		plane.setReflectivity(0d);
		world.getShapes().add(plane);

		for (int x = -6; x <= 6; x += 6) {
			for (int z = -6; z <= 6; z += 6) {
				Light pointLight;
				pointLight = new PointLight(new RawColor(0.01, 0.01, 0.01), new RawColor(.3, .3, .3),
						new RawColor(1d, 1d, 1d));
				pointLight.getTransformers().add(new Translation(x, 5d, z));
				world.getLights().add(pointLight);
			}
		}

		Camera camera = new BasicCamera(2.0, 45.0);
		camera.getTransformers().add(new Translation(0d, 0d, -6d));
		camera.getTransformers().add(new Rotation(-15d, 0d, 0d));
		world.setCamera(camera);

		world.setLightingModel(
				new FogDecoratingLightingModel(50d, new RawColor(Color.GREY), new PhongReflectionLightingModel()));

		return world;
	}

}
