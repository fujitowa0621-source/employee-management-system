package com.example.employeemanagement;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final JdbcTemplate jdbc;

    public EmployeeController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private ResponseEntity<?> auth(HttpSession session) {
        if (session.getAttribute("login_id") == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "ログインが必要です。"));
        }
        return null;
    }

    // 社員一覧
    @GetMapping
    public ResponseEntity<?> list(HttpSession session) {

        var authError = auth(session);
        if (authError != null) {
            return authError;
        }

        var employees = jdbc.queryForList("""
                SELECT
                    e.employee_id,
                    e.name,
                    d.department_name AS department,
                    e.position,
                    e.email
                FROM employees e
                JOIN departments d
                  ON e.department_id = d.department_id
                ORDER BY e.employee_id
                """);

        return ResponseEntity.ok(employees);
    }

    // 社員詳細
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(
            @PathVariable int id,
            HttpSession session) {

        var authError = auth(session);
        if (authError != null) {
            return authError;
        }

        var employees = jdbc.queryForList("""
                SELECT
                    e.employee_id,
                    e.name,
                    d.department_name AS department,
                    e.position,
                    e.email
                FROM employees e
                JOIN departments d
                  ON e.department_id = d.department_id
                WHERE e.employee_id = ?
                """, id);

        if (employees.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "社員が見つかりません。"));
        }

        return ResponseEntity.ok(employees.get(0));
    }

    // 社員登録
    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        var authError = auth(session);
        if (authError != null) {
            return authError;
        }

        String error = validate(body);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", error));
        }

        Integer departmentId =
                getDepartmentId(body.get("department"));

        if (departmentId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "部署が存在しません: " + body.get("department")
                    ));
        }

        jdbc.update("""
                INSERT INTO employees
                    (department_id, name, position, email)
                VALUES (?, ?, ?, ?)
                """,
                departmentId,
                body.get("name").trim(),
                emptyToNull(body.get("position")),
                body.get("email").trim()
        );

        return ResponseEntity.status(201)
                .body(Map.of(
                        "message",
                        "社員を登録しました。"
                ));
    }

    // 社員編集
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable int id,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        var authError = auth(session);
        if (authError != null) {
            return authError;
        }

        String error = validate(body);

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", error));
        }

        Integer departmentId =
                getDepartmentId(body.get("department"));

        if (departmentId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "部署が存在しません: " + body.get("department")
                    ));
        }

        int updated = jdbc.update("""
                UPDATE employees
                SET
                    department_id = ?,
                    name = ?,
                    position = ?,
                    email = ?
                WHERE employee_id = ?
                """,
                departmentId,
                body.get("name").trim(),
                emptyToNull(body.get("position")),
                body.get("email").trim(),
                id
        );

        if (updated == 0) {
            return ResponseEntity.status(404)
                    .body(Map.of(
                            "message",
                            "社員が見つかりません。"
                    ));
        }

        return ResponseEntity.ok(
                Map.of("message", "社員情報を更新しました。")
        );
    }

    // 社員削除
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable int id,
            HttpSession session) {

        var authError = auth(session);
        if (authError != null) {
            return authError;
        }

        int deleted = jdbc.update(
                "DELETE FROM employees WHERE employee_id = ?",
                id
        );

        if (deleted == 0) {
            return ResponseEntity.status(404)
                    .body(Map.of(
                            "message",
                            "社員が見つかりません。"
                    ));
        }

        return ResponseEntity.ok(
                Map.of("message", "社員を削除しました。")
        );
    }

    // 部署ID取得
    private Integer getDepartmentId(String departmentName) {

        if (departmentName == null ||
                departmentName.isBlank()) {
            return null;
        }

        String name = departmentName.trim();

        List<Map<String, Object>> departments =
                jdbc.queryForList(
                        "SELECT department_id, department_name FROM departments"
                );

        for (Map<String, Object> department : departments) {

            String dbName =
                    String.valueOf(
                            department.get("department_name")
                    ).trim();

            if (dbName.equals(name)) {

                Number id =
                        (Number) department.get("department_id");

                return id.intValue();
            }
        }

        return null;
    }

    // 入力チェック
    private String validate(
            Map<String, String> body) {

        String name = body.get("name");
        String department = body.get("department");
        String email = body.get("email");

        if (name == null || name.isBlank()) {
            return "氏名を入力してください。";
        }

        if (department == null ||
                department.isBlank()) {
            return "部署を選択してください。";
        }

        if (email == null || email.isBlank()) {
            return "メールアドレスを入力してください。";
        }

        if (!email.matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            return "メールアドレスの形式が正しくありません。";
        }

        return null;
    }

    private String emptyToNull(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}