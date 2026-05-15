package com.vipinsharma.interviewprep.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaFallbackController {

    @RequestMapping(value = { "/", "/new", "/sessions/**" })
    public String forward() {
        return "forward:/index.html";
    }
}
