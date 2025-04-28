//package com.sokoby.controller;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.Environment;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/system")
//public class SystemController {
//
//    @Autowired
//    private Environment env;
//
//    @GetMapping("/status")
//    public ResponseEntity<Map<String, Object>> getSystemStatus() {
//        Map<String, Object> status = new HashMap<>();
//
//        // Check if database is set to recreate
//        String ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto");
//        boolean isDatabaseRecreated = "create".equals(ddlAuto) || "create-drop".equals(ddlAuto);
//
//        status.put("isDatabaseRecreated", isDatabaseRecreated);
//        status.put("ddlAutoMode", ddlAuto);
//
//        return ResponseEntity.ok(status);
//    }
//}