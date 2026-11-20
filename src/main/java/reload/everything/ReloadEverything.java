package reload.everything;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReloadEverything implements ModInitializer {
	public static final String MOD_ID = "reload-everything";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Reload Everything initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static void info(String message, Object... args) {
		LOGGER.info(message, args);
	}

	public static void warn(String message, Object... args) {
		LOGGER.warn(message, args);
	}

	public static void error(String message) {
		LOGGER.error(message);
	}

	public static void error(String message, Throwable throwable) {
		LOGGER.error(message, throwable);
	}
}
