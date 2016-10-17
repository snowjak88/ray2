package org.snowjak.rays;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.color.ColorSchemeBuilder;
import org.snowjak.rays.color.RawColor;
import org.snowjak.rays.light.PointLightBuilder;
import org.snowjak.rays.light.model.AdditiveCompositingLightingModel;
import org.snowjak.rays.light.model.AmbientLightingModel;
import org.snowjak.rays.light.model.EnvironmentMapDecoratingLightingModel;
import org.snowjak.rays.light.model.FresnelLightingModel;
import org.snowjak.rays.light.model.LambertianDiffuseLightingModel;
import org.snowjak.rays.light.model.PhongSpecularLightingModel;
import org.snowjak.rays.light.model.SphericalEnvironmentMap;
import org.snowjak.rays.material.MaterialBuilder;
import org.snowjak.rays.shape.PlaneBuilder;
import org.snowjak.rays.shape.SphereBuilder;
import org.snowjak.rays.transform.Rotation;
import org.snowjak.rays.transform.Scale;
import org.snowjak.rays.transform.Translation;
import org.snowjak.rays.ui.impl.JavaFxPixelDrawer;
import org.snowjak.rays.world.World;
import org.snowjak.rays.world.importfile.BuilderInvoker;
import org.snowjak.rays.world.importfile.WorldFileObjectDefinition;
import org.snowjak.rays.world.importfile.WorldFileScanner;

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

		// World world = buildWorld();
		World world = loadWorldFromFile(new File("sample.world"));
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

		renderer.setLightingModel(new EnvironmentMapDecoratingLightingModel(
				new SphericalEnvironmentMap(new Image("resources/images/spherical-map-field2.jpg")),
				new FresnelLightingModel(new AdditiveCompositingLightingModel(new AmbientLightingModel(),
						new LambertianDiffuseLightingModel(), new PhongSpecularLightingModel()))));

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

	private World loadWorldFromFile(File file) {

		try {
			WorldFileScanner worldScanner = new WorldFileScanner(new FileReader(file));

			Optional<WorldFileObjectDefinition> worldFileDefinition = worldScanner.scan();
			worldScanner.close();

			assert (worldFileDefinition.isPresent());

			World world = (World) BuilderInvoker.getSingleton().invokeBuilders(worldFileDefinition.get()).get();

			Camera camera = new Camera(4.0, 60.0);
			camera.getTransformers().add(new Translation(0d, 2.5d, -10d));
			camera.getTransformers().add(new Rotation(-15d, 0d, 0d));
			world.setCamera(camera);

			return world;

		} catch (IOException e) {

			e.printStackTrace();

			Platform.exit();
			System.exit(-1);
			return null;
		}

	}

	private World buildWorld() {

		World world = new World();

		world.getShapes()
				.add(SphereBuilder.builder()
						.diffuse(ColorSchemeBuilder.builder().constant(Color.WHITE).build())
						.material(MaterialBuilder.builder().surfaceTransparency(1d).refractiveIndex(1.8).build())
						.transform(new Scale(2, 2, 2))
						.transform(new Translation(-4, 0.1, 0))
						.build());

		world.getShapes()
				.add(SphereBuilder.builder()
						.diffuse(ColorSchemeBuilder.builder().constant(Color.WHITE).build())
						.material(MaterialBuilder.builder().refractiveIndex(200d).build())
						.transform(new Scale(2, 2, 2))
						.transform(new Translation(4d, 0.1d, 0d))
						.build());

		world.getShapes()
				.add(PlaneBuilder.builder()
						.diffuse(ColorSchemeBuilder.builder().constant(Color.GHOSTWHITE).build())
						.transform(new Translation(0d, -2d, 0d))
						.build());

		world.getShapes()
				.add(PlaneBuilder.builder()
						.diffuse(ColorSchemeBuilder.builder().constant(Color.GHOSTWHITE).build())
						.transform(new Translation(0d, 12d, 0d))
						.build());

		world.getShapes()
				.add(PlaneBuilder.builder()
						.diffuse(ColorSchemeBuilder.builder().constant(Color.GHOSTWHITE).build())
						.transform(new Rotation(-90d, 0d, 0d))
						.transform(new Translation(0d, 0d, 5d))
						.build());

		world.getShapes()
				.add(PlaneBuilder.builder()
						.diffuse(ColorSchemeBuilder.builder().constant(Color.INDIANRED).build())
						.transform(new Rotation(0d, 0d, -90d))
						.transform(new Translation(-10d, 0d, 0d))
						.build());

		world.getShapes()
				.add(PlaneBuilder.builder()
						.diffuse(ColorSchemeBuilder.builder().constant(Color.GREEN).build())
						.transform(new Rotation(0d, 0d, 90d))
						.transform(new Translation(10d, 0d, 0d))
						.build());

		world.getLights()
				.add(PointLightBuilder.builder()
						.ambient(new RawColor(Color.WHITE).multiplyScalar(0.04))
						.intensity(100d)
						.radius(0.5)
						.transform(new Translation(0d, 9d, 0d))
						.build());

		Camera camera = new Camera(4.0, 60.0);
		camera.getTransformers().add(new Translation(0d, 2.5d, -10d));
		camera.getTransformers().add(new Rotation(-15d, 0d, 0d));
		world.setCamera(camera);

		return world;
	}

}
