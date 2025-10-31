package game.Happy_Zombie_Farm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    // корень сайта и /index → индекс
    @GetMapping({"/", "/index"})
    public String index() {
        return "forward:/index.html";
    }

    // fallback для «левых» урлов типа /profile, /map и т.п.,
    // НО мы не трогаем /api, /graphql, /auth и т.п.
    @GetMapping({
            "/{path:^(?!api|graphql|auth|actuator|ws).*$}",
            "/{path:^(?!api|graphql|auth|actuator|ws).*$}/**"
    })
    public String spaFallback() {
        return "forward:/index.html";
    }
}
