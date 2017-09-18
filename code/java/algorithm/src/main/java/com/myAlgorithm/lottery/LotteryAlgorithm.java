package com.myAlgorithm.lottery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LotteryAlgorithm {

    /**
     * 产生随机数
     *
     * @param ave  均值
     * @param sqrt 方差
     * @return
     */
    public static double getRandom(double ave, double sqrt) {
        Random random = new Random();
        return Math.sqrt(sqrt) * random.nextGaussian() + ave;
    }

    /**
     * 加权概率抽奖
     *
     * @param originRateList 原始的概率列表，保证顺序和实际奖品对应
     * @return 奖品在lotteryList里面的索引
     */
    public static int getLottery(List<Double> originRateList) {

        if (originRateList == null || originRateList.isEmpty()) {
            return -1;
        }

        int size = originRateList.size();
        // 计算总概率，保证总概率不一定是1的情况(其中某个奖品不足或者又改需求了)
        double sumRate = 0d;

        for (double rate : originRateList) {
            sumRate += rate;
        }

        // 计算每个物品在总概率的基础下的概率情况
        List<Double> sortOriginRateList = new ArrayList<Double>(size);
        Double tempSumRate = 0d;

        for (double rate : originRateList) {
            tempSumRate += rate;
            sortOriginRateList.add(tempSumRate / sumRate);
        }

        // 根据区块值来获取抽取到的物品索引
        double nextDouble = Math.random();
        sortOriginRateList.add(nextDouble);
        Collections.sort(sortOriginRateList);
        return sortOriginRateList.indexOf(nextDouble);
    }
}
