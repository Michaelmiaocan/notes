package com.myAlgorithm.lottery;

import com.alibaba.fastjson.JSONArray;
import com.myAlgorithm.lottery.LotteryAlgorithm;
import com.myAlgorithm.lottery.LotteryEntity;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LotteryTest {


    @Test
    public void lotteryTest() {
        int times = 1000000;
        System.out.println("模拟" + times + "次:");
        List<LotteryEntity> lotteryList = new ArrayList<LotteryEntity>();
        lotteryList.add(new LotteryEntity("套套", 1, 0.12));
        lotteryList.add(new LotteryEntity("电脑", 2, 0.22));
        lotteryList.add(new LotteryEntity("美女", 3, 0.41));
        lotteryList.add(new LotteryEntity("车", 3, 0.25));
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        List<Double> originRateList = new ArrayList<Double>();
        for (LotteryEntity lotteryEntity : lotteryList) {
            originRateList.add(lotteryEntity.getRate());
        }
        for (int i = 0; i < times; i++) {
            int id = LotteryAlgorithm.getLottery(originRateList);
            if (map.containsKey(id)) {
                map.put(id, map.get(id) + 1);
            } else {
                map.put(id, 1);
            }
        }
        for (Integer id : map.keySet()) {
            System.out.println(lotteryList.get(id).getName() + "的原始概率=" + lotteryList.get(id).getRate() + "，模拟后的概率=" + map.get(id) / (times * 1.0f));
        }
        System.out.println();
    }

    @Test
    public void gaussianDistributionTest() {
        int times = 1000000;
        System.out.println("模拟" + times + "次:");
        double ave = 10;
        System.out.println("预期的平均数="+ave);
        double sqrt = 7;
        double sum = 0;
        JSONArray jsonArray=new JSONArray();
        for (int i=0;i<times;i++){
            Double a= LotteryAlgorithm.getRandom(ave, sqrt);
            sum += a;
            if (i<100){
                jsonArray.add(a.intValue());
            }
        }
        System.out.println("模拟数据的平均数="+sum/times);
        System.out.println("生成的前100个随机数:"+jsonArray.toJSONString());
    }

}
