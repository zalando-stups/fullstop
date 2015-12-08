package org.zalando.stups.fullstop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author mrandi
 */
@Controller
public class HomeController {

    @RequestMapping({ "/", "/index" })
    public String home() {
        return "redirect:/swagger-ui.html";
    }

}
