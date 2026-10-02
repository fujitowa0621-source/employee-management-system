package com.example.employeemanagement;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
public class AuthController {
 private final JdbcTemplate jdbc;
 public AuthController(JdbcTemplate jdbc){this.jdbc=jdbc;}
 @PostMapping("/login")
 public Map<String,Object> login(@RequestBody Map<String,String> body,HttpSession session){
  String id=body.get("login_id"),pw=body.get("password");
  Integer count=jdbc.queryForObject(
   "SELECT COUNT(*) FROM login WHERE login_id=? AND password=?",
   Integer.class,id,pw);
  boolean ok=count!=null&&count>0;
  if(ok) session.setAttribute("login_id",id);
  return Map.of("success",ok,"message",ok?"ログインに成功しました。":"IDまたはパスワードが正しくありません。");
 }
 @PostMapping("/logout")
 public Map<String,Object> logout(HttpSession session){
  session.invalidate();
  return Map.of("success",true);
 }
}
