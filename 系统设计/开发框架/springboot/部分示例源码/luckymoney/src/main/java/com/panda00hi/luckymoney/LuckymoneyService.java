package com.panda00hi.luckymoney;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author panda00hi
 * 2020/2/18
 */
@Service
public class LuckymoneyService {

    @Autowired
    private LuckymoneyRepository repository;

    /**
     * 事务 指数据库事务
     * 确保方法中涉及修改数据库的操作要么都成功，要么都失败。
     */
    @Transactional
    public void createTwo() {
        Luckymoney luckymoney1 = new Luckymoney();
        luckymoney1.setProducer("刘1");
        luckymoney1.setMoney(new BigDecimal("520.00"));
        repository.save(luckymoney1);

        Luckymoney luckymoney2 = new Luckymoney();
        luckymoney2.setProducer("刘1");
        luckymoney2.setMoney(new BigDecimal("1314.00"));
        repository.save(luckymoney2);

    }

}
