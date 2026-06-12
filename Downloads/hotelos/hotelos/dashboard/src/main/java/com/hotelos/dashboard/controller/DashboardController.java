package com.hotelos.dashboard.controller;

import com.hotelos.dashboard.websocket.DashboardBroadcaster;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class DashboardController {

    private final DashboardBroadcaster broadcaster;

    public DashboardController(DashboardBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/index.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/index.html";
    }

    @GetMapping("/api/state")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getState() {
        return ResponseEntity.ok(broadcaster.getCurrentState());
    }
}
