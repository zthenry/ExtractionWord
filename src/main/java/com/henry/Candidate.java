package com.henry;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: zhangtao13
 * Date: 15/11/16
 * Time: 下午2:55
 */
public class Candidate {

    //词
    private String keyword;

    //左邻字集合
    private Map<String,Integer> leftMap = new HashMap<String, Integer>();

    //右邻字集合
    private Map<String,Integer> rightMap = new HashMap<String, Integer>();

    //出现的频次
    private AtomicInteger frequence = new AtomicInteger(0);

    private double leftEntropy=0;

    private double rigthEntropy=0;

    private double entropy=0;

    public void setEntropy(double entropy) {
        this.entropy = entropy;
    }

    public Candidate(String keyword,AtomicInteger frequence){
        this.keyword=keyword;
        this.frequence=frequence;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Map<String, Integer> getLeftMap() {
        return leftMap;
    }

    public void setLeftMap(Map<String, Integer> leftMap) {
        this.leftMap = leftMap;
    }

    public Map<String, Integer> getRightMap() {
        return rightMap;
    }

    public void setRightMap(Map<String, Integer> rightMap) {
        this.rightMap = rightMap;
    }

    public AtomicInteger getFrequence() {
        return frequence;
    }

    public void setFrequence(AtomicInteger frequence) {
        this.frequence = frequence;
    }

    public int getKeywordLength(){
        if (keyword==null){
            return 0;
        }
        return keyword.length();
    }

    public double getLeftEntropy() {
        return leftEntropy;
    }

    public void setLeftEntropy(double leftEntropy) {
        this.leftEntropy = leftEntropy;
    }

    public double getRigthEntropy() {
        return rigthEntropy;
    }

    public void setRigthEntropy(double rigthEntropy) {
        this.rigthEntropy = rigthEntropy;
    }

    public void incrementFrequence(){
        frequence.incrementAndGet();
    }


    public double computerLeft(){
        if (leftEntropy!=0){
            return leftEntropy;
        }
        BigDecimal entropy = new BigDecimal(0);
        int total = 0;
        for (String s : leftMap.keySet()) {
            total = total + leftMap.get(s);
        }

        BigDecimal totalBD = new BigDecimal(total);
        for (String s : leftMap.keySet()) {
            Integer frequence = leftMap.get(s);
            BigDecimal frequenceBigDecimal = new BigDecimal(frequence);
            BigDecimal p = frequenceBigDecimal.divide(totalBD,16,BigDecimal.ROUND_HALF_UP);
            BigDecimal log = new BigDecimal(Math.log(p.doubleValue()));
            BigDecimal sub = p.multiply(log);
            entropy = entropy.add(sub);
        }

        this.setLeftEntropy(-entropy.doubleValue());
        return -entropy.doubleValue();
    }


    public double computerRight(){
        if (rigthEntropy!=0){
            return rigthEntropy;
        }
        BigDecimal entropy = new BigDecimal(0);
        int total = 0;
        for (String s : rightMap.keySet()) {
            total = total + rightMap.get(s);
        }

        BigDecimal totalBD = new BigDecimal(total);
        for (String s : rightMap.keySet()) {
            Integer frequence = rightMap.get(s);
            BigDecimal frequenceBigDecimal = new BigDecimal(frequence);
            BigDecimal p = frequenceBigDecimal.divide(totalBD,16,BigDecimal.ROUND_HALF_UP);
            BigDecimal log = new BigDecimal(Math.log(p.doubleValue()));
            BigDecimal sub = p.multiply(log);
            entropy = entropy.add(sub);
        }

        this.setRigthEntropy(-entropy.doubleValue());
        return -entropy.doubleValue();
    }


    public double getEntropy(){
        double left = computerLeft();
        double right = computerRight();
        double entropy = left >right?right:left;
        this.setEntropy(entropy);
        return entropy;
    }
}
