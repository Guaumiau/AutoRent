package com.autorent.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClienteController {

    @GetMapping("/cliente/dashboard")
    public String dashboardCliente() {
        return "cliente/dashboard";
    }
}
