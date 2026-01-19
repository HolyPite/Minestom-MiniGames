package me.holypite.manager;

import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.stream.Stream;

public class ResourcePackManager {

    private static final int PORT = 8080;
    private static final String PACK_DIR = "resource_pack";
    private static final String CUSTOM_ASSETS_DIR = "custom_assets";
    private static final String ZIP_FILE = "resource_pack.zip";
    private String hash;
    private HttpServer httpServer;

    public ResourcePackManager() {
        try {
            Path sourceDir = Path.of(PACK_DIR);
            Path customDir = Path.of(CUSTOM_ASSETS_DIR);
            Path zipPath = Path.of(ZIP_FILE);
            
            if (Files.exists(sourceDir)) {
                // 1. Merge custom assets into the generated pack
                if (Files.exists(customDir)) {
                    System.out.println("Merging custom assets from '" + CUSTOM_ASSETS_DIR + "'...");
                    mergeDirectories(customDir, sourceDir);
                }

                // 2. Zip the resource pack folder
                System.out.println("Zipping resource pack...");
                zipFolder(sourceDir, zipPath);
                
                // 3. Calculate Hash
                this.hash = calculateSha1(zipPath);
                System.out.println("Resource Pack Hash: " + hash);

                // 3. Start HTTP Server
                startServer(zipPath);

                // Determine URL (Try to get LAN IP, fallback to localhost)
                String ip = "localhost";
                try {
                    ip = InetAddress.getLocalHost().getHostAddress();
                } catch (Exception e) {
                    System.err.println("Could not determine local IP, using localhost.");
                }
                final String downloadUrl = "http://" + ip + ":" + PORT + "/resource_pack.zip";
                System.out.println("Resource Pack URL: " + downloadUrl);

                // 4. Register Event to send pack
                GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();
                handler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
                    System.out.println("Sending Resource Pack request to " + event.getPlayer().getUsername());
                    
                    ResourcePackInfo packInfo = ResourcePackInfo.resourcePackInfo(
                            UUID.randomUUID(),
                            URI.create(downloadUrl),
                            hash
                    );
                    
                    event.getPlayer().sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                            .packs(packInfo)
                            .required(true)
                            .prompt(net.kyori.adventure.text.Component.text("Téléchargement des modèles 3D requis."))
                            .build()
                    );
                });
            } else {
                System.err.println("Resource Pack folder not found: " + PACK_DIR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startServer(Path zipPath) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0); // Listen on all interfaces
        httpServer.createContext("/resource_pack.zip", exchange -> {
            System.out.println("Incoming HTTP Request for Resource Pack from: " + exchange.getRemoteAddress());
            try {
                byte[] bytes = Files.readAllBytes(zipPath);
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
                System.out.println("Resource Pack sent successfully (" + bytes.length + " bytes).");
            } catch (Exception e) {
                System.err.println("Failed to send Resource Pack via HTTP:");
                e.printStackTrace();
                exchange.sendResponseHeaders(500, 0);
                exchange.close();
            }
        });
        httpServer.setExecutor(null); // creates a default executor
        httpServer.start();
        System.out.println("Resource Pack Server started on port " + PORT);
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    // ---

    private void zipFolder(Path sourceDirPath, Path zipFilePath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath.toFile()));
             Stream<Path> paths = Files.walk(sourceDirPath)) {
            paths.filter(path -> !Files.isDirectory(path))
                 .forEach(path -> {
                     ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString().replace("\\", "/"));
                     try {
                         zos.putNextEntry(zipEntry);
                         Files.copy(path, zos);
                         zos.closeEntry();
                     } catch (IOException e) {
                         System.err.println(e);
                     }
                 });
        }
    }

    private void mergeDirectories(Path source, Path target) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            paths.forEach(sourcePath -> {
                Path targetPath = target.resolve(source.relativize(sourcePath));
                try {
                    if (Files.isDirectory(sourcePath)) {
                        if (!Files.exists(targetPath)) {
                            Files.createDirectory(targetPath);
                        }
                    } else {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to merge file: " + sourcePath);
                    e.printStackTrace();
                }
            });
        }
    }

    private String calculateSha1(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream fis = Files.newInputStream(path)) {
            int n = 0;
            byte[] buffer = new byte[8192];
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
        }
        byte[] hash = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
