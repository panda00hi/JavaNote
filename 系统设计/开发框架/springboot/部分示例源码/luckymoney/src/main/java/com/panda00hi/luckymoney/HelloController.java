package com.panda00hi.luckymoney;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * @author panda00hi
 * 2020/2/17
 */
@RestController
@RequestMapping("project")
// @Controller
public class HelloController {
    // // @Value注解，载入配置中的值
    // @Value("${minMoney}")
    // private BigDecimal minMoney;
    //
    // @Value("${description}")
    // private String description;

    @Autowired
    private LimitConfig limitConfig;

    @RequestMapping({"/test1", "/demo1"})
    public String say() {

        return "说明" + limitConfig.getDescription();
        // return "index";
    }

    @GetMapping("/say")
    public String say2(@RequestParam(value = "id", required = false,defaultValue = "默认0") String id1) {
        return "id:" + id1;
    }


}
