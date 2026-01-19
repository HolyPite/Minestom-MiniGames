package me.holypite.manager.model;

import net.worldseed.multipart.ModelEngine;
import net.worldseed.resourcepack.PackBuilder;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModelManager {

    public ModelManager() {
        try {
            Path source = Path.of("models_source");
            Path rp = Path.of("resource_pack");
            Path data = Path.of("models_data");

            if (!Files.exists(source)) {
                Files.createDirectories(source);
                System.out.println("Created 'models_source' directory. Put your .bbmodel files here.");
                return;
            }

            // Generate Resource Pack and Model Data
            System.out.println("Generating Model Resource Pack...");
            var config = PackBuilder.generate(source, rp, data);

            // Load mappings into Engine
            System.out.println("Loading Model Mappings...");
            ModelEngine.loadMappings(new StringReader(config.modelMappings()), data);
            
            System.out.println("Models loaded successfully!");

        } catch (Exception e) {
            System.err.println("Failed to load models:");
            e.printStackTrace();
        }
    }
}
