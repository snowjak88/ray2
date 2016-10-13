package org.snowjak.rays;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.color.SimpleColorScheme;
import org.snowjak.rays.function.Functions;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.PointLight;
import org.snowjak.rays.light.model.AdditiveCompositingLightingModel;
import org.snowjak.rays.light.model.AmbientLightingModel;
import org.snowjak.rays.light.model.EnvironmentMapDecoratingLightingModel;
import org.snowjak.rays.light.model.FresnelLightingModel;
import org.snowjak.rays.light.model.LambertianDiffuseLightingModel;
import org.snowjak.rays.light.model.PhongSpecularLightingModel;
import org.snowjak.rays.light.model.SphericalEnvironmentMap;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.shape.Plane;
import org.snowjak.rays.shape.Sphere;
import org.snowjak.rays.transform.Rotation;
import org.snowjak.rays.transform.Scale;
import org.snowjak.rays.transform.Translation;
import org.snowjak.rays.ui.impl.JavaFxPixelDrawer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

@SuppressWarnings("javadoc")
public class RaytracerApp extends Application {

	private static String[] args;

	public static void main(String[] args) {

		RaytracerApp.args = args;
		RaytracerApp.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		World world = buildWorld();
		Settings settings = Settings.presetFast();

		CommandLine cmd = new DefaultParser().parse(getCommandLineOptions(), args);

		if (cmd.hasOption('d')) {
			File settingsFile = new File("settings.properties");
			Properties settingsProperties = settings.saveToProperties();
			DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss");

			try {
				Writer writer = new BufferedWriter(new FileWriter(settingsFile));
				settingsProperties.store(writer, "Default render settings - " + fmt.format(new Date()));
				writer.close();
			} catch (IOException e) {
				System.err.println("Cannot save default-settings file: '" + e.getMessage() + "'. Please try again.");
			}

			Platform.exit();
			return;
		}
		if (cmd.hasOption('s')) {
			File settingsFile = new File(cmd.getOptionValue('s'));
			if (!settingsFile.exists()) {
				System.err.println("Cannot find settings file '" + settingsFile.getPath() + "'. Please try again.");
				Platform.exit();
				return;
			}
			if (!settingsFile.isFile()) {
				System.err.println("Settings file-name '" + settingsFile.getPath()
						+ "' does not map to a file. Please try again.");
				Platform.exit();
				return;
			}
			if (!settingsFile.canRead()) {
				System.err.println("Settings file '" + settingsFile.getPath()
						+ "' is unreadable. Please check file permissions and try again.");
				Platform.exit();
				return;
			}

			Properties settingsProperties = new Properties();
			try {
				Reader reader = new BufferedReader(new FileReader(settingsFile));
				settingsProperties.load(reader);
				reader.close();
			} catch (IOException e) {
				System.err.println("Could not read settings file '" + settingsFile.getPath() + "': '" + e.getMessage()
						+ "'. Please try again.");
				Platform.exit();
				return;
			}
			settings = Settings.fromProperties(settingsProperties, settings);

		}
		if (cmd.hasOption('h')) {
			new HelpFormatter().printHelp("java -jar rays2.jar", getCommandLineOptions(), true);
			Platform.exit();
			return;
		}

		Renderer renderer = new Renderer(new JavaFxPixelDrawer(primaryStage, settings));

		RaytracerContext.getSingleton().setSettings(settings);
		RaytracerContext.getSingleton().setCurrentRenderer(renderer);
		RaytracerContext.getSingleton().setCurrentWorld(world);

		primaryStage.setOnCloseRequest((e) -> {
			RaytracerContext.getSingleton().shutdown();

			primaryStage.close();
		});

		Executors.newSingleThreadExecutor().submit(() -> {
			System.out.println("Rendering ...");
			RaytracerContext.getSingleton().getCurrentRenderer().render(world.getCamera());
		});
	}

	private Options getCommandLineOptions() {

		Options options = new Options();

		options.addOption(Option.builder("s")
				.longOpt("settings")
				.hasArg()
				.argName("file")
				.desc("render settings file name")
				.build());
		options.addOption(Option.builder("d")
				.longOpt("create-default-settings")
				.desc("create a default settings file as './settings.properties'")
				.build());
		options.addOption(Option.builder("h").longOpt("help").desc("show this help message").build());

		return options;
	}

