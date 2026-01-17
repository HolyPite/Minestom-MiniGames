package me.holypite.manager;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkLoader;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryChunkLoader implements ChunkLoader {

    private final Map<Long, Chunk> chunks = new ConcurrentHashMap<>();

    @Override
    public @Nullable Chunk loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        long index = getChunkIndex(chunkX, chunkZ);
        return chunks.get(index);
    }

    @Override
    public void saveChunk(@NotNull Chunk chunk) {
        long index = getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        chunks.put(index, chunk);
    }
    
    // Attempting to keep these, but if they are not in interface, I'll remove them.
    // The previous error didn't complain about them specifically, but about the @Override on load/save.
    // However, if the interface changed significantly, maybe these are gone too.
    // I'll keep them for now, but remove @Override just in case they are not in the interface anymore.
    // Actually, safer to keep @Override and see if it fails. If so, remove.
    // The previous log showed "method does not override" for loadChunk/saveChunk but NOT for supportsParallel...
    // Wait, log line 32 and 37 were:
    // MemoryChunkLoader.java:32: error: method does not override or implement a method from a supertype
    // MemoryChunkLoader.java:37: error: method does not override or implement a method from a supertype
    // Those were supportsParallel... lines!
    
    // So supportsParallel... are ALSO gone from the interface.

    private long getChunkIndex(int x, int z) {
        return (long) x << 32 | (z & 0xFFFFFFFFL);
    }
}
