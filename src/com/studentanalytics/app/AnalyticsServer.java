
package com.studentanalytics.app;

import com.studentanalytics.util.DatabaseConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class AnalyticsServer {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
            new InetSocketAddress(PORT), 0
        );

        server.createContext("/api/stats", AnalyticsServer::getStats);
        server.createContext("/api/problems/status", AnalyticsServer::updateProblemStatus);
        server.createContext("/api/problems", AnalyticsServer::problems);
        server.createContext("/api/students", AnalyticsServer::students);
        server.createContext("/api/marks", AnalyticsServer::addMarks);
        server.createContext("/api/performance", AnalyticsServer::getPerformance);
        server.createContext("/api/topper", AnalyticsServer::getTopper);
        server.createContext("/", AnalyticsServer::serveWebsite);

        server.setExecutor(null);
        server.start();

        System.out.println("Student Analytics Server started!");
        System.out.println("Open http://localhost:8080");
    }

    // ---------- STUDENTS ----------

    private static void students(HttpExchange ex) throws IOException {

        if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
            getStudents(ex);
        }
        else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            addStudent(ex);
        }
        else {
            methodNotAllowed(ex);
        }
    }

    private static void getStudents(HttpExchange ex) throws IOException {

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        String sql = "SELECT student_id, name FROM students ORDER BY student_id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                if (!first) json.append(",");
                first = false;

                json.append("{\"id\":")
                    .append(rs.getInt("student_id"))
                    .append(",\"name\":")
                    .append(quote(rs.getString("name")))
                    .append("}");
            }

            json.append("]");
            sendJson(ex, 200, json.toString());

        } catch (Exception e) {
            sendError(ex, e);
        }
    }

    private static void addStudent(HttpExchange ex) throws IOException {

        Map<String, String> form = readForm(ex);

        String name = form.getOrDefault("name", "").trim();

        if (name.isEmpty()) {
            sendJson(ex, 400,
                "{\"error\":\"Student name is required.\"}");
            return;
        }

        String sql = "INSERT INTO students (name) VALUES (?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.executeUpdate();

            int id = 0;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                }
            }

            sendJson(ex, 201,
                "{\"message\":\"Student added successfully.\",\"id\":" + id + "}");

        } catch (Exception e) {
            sendError(ex, e);
        }
    }

    // ---------- DSA PROBLEMS ----------

    private static void problems(HttpExchange ex) throws IOException {

        if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
            getProblems(ex);
        }
        else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            addProblem(ex);
        }
        else {
            methodNotAllowed(ex);
        }
    }

    private static void getProblems(HttpExchange ex) throws IOException {

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        String sql = "SELECT problem_id, title, topic, difficulty, solved " +
                     "FROM dsa_problems ORDER BY problem_id DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                if (!first) json.append(",");
                first = false;

                json.append("{")
                    .append("\"id\":").append(rs.getInt("problem_id")).append(",")
                    .append("\"title\":").append(quote(rs.getString("title"))).append(",")
                    .append("\"topic\":").append(quote(rs.getString("topic"))).append(",")
                    .append("\"difficulty\":").append(quote(rs.getString("difficulty"))).append(",")
                    .append("\"solved\":").append(rs.getBoolean("solved"))
                    .append("}");
            }

            json.append("]");
            sendJson(ex, 200, json.toString());

        } catch (Exception e) {
            sendError(ex, e);
        }
    }

    private static void addProblem(HttpExchange ex) throws IOException {

        Map<String, String> form = readForm(ex);

        String title = form.getOrDefault("title", "").trim();
        String topic = form.getOrDefault("topic", "").trim();
        String difficulty = form.getOrDefault("difficulty", "").trim();

        if (title.isEmpty() || topic.isEmpty() || difficulty.isEmpty()) {
            sendJson(ex, 400,
                "{\"error\":\"Complete all DSA problem fields.\"}");
            return;
        }

        String sql = "INSERT INTO dsa_problems " +
                     "(title, topic, difficulty, solved) VALUES (?, ?, ?, 0)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, topic);
            ps.setString(3, difficulty);

            ps.executeUpdate();

            sendJson(ex, 201,
                "{\"message\":\"DSA problem added successfully.\"}");

        } catch (Exception e) {
            sendError(ex, e);
        }
    }

    private static void updateProblemStatus(HttpExchange ex) throws IOException {

        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            methodNotAllowed(ex);
            return;
        }

        Map<String, String> form = readForm(ex);

        try {

            int problemId = Integer.parseInt(
                form.getOrDefault("problemId", "")
            );

            boolean solved = Boolean.parseBoolean(
                form.getOrDefault("solved", "false")
            );

            String sql = "UPDATE dsa_problems SET solved = ? WHERE problem_id = ?";

            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setBoolean(1, solved);
                ps.setInt(2, problemId);

                int changed = ps.executeUpdate();

                if (changed == 0) {
                    sendJson(ex, 404,
                        "{\"error\":\"DSA problem not found.\"}");
                }
                else {
                    sendJson(ex, 200,
                        "{\"message\":\"Problem status updated.\"}");
                }
            }

        } catch (NumberFormatException e) {
            sendJson(ex, 400,
                "{\"error\":\"Invalid problem ID.\"}");
        }
        catch (Exception e) {
            sendError(ex, e);
        }
    }

    // ---------- MARKS ----------

    private static void addMarks(HttpExchange ex) throws IOException {

        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            methodNotAllowed(ex);
            return;
        }

        Map<String, String> form = readForm(ex);

        try {

            int studentId = Integer.parseInt(
                form.getOrDefault("studentId", "")
            );

            String subject = form.getOrDefault("subject", "").trim();

            double marks = Double.parseDouble(
                form.getOrDefault("marks", "")
            );

            double maxMarks = Double.parseDouble(
                form.getOrDefault("maxMarks", "")
            );

            if (subject.isEmpty() || marks < 0 ||
                maxMarks <= 0 || marks > maxMarks) {

                sendJson(ex, 400,
                    "{\"error\":\"Enter a subject and valid marks.\"}");
                return;
            }

            String sql = "INSERT INTO marks " +
                "(student_id, subject, marks, max_marks) VALUES (?, ?, ?, ?)";

            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, studentId);
                ps.setString(2, subject);
                ps.setDouble(3, marks);
                ps.setDouble(4, maxMarks);

                ps.executeUpdate();

                sendJson(ex, 201,
                    "{\"message\":\"Marks added successfully.\"}");
            }

        } catch (NumberFormatException e) {

            sendJson(ex, 400,
                "{\"error\":\"Enter valid student ID and numeric marks.\"}");

        } catch (Exception e) {
            sendError(ex, e);
        }
    }

    // ---------- DASHBOARD STATISTICS ----------

    private static void getStats(HttpExchange ex) throws IOException {

        try (Connection con = DatabaseConnection.getConnection()) {

            int students = count(con, "SELECT COUNT(*) FROM students");

            int total = count(con,
                "SELECT COUNT(*) FROM dsa_problems");

            int solved = count(con,
                "SELECT COUNT(*) FROM dsa_problems WHERE solved = 1");

            int unsolved = total - solved;

            double completion = total == 0
                ? 0 : solved * 100.0 / total;

            String json = String.format(Locale.US,
                "{\"students\":%d,\"totalProblems\":%d," +
                "\"solved\":%d,\"unsolved\":%d,\"completion\":%.2f}",
                students, total, solved, unsolved, completion);

            sendJson(ex, 200, json);

        } catch (Exception e) {
            sendError(ex, e);
        }
    }

    // ---------- PERFORMANCE ----------

    private static void getPerformance(HttpExchange ex) throws IOException {

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        String sql = "SELECT s.student_id, s.name, " +
            "COALESCE(SUM(m.marks), 0) AS total_marks, " +
            "COALESCE(SUM(m.max_marks), 0) AS max_marks " +
            "FROM students s LEFT JOIN marks m " +
            "ON s.student_id = m.student_id " +
            "GROUP BY s.student_id, s.name ORDER BY s.student_id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                if (!first) json.append(",");
                first = false;

                double obtained = rs.getDouble("total_marks");
                double maximum = rs.getDouble("max_marks");

                double percentage = maximum == 0
                    ? 0 : obtained * 100.0 / maximum;

                json.append("{")
                    .append("\"id\":").append(rs.getInt("student_id")).append(",")
                    .append("\"name\":").append(quote(rs.getString("name"))).append(",")
                    .append("\"totalMarks\":").append(number(obtained)).append(",")
                    .append("\"maxMarks\":").append(number(maximum)).append(",")
                    .append("\"percentage\":").append(number(percentage)).append(",")
                    .append("\"grade\":").append(quote(grade(percentage)))
                    .append("}");
            }

            json.append("]");
            sendJson(ex, 200, json.toString());

        } catch (Exception e) {
            sendError(ex, e);
        }
    }

    // ---------- TOPPER ----------

    private static void getTopper(HttpExchange ex) throws IOException {

        String sql = "SELECT s.student_id, s.name, " +
            "SUM(m.marks) AS total_marks, " +
            "SUM(m.max_marks) AS max_marks " +
            "FROM students s JOIN marks m " +
            "ON s.student_id = m.student_id " +
            "GROUP BY s.student_id, s.name " +
            "HAVING SUM(m.max_marks) > 0 " +
            "ORDER BY SUM(m.marks) / SUM(m.max_marks) DESC LIMIT 1";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                sendJson(ex, 200, "null");
                return;
            }

            double percentage =
                rs.getDouble("total_marks") * 100.0 /
                rs.getDouble("max_marks");

            String json = "{\"id\":" + rs.getInt("student_id") +
                ",\"name\":" + quote(rs.getString("name")) +
                ",\"percentage\":" + number(percentage) +
                ",\"grade\":" + quote(grade(percentage)) + "}";

            sendJson(ex, 200, json);

        } catch (Exception e) {
            sendError(ex, e);
        }
    }

    // ---------- COMMON HELPERS ----------

    private static Map<String, String> readForm(HttpExchange ex)
            throws IOException {

        String body;

        try (InputStream input = ex.getRequestBody()) {
            body = new String(
                input.readAllBytes(),
                StandardCharsets.UTF_8
            );
        }

        Map<String, String> values = new HashMap<>();

        if (body.isEmpty()) {
            return values;
        }

        for (String pair : body.split("&")) {

            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(
                parts[0], StandardCharsets.UTF_8
            );

            String value = parts.length > 1
                ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                : "";

            values.put(key, value);
        }

        return values;
    }

    private static int count(Connection con, String sql)
            throws SQLException {

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            rs.next();
            return rs.getInt(1);
        }
    }

    private static String grade(double p) {

        if (p >= 90) return "A+";
        if (p >= 80) return "A";
        if (p >= 70) return "B";
        if (p >= 60) return "C";
        if (p >= 50) return "D";

        return "F";
    }

    private static String number(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static String quote(String value) {

        if (value == null) return "null";

        StringBuilder out = new StringBuilder("\"");

        for (char c : value.toCharArray()) {

            switch (c) {

                case '"':
                    out.append("\\\"");
                    break;

                case '\\':
                    out.append("\\\\");
                    break;

                case '\n':
                    out.append("\\n");
                    break;

                case '\r':
                    out.append("\\r");
                    break;

                case '\t':
                    out.append("\\t");
                    break;

                default:

                    if (c < 32) {
                        out.append(
                            String.format("\\u%04x", (int)c)
                        );
                    }
                    else {
                        out.append(c);
                    }
            }
        }

        return out.append("\"").toString();
    }

    private static void methodNotAllowed(HttpExchange ex)
            throws IOException {

        sendJson(ex, 405,
            "{\"error\":\"Method not allowed.\"}");
    }

    private static void sendError(HttpExchange ex, Exception e)
            throws IOException {

        e.printStackTrace();

        sendJson(ex, 500,
            "{\"error\":" + quote(e.getMessage()) + "}");
    }

    private static void sendJson(
            HttpExchange ex, int status, String response)
            throws IOException {

        byte[] data = response.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set(
            "Content-Type", "application/json; charset=UTF-8"
        );

        ex.getResponseHeaders().set("Cache-Control", "no-store");

        ex.sendResponseHeaders(status, data.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    // ---------- WEBSITE FILES ----------

    private static void serveWebsite(HttpExchange ex)
            throws IOException {

        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            methodNotAllowed(ex);
            return;
        }

        String path = ex.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        Path root = Paths.get("web").toAbsolutePath().normalize();

        Path file = root.resolve(path.substring(1)).normalize();

        if (!file.startsWith(root) ||
            !Files.exists(file) ||
            Files.isDirectory(file)) {

            byte[] data = "File not found".getBytes(
                StandardCharsets.UTF_8
            );

            ex.sendResponseHeaders(404, data.length);

            try (OutputStream os = ex.getResponseBody()) {
                os.write(data);
            }

            return;
        }

        String type = "text/plain; charset=UTF-8";

        if (path.endsWith(".html")) {
            type = "text/html; charset=UTF-8";
        }

        if (path.endsWith(".css")) {
            type = "text/css; charset=UTF-8";
        }

        if (path.endsWith(".js")) {
            type = "application/javascript; charset=UTF-8";
        }

        byte[] data = Files.readAllBytes(file);

        ex.getResponseHeaders().set("Content-Type", type);

        ex.sendResponseHeaders(200, data.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }
}
