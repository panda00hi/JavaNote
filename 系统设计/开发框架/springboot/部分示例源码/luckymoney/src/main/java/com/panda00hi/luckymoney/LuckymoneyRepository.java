package com.panda00hi.luckymoney;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 持久层接口
 * 继承JpaRepository，两个参数，一个实体类，一个id类型
 * @author panda00hi
 * 2020/2/18
 */
public interface LuckymoneyRepository extends JpaRepository<Luckymoney, Integer> {
}
