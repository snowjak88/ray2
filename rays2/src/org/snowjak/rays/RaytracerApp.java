package org.snowjak.rays;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

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
import org.snowjak.rays.ui.AntialiasingScreenDecorator;
import org.snowjak.rays.ui.AntialiasingScreenDecorator.AA;
import org.snowjak.rays.ui.BasicScreen;
import org.snowjak.rays.ui.DrawsEntireScreen;
import org.snowjak.rays.ui.MultithreadedScreenDecorator;

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
public class RaytracerApp extends Application implements Renderer {

	private DrawsEntireScreen screenDrawer;

	public static void main(String[] args) {

		RaytracerApp.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		World world = buildWorld();

		WritableImage image = new WritableImage(800, 500);
		DrawsEntireScreen screen = new MultithreadedScreenDecorator(
				new AntialiasingScreenDecorator(AA.x8, new BasicScreen(image, world.getCamera())));

		ImageView imageView = new ImageView(image);
		Group root = new Group(imageView);
		Scene scene = new Scene(root, Color.BLACK);

		ContextMenu imageContextMenu = new ContextMenu();
		MenuItem saveImageMenuItem = new MenuItem("Save...");
		FileChooser saveFileChooser = new FileChooser();
		saveFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		saveFileChooser.setInitialFileName("image");

		saveFileChooser.getExtensionFilters().add(new ExtensionFilter("png", "*.png"));
		saveFileChooser.getExtensionFilters().add(new ExtensionFilter("jpeg", "*.jpg"));
		saveFileChooser.getExtensionFilters().add(new ExtensionFilter("bmp", "*.bmp"));
		saveImageMenuItem.setOnAction(e -> {
			imageContextMenu.hide();
			int selectedIndex = 0;
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
		Renderer renderer = this;
		Executors.newSingleThreadExecutor().submit(() -> renderer.render(world.getCamera()));
	}

	private World buildWorld() {

		World world = World.getSingleton();

		Cylinder cylinder1 = new Cylinder(true);
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
				new FogDecoratingLightingModel(75d, new RawColor(Color.GRAY), new PhongReflectionLightingModel()));

		return world;
	}

	@Override
	public DrawsEntireScreen getScreenDrawer() {

		return screenDrawer;

	}

	public void setScreenDrawer(DrawsEntireScreen screenDrawer) {

		this.screenDrawer = screenDrawer;
	}

}
