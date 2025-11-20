package com.autorent.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PropietarioController {

    @GetMapping("/propietario/dashboard")
    public String dashboardPropietario() {
        return "propietario/dashboard";
    }
}
