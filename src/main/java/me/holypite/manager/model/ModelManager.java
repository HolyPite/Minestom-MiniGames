package me.holypite.manager.model;

import net.worldseed.multipart.ModelEngine;
import net.worldseed.resourcepack.PackBuilder;
import java.io.StringReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModelManager {

    public ModelManager() {
        try {
            Path source = Path.of("models_source");
            Path rp = Path.of("resource_pack");
            Path data = Path.of("models_data");

            // CLEANUP PREVIOUS GENERATIONS
            if (Files.exists(rp)) {
                System.out.println("Cleaning up old resource pack...");
                deleteDirectory(rp);
            }
            if (Files.exists(data)) {
                System.out.println("Cleaning up old model data...");
                deleteDirectory(data);
            }
            Files.createDirectories(rp);
            Files.createDirectories(data);

            if (!Files.exists(source)) {
                Files.createDirectories(source);
                System.out.println("Created 'models_source' directory. Put your .bbmodel files here.");
                return;
            }

            // Generate Resource Pack and Model Data
            System.out.println("Generating Model Resource Pack...");
            var config = PackBuilder.generate(source, rp, data);
            
            String mappings = config.modelMappings();
            System.out.println("--- DEBUG: WSEE Model Mappings ---");
            System.out.println(mappings);
            System.out.println("----------------------------------");

            // Load mappings into Engine
            System.out.println("Loading Model Mappings...");
            ModelEngine.loadMappings(new StringReader(mappings), data);
            
            // Force the material to match what was generated in the resource pack
            ModelEngine.setModelMaterial(net.minestom.server.item.Material.MAGMA_CREAM);
            
            System.out.println("Models loaded successfully with MAGMA_CREAM material!");

        } catch (Exception e) {
            System.err.println("Failed to load models:");
            e.printStackTrace();
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        try (var walk = Files.walk(path)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(java.io.File::delete);
        }
    }
}
