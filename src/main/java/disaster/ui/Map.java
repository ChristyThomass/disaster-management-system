package disaster.ui;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * OpenStreetMap Engine & Slippy Tile Manager for Kerala SDMA Portal.
 * Handles Web Mercator projection, multi-tier caching (RAM + disk),
 * and asynchronous background tile streaming directly from OpenStreetMap.
 */
public class Map {

    public enum TileProvider {
        OSM_STANDARD("OpenStreetMap Standard", "https://tile.openstreetmap.org/{z}/{x}/{y}.png", "© OpenStreetMap contributors");

        public final String displayName;
        public final String urlPattern;
        public final String attribution;

        TileProvider(String displayName, String urlPattern, String attribution) {
            this.displayName = displayName;
            this.urlPattern = urlPattern;
            this.attribution = attribution;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static Map instance;

    // In-memory LRU cache (capped at 400 tiles ~ 25MB RAM)
    private final int MAX_MEMORY_TILES = 400;
    private final java.util.Map<String, BufferedImage> memoryCache = Collections.synchronizedMap(
            new LinkedHashMap<String, BufferedImage>(MAX_MEMORY_TILES, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, BufferedImage> eldest) {
                    return size() > MAX_MEMORY_TILES;
                }
            }
    );

    // Persistent disk cache root
    private final File diskCacheDir;

    // Asynchronous download worker pool
    private final ExecutorService executor = Executors.newFixedThreadPool(4, new ThreadFactory() {
        private int count = 0;
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "OsmTileWorker-" + (++count));
            t.setDaemon(true);
            return t;
        }
    });

    // In-flight download tracker to prevent duplicate requests
    private final Set<String> inFlightRequests = Collections.synchronizedSet(new HashSet<>());

    // Failed requests set to prevent rapid re-fetching within session
    private final Set<String> failedRequests = Collections.synchronizedSet(new HashSet<>());

    private Map() {
        File localDir = new File("cache/osm_tiles");
        if (!localDir.exists()) {
            localDir.mkdirs();
        }
        if (!localDir.canWrite()) {
            localDir = new File(System.getProperty("user.home"), ".sdma_osm_cache");
            localDir.mkdirs();
        }
        this.diskCacheDir = localDir;
    }

    public static synchronized Map getInstance() {
        if (instance == null) {
            instance = new Map();
        }
        return instance;
    }

    /**
     * Retrieve a tile image. If present in RAM or disk, returns immediately.
     * Otherwise returns null and queues an async background download.
     */
    public BufferedImage getTile(TileProvider provider, int zoom, int x, int y, Runnable onLoadedCallback) {
        int maxIndex = (1 << zoom) - 1;
        if (x < 0 || x > maxIndex || y < 0 || y > maxIndex) {
            return null;
        }

        String key = provider.name() + "/" + zoom + "/" + x + "/" + y;

        // 1. Check RAM cache
        BufferedImage ramImg = memoryCache.get(key);
        if (ramImg != null) {
            return ramImg;
        }

        // 2. Check Disk Cache
        File tileFile = getDiskFile(provider, zoom, x, y);
        if (tileFile.exists() && tileFile.length() > 0) {
            try {
                BufferedImage diskImg = ImageIO.read(tileFile);
                if (diskImg != null) {
                    memoryCache.put(key, diskImg);
                    return diskImg;
                }
            } catch (Exception ignored) {
                tileFile.delete();
            }
        }

        // 3. Avoid hammering failed URLs
        if (failedRequests.contains(key)) {
            return null;
        }

        // 4. Queue Async Download
        if (!inFlightRequests.contains(key)) {
            inFlightRequests.add(key);
            executor.submit(() -> {
                try {
                    String tileUrl = provider.urlPattern
                            .replace("{z}", String.valueOf(zoom))
                            .replace("{x}", String.valueOf(x))
                            .replace("{y}", String.valueOf(y));

                    URL url = URI.create(tileUrl).toURL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(4500);
                    conn.setReadTimeout(6000);
                    conn.setRequestProperty("User-Agent", "SmartDisasterManagementSystem/2.0 (Kerala SDMA Portal; admin@keralasdma.gov.in)");
                    conn.setRequestProperty("Accept", "image/png,image/webp,image/*;q=0.9");

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        try (InputStream in = conn.getInputStream();
                             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                            byte[] buffer = new byte[4096];
                            int read;
                            while ((read = in.read(buffer)) != -1) {
                                baos.write(buffer, 0, read);
                            }
                            byte[] data = baos.toByteArray();
                            if (data.length > 0) {
                                tileFile.getParentFile().mkdirs();
                                Files.write(tileFile.toPath(), data);

                                BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                                if (img != null) {
                                    memoryCache.put(key, img);
                                    if (onLoadedCallback != null) {
                                        SwingUtilities.invokeLater(onLoadedCallback);
                                    }
                                }
                            }
                        }
                    } else {
                        failedRequests.add(key);
                    }
                } catch (Exception e) {
                    failedRequests.add(key);
                } finally {
                    inFlightRequests.remove(key);
                }
            });
        }

        return null;
    }

    private File getDiskFile(TileProvider provider, int zoom, int x, int y) {
        return new File(diskCacheDir, provider.name().toLowerCase() + "/" + zoom + "/" + x + "/" + y + ".png");
    }

    public void clearMemoryCache() {
        memoryCache.clear();
        failedRequests.clear();
    }

    // =========================================================================
    // WEB MERCATOR (EPSG:3857) PROJECTION UTILITIES
    // =========================================================================

    public static double lonToWorldX(double lon, int zoom) {
        double worldSize = 256.0 * (1 << zoom);
        return (lon + 180.0) / 360.0 * worldSize;
    }

    public static double latToWorldY(double lat, int zoom) {
        double worldSize = 256.0 * (1 << zoom);
        double sinLat = Math.sin(Math.toRadians(lat));
        sinLat = Math.max(-0.9999, Math.min(0.9999, sinLat));
        return (0.5 - Math.log((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * Math.PI)) * worldSize;
    }

    public static double worldXToLon(double worldX, int zoom) {
        double worldSize = 256.0 * (1 << zoom);
        double lon = (worldX / worldSize) * 360.0 - 180.0;
        while (lon < -180.0) lon += 360.0;
        while (lon > 180.0) lon -= 360.0;
        return lon;
    }

    public static double worldYToLat(double worldY, int zoom) {
        double worldSize = 256.0 * (1 << zoom);
        double n = Math.PI - (2.0 * Math.PI * worldY) / worldSize;
        double lat = Math.toDegrees(Math.atan(Math.sinh(n)));
        return Math.max(-85.0511, Math.min(85.0511, lat));
    }

    public static double getGroundResolution(double lat, int zoom) {
        return 156543.03392 * Math.cos(Math.toRadians(lat)) / (1 << zoom);
    }
}
