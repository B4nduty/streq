package banduty.streq;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(StrEqNeoforge.MOD_ID)
public class StrEqNeoforge {
	public static final String MOD_ID = "streq";
	public static final Logger LOGGER = LogUtils.getLogger();

	public StrEqNeoforge(IEventBus modEventBus) {
		LOGGER.info("StrEq has been Loaded successfully");
	}
}