	private World buildWorld() {

		World world = new World();

		Sphere sphere1 = new Sphere();
		sphere1.setMaterial(
				new Material(Functions.constant(Color.WHITE), Functions.constant(1d), Functions.constant(1.3d)));
		sphere1.getTransformers().add(new Scale(2, 2, 2));
		sphere1.getTransformers().add(new Translation(-4d, 0.1d, 0d));
		sphere1.setDiffuseColorScheme(new SimpleColorScheme(Color.WHITE));
		world.getShapes().add(sphere1);

		Sphere sphere2 = new Sphere();
		sphere2.setMaterial(
				new Material(Functions.constant(Color.WHITE), Functions.constant(0d), Functions.constant(180d)));
		sphere2.getTransformers().add(new Scale(2, 2, 2));
		sphere2.getTransformers().add(new Translation(4d, 0.1d, 0d));
		sphere2.setDiffuseColorScheme(new SimpleColorScheme(Color.WHITE));
		world.getShapes().add(sphere2);

		Plane plane = new Plane();
		plane.setMinusMaterial(
				new Material(Functions.constant(Color.GHOSTWHITE), Functions.constant(0d), Functions.constant(1d)));
		plane.getTransformers().add(new Translation(0d, -2d, 0d));
		plane.setDiffuseColorScheme(new SimpleColorScheme(Color.GHOSTWHITE));
		world.getShapes().add(plane);

		plane = new Plane();
		plane.setPlusMaterial(
				new Material(Functions.constant(Color.GHOSTWHITE), Functions.constant(0d), Functions.constant(1d)));
		plane.getTransformers().add(new Translation(0d, 10.5d, 0d));
		plane.setDiffuseColorScheme(new SimpleColorScheme(Color.GHOSTWHITE));
		world.getShapes().add(plane);

		plane = new Plane();
		plane.setPlusMaterial(
				new Material(Functions.constant(Color.GHOSTWHITE), Functions.constant(0d), Functions.constant(1d)));
		plane.getTransformers().add(new Rotation(-90d, 0d, 0d));
		plane.getTransformers().add(new Translation(0d, 0d, 5d));
		plane.setDiffuseColorScheme(new SimpleColorScheme(Color.GHOSTWHITE));
		world.getShapes().add(plane);

		plane = new Plane();
		plane.setPlusMaterial(
				new Material(Functions.constant(Color.ROYALBLUE), Functions.constant(0d), Functions.constant(1d)));
		plane.getTransformers().add(new Rotation(0d, 0d, -90d));
		plane.getTransformers().add(new Translation(-10d, 0d, 0d));
		plane.setDiffuseColorScheme(new SimpleColorScheme(Color.ROYALBLUE));
		world.getShapes().add(plane);

		plane = new Plane();
		plane.setPlusMaterial(
				new Material(Functions.constant(Color.INDIANRED), Functions.constant(0d), Functions.constant(1d)));
		plane.getTransformers().add(new Rotation(0d, 0d, 90d));
		plane.getTransformers().add(new Translation(10d, 0d, 0d));
		plane.setDiffuseColorScheme(new SimpleColorScheme(Color.INDIANRED));
		world.getShapes().add(plane);

		Light light = new PointLight(new RawColor(Color.WHITE).multiplyScalar(0.02), new RawColor(Color.WHITE),
				new RawColor(Color.WHITE), 100d, 0.5);
		light.getTransformers().add(new Translation(0d, 10d, 0d));
		world.getLights().add(light);

		Camera camera = new Camera(4.0, 60.0);
		camera.getTransformers().add(new Translation(0d, 2.5d, -10d));
		camera.getTransformers().add(new Rotation(-10d, 0d, 0d));
		world.setCamera(camera);

		world.setLightingModel(new EnvironmentMapDecoratingLightingModel(
				new SphericalEnvironmentMap(new Image("resources/images/spherical-map-field2.jpg")),
				new FresnelLightingModel(new AdditiveCompositingLightingModel(new AmbientLightingModel(),
						new LambertianDiffuseLightingModel(), new PhongSpecularLightingModel()))));

		return world;
	}

}
