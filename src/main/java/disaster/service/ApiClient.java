package disaster.service;

import disaster.model.DisasterReport;
import disaster.model.SOSAlert;
import disaster.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private static String baseUrl = "http://localhost:8085";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public static void setBaseUrl(String url) {
        baseUrl = url;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static class ApiResponse<T> {
        private final boolean success;
        private final T data;
        private final String error;
        private final String errorCode;

        public ApiResponse(boolean success, T data, String error, String errorCode) {
            this.success = success;
            this.data = data;
            this.error = error;
            this.errorCode = errorCode;
        }

        public boolean isSuccess() { return success; }
        public T getData() { return data; }
        public String getError() { return error; }
        public String getErrorCode() { return errorCode; }
    }

    public static boolean checkHealth() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/health"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public static ApiResponse<User> login(String username, String password) {
        String json = "{\"username\":\"" + escape(username) + "\",\"password\":\"" + escape(password) + "\"}";
        try {
            HttpResponse<String> resp = postJson("/api/users/login", json);
            Map<String, Object> body = parseResponse(resp.body());
            boolean success = Boolean.TRUE.equals(body.get("success"));
            if (success && body.get("data") instanceof Map) {
                Map<String, String> data = (Map<String, String>) body.get("data");
                User u = parseUser(data);
                return new ApiResponse<>(true, u, null, null);
            } else {
                String err = (String) body.getOrDefault("error", "Login failed");
                String code = (String) body.getOrDefault("errorCode", "AUTH_ERROR");
                return new ApiResponse<>(false, null, err, code);
            }
        } catch (Exception e) {
            return new ApiResponse<>(false, null, "Cannot connect to SDRP backend: " + e.getMessage(), "NETWORK_ERROR");
        }
    }

    public static ApiResponse<User> register(String username, String email, String password, String userType, double lat, double lon) {
        String json = "{" +
                "\"username\":\"" + escape(username) + "\"," +
                "\"email\":\"" + escape(email) + "\"," +
                "\"password\":\"" + escape(password) + "\"," +
                "\"userType\":\"" + escape(userType) + "\"," +
                "\"latitude\":" + lat + "," +
                "\"longitude\":" + lon +
                "}";
        try {
            HttpResponse<String> resp = postJson("/api/users/register", json);
            Map<String, Object> body = parseResponse(resp.body());
            boolean success = Boolean.TRUE.equals(body.get("success"));
            if (success && body.get("data") instanceof Map) {
                Map<String, String> data = (Map<String, String>) body.get("data");
                User u = parseUser(data);
                return new ApiResponse<>(true, u, null, null);
            } else {
                String err = (String) body.getOrDefault("error", "Registration failed");
                String code = (String) body.getOrDefault("errorCode", "REG_ERROR");
                return new ApiResponse<>(false, null, err, code);
            }
        } catch (Exception e) {
            return new ApiResponse<>(false, null, "Cannot connect to SDRP backend: " + e.getMessage(), "NETWORK_ERROR");
        }
    }

    public static ApiResponse<SOSAlert> createSOS(String userId, double lat, double lon, String urgency, String description) {
        String json = "{" +
                "\"userId\":\"" + escape(userId) + "\"," +
                "\"latitude\":" + lat + "," +
                "\"longitude\":" + lon + "," +
                "\"urgencyLevel\":\"" + escape(urgency) + "\"," +
                "\"description\":\"" + escape(description) + "\"" +
                "}";
        try {
            HttpResponse<String> resp = postJson("/api/sos/create", json);
            Map<String, Object> body = parseResponse(resp.body());
            boolean success = Boolean.TRUE.equals(body.get("success"));
            if (success && body.get("data") instanceof Map) {
                Map<String, String> data = (Map<String, String>) body.get("data");
                SOSAlert s = parseSOSAlert(data);
                return new ApiResponse<>(true, s, null, null);
            } else {
                String err = (String) body.getOrDefault("error", "Failed to dispatch SOS");
                return new ApiResponse<>(false, null, err, "SOS_ERROR");
            }
        } catch (Exception e) {
            return new ApiResponse<>(false, null, "Cannot connect to SDRP backend: " + e.getMessage(), "NETWORK_ERROR");
        }
    }

    public static ApiResponse<List<SOSAlert>> getActiveSOS() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/sos/active"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> body = parseResponse(resp.body());
            boolean success = Boolean.TRUE.equals(body.get("success"));
            if (success && body.get("data") instanceof List) {
                List<Map<String, String>> list = (List<Map<String, String>>) body.get("data");
                List<SOSAlert> results = new ArrayList<>();
                for (Map<String, String> item : list) {
                    results.add(parseSOSAlert(item));
                }
                return new ApiResponse<>(true, results, null, null);
            }
            return new ApiResponse<>(false, new ArrayList<>(), "Failed to load active SOS alerts", "FETCH_ERROR");
        } catch (Exception e) {
            return new ApiResponse<>(false, new ArrayList<>(), "Backend unreachable: " + e.getMessage(), "NETWORK_ERROR");
        }
    }

    public static ApiResponse<DisasterReport> submitDisasterReport(String userId, String type, double lat, double lon, String severity, String description, int affectedPeople) {
        String json = "{" +
                "\"userId\":\"" + escape(userId) + "\"," +
                "\"disasterType\":\"" + escape(type) + "\"," +
                "\"latitude\":" + lat + "," +
                "\"longitude\":" + lon + "," +
                "\"severity\":\"" + escape(severity) + "\"," +
                "\"description\":\"" + escape(description) + "\"," +
                "\"affectedPeople\":" + affectedPeople +
                "}";
        try {
            HttpResponse<String> resp = postJson("/api/disaster/report", json);
            Map<String, Object> body = parseResponse(resp.body());
            boolean success = Boolean.TRUE.equals(body.get("success"));
            if (success && body.get("data") instanceof Map) {
                Map<String, String> data = (Map<String, String>) body.get("data");
                DisasterReport r = parseDisasterReport(data);
                return new ApiResponse<>(true, r, null, null);
            } else {
                String err = (String) body.getOrDefault("error", "Failed to submit disaster report");
                return new ApiResponse<>(false, null, err, "REPORT_ERROR");
            }
        } catch (Exception e) {
            return new ApiResponse<>(false, null, "Cannot connect to SDRP backend: " + e.getMessage(), "NETWORK_ERROR");
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static HttpResponse<String> postJson(String endpoint, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + endpoint))
                .timeout(Duration.ofSeconds(4))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static User parseUser(Map<String, String> map) {
        User u = new User();
        u.setUserId(map.get("userId"));
        u.setUsername(map.get("username"));
        u.setEmail(map.get("email"));
        u.setUserType(map.get("userType"));
        u.setLatitude(parseDouble(map.get("latitude"), 0.0));
        u.setLongitude(parseDouble(map.get("longitude"), 0.0));
        return u;
    }

    private static SOSAlert parseSOSAlert(Map<String, String> map) {
        SOSAlert s = new SOSAlert();
        s.setSosId(map.get("sosId"));
        s.setUserId(map.get("userId"));
        s.setLatitude(parseDouble(map.get("latitude"), 0.0));
        s.setLongitude(parseDouble(map.get("longitude"), 0.0));
        s.setUrgencyLevel(map.get("urgencyLevel"));
        s.setDescription(map.get("description"));
        s.setStatus(map.get("status"));
        s.setRespondersCount((int) parseDouble(map.get("respondersCount"), 0.0));
        return s;
    }

    private static DisasterReport parseDisasterReport(Map<String, String> map) {
        DisasterReport r = new DisasterReport();
        r.setReportId(map.get("reportId"));
        r.setUserId(map.get("userId"));
        r.setDisasterType(map.get("disasterType"));
        r.setLatitude(parseDouble(map.get("latitude"), 0.0));
        r.setLongitude(parseDouble(map.get("longitude"), 0.0));
        r.setSeverity(map.get("severity"));
        r.setDescription(map.get("description"));
        r.setStatus(map.get("status"));
        r.setAffectedPeople((int) parseDouble(map.get("affectedPeople"), 0.0));
        r.setVerifiedBy(map.get("verifiedBy"));
        return r;
    }

    private static double parseDouble(String s, double defaultVal) {
        if (s == null) return defaultVal;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static Map<String, Object> parseResponse(String rawJson) {
        Map<String, Object> result = new HashMap<>();
        if (rawJson == null || rawJson.trim().isEmpty()) return result;
        String json = rawJson.trim();

        // Check success
        boolean success = json.contains("\"success\":true");
        result.put("success", success);

        // Extract error
        int errIdx = json.indexOf("\"error\":");
        if (errIdx != -1) {
            int start = json.indexOf("\"", errIdx + 8);
            if (start != -1) {
                int end = json.indexOf("\"", start + 1);
                if (end != -1) {
                    result.put("error", json.substring(start + 1, end));
                }
            }
        }

        // Extract data
        int dataIdx = json.indexOf("\"data\":");
        if (dataIdx != -1) {
            String dataPart = json.substring(dataIdx + 7).trim();
            if (dataPart.endsWith("}")) dataPart = dataPart.substring(0, dataPart.length() - 1).trim();

            if (dataPart.startsWith("{")) {
                result.put("data", extractObjectMap(dataPart));
            } else if (dataPart.startsWith("[")) {
                result.put("data", extractList(dataPart));
            }
        }
        return result;
    }

    private static Map<String, String> extractObjectMap(String objJson) {
        Map<String, String> map = new HashMap<>();
        String inner = objJson.trim();
        if (inner.startsWith("{")) inner = inner.substring(1);
        if (inner.endsWith("}")) inner = inner.substring(0, inner.length() - 1);

        boolean inQuotes = false;
        StringBuilder token = new StringBuilder();
        List<String> pairs = new ArrayList<>();

        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '"' && (i == 0 || inner.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (c == ',' && !inQuotes) {
                pairs.add(token.toString().trim());
                token.setLength(0);
            } else {
                token.append(c);
            }
        }
        if (token.length() > 0) pairs.add(token.toString().trim());

        for (String pair : pairs) {
            int colon = pair.indexOf(':');
            if (colon > 0) {
                String key = cleanStr(pair.substring(0, colon));
                String val = cleanStr(pair.substring(colon + 1));
                map.put(key, val);
            }
        }
        return map;
    }

    private static List<Map<String, String>> extractList(String arrayJson) {
        List<Map<String, String>> list = new ArrayList<>();
        String inner = arrayJson.trim();
        if (inner.startsWith("[")) inner = inner.substring(1);
        if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);

        int depth = 0;
        StringBuilder obj = new StringBuilder();
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '{') depth++;
            if (depth > 0) obj.append(c);
            if (c == '}') {
                depth--;
                if (depth == 0) {
                    list.add(extractObjectMap(obj.toString()));
                    obj.setLength(0);
                }
            }
        }
        return list;
    }

    private static String cleanStr(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
