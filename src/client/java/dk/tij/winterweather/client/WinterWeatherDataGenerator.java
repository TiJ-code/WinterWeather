package dk.tij.winterweather.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Provides winter weather data generator functionality for Winter Weather.
 */
public class WinterWeatherDataGenerator implements DataGeneratorEntrypoint {
    /**
     * Performs the on initialize data generator operation.
     *
     * @param fabricDataGenerator the fabric data generator value
     */
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
    }
}
