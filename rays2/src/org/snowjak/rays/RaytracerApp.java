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
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.snowjak.rays.light.model.AdditiveCompositingLightingModel;
import org.snowjak.rays.light.model.AmbientLightingModel;
import org.snowjak.rays.light.model.EnvironmentMapDecoratingLightingModel;
import org.snowjak.rays.light.model.FresnelLightingModel;
import org.snowjak.rays.light.model.LambertianDiffuseLightingModel;
import org.snowjak.rays.light.model.PhongSpecularLightingModel;
import org.snowjak.rays.light.model.SphericalEnvironmentMap;
import org.snowjak.rays.ui.impl.JavaFxPixelDrawer;
import org.snowjak.rays.world.World;
import org.snowjak.rays.world.importfile.BuilderInvoker;
import org.snowjak.rays.world.importfile.WorldFileObjectDefinition;
import org.snowjak.rays.world.importfile.WorldFileScanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

@SuppressWarnings("javadoc")
public class RaytracerApp extends Application {

	private static Settings settings;

	private static World world;

	public static void main(String[] args) {

		settings = Settings.presetFast();
		world = loadWorldFromFile(new File("sample.world"));

		processCommandLineOptions(args);

		RaytracerApp.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

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

	private static Options getCommandLineOptions() {

		Options options = new Options();

		options.addOption(Option.builder("s")
				.longOpt("settings")
				.hasArg()
				.argName(".settings-file")
				.desc("render settings file name")
				.build());
		options.addOption(Option.builder("d")
				.longOpt("create-default-settings")
				.desc("create a default settings file as './settings.properties'")
				.build());
		options.addOption(Option.builder("w")
				.longOpt("world")
				.hasArg()
				.argName(".world-file")
				.desc("load and render a .world file")
				.build());
		options.addOption(Option.builder("h").longOpt("help").desc("show this help message").build());

		return options;
	}

	private static void processCommandLineOptions(String[] args) {

		CommandLine cmd;
		try {
			cmd = new DefaultParser().parse(getCommandLineOptions(), args);

		} catch (ParseException e1) {
			System.err.println("\nCould not parse the given command-line -- unexpected exception!");
			System.err.println("Exception message reads: " + e1.getMessage());

			System.exit(-1);
			return;
		}

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
			System.exit(-1);
		}
		if (cmd.hasOption('s')) {
			File settingsFile = new File(cmd.getOptionValue('s'));
			if (!settingsFile.exists()) {
				System.err.println("Cannot find settings file '" + settingsFile.getPath() + "'. Please try again.");
				System.exit(-1);
			}
			if (!settingsFile.isFile()) {
				System.err.println("Settings file-name '" + settingsFile.getPath()
						+ "' does not map to a file. Please try again.");
				System.exit(-1);
			}
			if (!settingsFile.canRead()) {
				System.err.println("Settings file '" + settingsFile.getPath()
						+ "' is unreadable. Please check file permissions and try again.");
				System.exit(-1);
			}

			Properties settingsProperties = new Properties();
			try {
				Reader reader = new BufferedReader(new FileReader(settingsFile));
				settingsProperties.load(reader);
				reader.close();
			} catch (IOException e) {
				System.err.println("Could not read settings file '" + settingsFile.getPath() + "': '" + e.getMessage()
						+ "'. Please try again.");
				System.exit(-1);
			}
			settings = Settings.fromProperties(settingsProperties, settings);

		}
		if (cmd.hasOption("w")) {
			String worldFileName = cmd.getOptionValue("w");
			if (worldFileName == null) {
				System.err.println("You must provide the name of a .world file you wish to render!");
				System.exit(-1);
			}
			File worldFile = new File(worldFileName);
			if (!worldFile.exists()) {
				System.err.println("Error: the indicated world-file '" + worldFile.getPath()
						+ "' does not appear to exist.\n" + "Please double-check your file-name and try again.");
				System.exit(-1);
			}
			if (!worldFile.isFile()) {
				System.err.println("Error: the indicated world-file '" + worldFile.getPath()
						+ "' does not appear to be a file.\n" + "Please double-check your file-name and try again.");
				System.exit(-1);
			}

			world = loadWorldFromFile(worldFile);

		}
		if (cmd.hasOption('h')) {
			new HelpFormatter().printHelp("java -jar rays2.jar", getCommandLineOptions(), true);
			System.exit(-1);
		}
	}

	private static World loadWorldFromFile(File file) {

		try {
			WorldFileScanner worldScanner = new WorldFileScanner(new FileReader(file));

			Optional<WorldFileObjectDefinition> worldFileDefinition = worldScanner.scan();
			worldScanner.close();

			assert (worldFileDefinition.isPresent());

			World world = (World) BuilderInvoker.getSingleton().invokeBuilders(worldFileDefinition.get()).get();

			return world;

		} catch (IOException e) {

			e.printStackTrace();

			Platform.exit();
			System.exit(-1);
			return null;
		}

	}
}
