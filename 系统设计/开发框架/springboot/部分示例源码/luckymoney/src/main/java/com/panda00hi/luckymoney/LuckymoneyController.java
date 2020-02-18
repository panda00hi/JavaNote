package com.panda00hi.luckymoney;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author panda00hi
 * 2020/2/18
 */
@RestController
@RequestMapping("/demo01")
public class LuckymoneyController {

    @Autowired
    private LuckymoneyRepository repository;

    @Autowired
    private LuckymoneyService service;

    /**
     * 获取红包列表
     */
    @GetMapping("/luckymoneys")
    public List<Luckymoney> list() {
        List<Luckymoney> all = repository.findAll();
        return CollectionUtils.isEmpty(all) ? new ArrayList<>() : all;
    }

    /**
     * 创建红包
     */
    @PostMapping("/luckymoneys")
    public Luckymoney create(@RequestParam("money") BigDecimal mon,
                             @RequestParam("producer") String pro) {
        Luckymoney luckymoney = new Luckymoney();
        luckymoney.setProducer(pro);
        luckymoney.setMoney(mon);
        return repository.save(luckymoney);

    }

    /**
     * 通过id查询红包
     */
    @GetMapping("/luckymoneys/{id}")
    public Luckymoney findById(@PathVariable("id") Integer id) {

        return repository.findById(id).orElse(null);
    }

    /**
     * 通过id更新红包
     */
    @PutMapping("/luckymoneys/{id}")
    public Luckymoney update(@PathVariable("id") Integer id,
                             @RequestParam("consumer") String consumer) {

        Optional<Luckymoney> optional = repository.findById(id);
        if (optional.isPresent()) {
            Luckymoney luckymoney = optional.get();
            luckymoney.setConsumer(consumer);
            return repository.save(luckymoney);
        }
        return null;
    }

    @PostMapping("/luckymoneys/two")
    public void createTwo() {
        service.createTwo();
    }


}
