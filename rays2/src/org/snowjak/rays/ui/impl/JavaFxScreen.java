package org.snowjak.rays.ui.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.snowjak.rays.Renderer.Settings;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.ui.Screen;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * A basic implementation of Screen under JavaFX.
 * <p>
 * This implementation creates a new window into which the Renderer draws the
 * finished image. The window has a context-menu (summoned by right-clicking the
 * completed image) which allows the user to save the finished image to
 * secondary storage in a variety of formats.
 * </p>
 * 
 * @author rr247200
 *
 */
public class JavaFxScreen extends Screen {

	private PixelWriter pixels;

	/**
	 * Create a new JavaFxScreen contained within the provided {@link Stage}.
	 * 
	 * @param screenStage
	 * 
	 * @param image
	 */
	public JavaFxScreen(Stage screenStage) {

		super(Settings.getSingleton().getImageWidth() - 1, Settings.getSingleton().getImageHeight() - 1);

		Settings settings = Settings.getSingleton();

		WritableImage image = new WritableImage(settings.getImageWidth(), settings.getImageHeight());
		ImageView imageView = new ImageView(image);
		Group root = new Group(imageView);
		Scene scene = new Scene(root, Color.BLACK);

		ContextMenu imageContextMenu = constructImageContextMenu(screenStage, imageView);

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

		screenStage.setScene(scene);

		screenStage.show();

		this.pixels = image.getPixelWriter();
	}

	@Override
	public void drawPixel(int x, int y, RawColor color) {

		Platform.runLater(() -> pixels.setColor(x, y, color.toColor()));
	}

	private ContextMenu constructImageContextMenu(Stage screenStage, ImageView imageView) {

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

			File saveFile = saveFileChooser.showSaveDialog(screenStage);

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

		return imageContextMenu;
	}

}
