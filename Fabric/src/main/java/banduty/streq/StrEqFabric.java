package banduty.streq;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrEqFabric implements ModInitializer {
	public static final String MOD_ID = "streq";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		LOGGER.info("StrEq has been Loaded successfully");
	}
}