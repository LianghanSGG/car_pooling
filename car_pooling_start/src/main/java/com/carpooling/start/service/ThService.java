package com.carpooling.start.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author LiangHanSggg
 * @date 2023-07-18 19:34
 */
@Service
public class ThService {

    @Async("ordinaryThreadPool")
    public int t(int x) {
        System.out.println(Thread.currentThread().getName() + ":" + Thread.currentThread().getId());

        System.out.println("==============================");
        int i = 10 / 0;

        return -1;

    }

}
