package banduty.streq;

import net.fabricmc.api.ModInitializer;

import java.util.HashMap;
import java.util.Map;

public class StrEqFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		long fstStartTime = System.currentTimeMillis();
		String formula = "2x - sin(pi/y) * 3 + 2^z + 4*logb(x,y) + 2*(2z+2)";

		Map<String, Double> variables = new HashMap<>();
		variables.put("x", 4d);
		variables.put("y", 2d);
		variables.put("z", 5d);

		double value = StrEq.evaluate(formula, variables);
		System.out.println("First Value: " + value);
		long fstEndTime = System.currentTimeMillis();
		System.out.println("First Time: " + (fstEndTime - fstStartTime) + " ms");
		long sndStartTime = System.currentTimeMillis();
		double secondValue = StrEq.evaluate(formula, variables);
		System.out.println("Second Value: " + secondValue);
		long sndEndTime = System.currentTimeMillis();
		System.out.println("Second Time: " + (sndEndTime - sndStartTime) + " ms");
	}
}