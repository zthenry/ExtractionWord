package com.henry;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: zhangtao13
 * Date: 15/11/15
 * Time: 下午4:17
 */
public class TextExtractionWord {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private HttpClient client = new DefaultHttpClient();
    private static final String specialChar = "，。；";
    public static void main(String[] args) {
        String text = "";
//        String text = "他踌躇了一会，终于决定还是自己送我去。我两三劝他不必去；他只说，不要紧，他们去不好";
        int wordMaxLength = 4;
        int frequencyThreshold = 5;

        TextExtractionWord textExtraction = new TextExtractionWord();

//        text = textExtraction.filterText(text);
        text = textExtraction.readText("/Users/henry/Downloads", "poi_name");
        int textLength = text.length();
//        System.out.println(text);
        Map<String,Candidate> candidatesMap = textExtraction.getCandidate(wordMaxLength, text);
//        System.out.println(candidatesMap.keySet());

        List<String> filteredKeywordList = new ArrayList<String>();
        for (String keyword : candidatesMap.keySet()) {
            if (keyword.length()<2){
                continue;
            }

            if (keyword.equals("将太无二")){
                System.out.println("####");
            }
            Candidate candidate = candidatesMap.get(keyword);
            AtomicInteger frequency = candidate.getFrequence();
            if (frequency.intValue() < frequencyThreshold){
                continue;
            }

            /**
             * 凝固度
             */
            double agglomerationDegree = textExtraction.computeAgglomerationDegree(keyword,frequency.intValue(),textLength,candidatesMap);


            /**
             * 自由度
             */
            double combinationDegree = textExtraction.computeCombinationDegree(candidate);

            if (combinationDegree<2 || agglomerationDegree< 1000){
                continue;
            }
//            System.out.println(keyword+":"+"agglomerationDegree:"+agglomerationDegree+",combinationDegree:"+combinationDegree);

            filteredKeywordList.add(keyword);
        }


        for (String key : filteredKeywordList) {
            List<String> tokens = textExtraction.getTokens(key, "text_ik");
            if (tokens.size()!=1){
                System.out.println(key+":"+tokens);
            }

        }



    }


    /**
     * 从text中获取长度不超过maxLength 的候选词
     * maxLength > 1
     * @param maxLength
     * @param text
     * @return
     */
    public Map<String,Candidate> getCandidate(int maxLength,String text){
        int initLength = 1;
        Map<String,Candidate> candidatesMap = new HashMap<String, Candidate>();
//        Set<String> candidates = new HashSet<String>();
        for (int i = initLength; i < maxLength+1; i++) {
            //获取长度为i的候选词
            for (int j = 0; j < text.length()-i; j++) {
                String s = text.substring(j,j+i).trim();
                if (s==null || s.trim().equals("")){
                    continue;
                }
                Candidate candidate = candidatesMap.get(s);
                if (candidate==null){
                    AtomicInteger atomicInteger = new AtomicInteger(1);
                    candidate = new Candidate(s,atomicInteger);
                    if (j!=0){
                        char left= text.charAt(j-1);
                        String leftStr = Character.toString(left);
                        Map<String,Integer> leftMap = candidate.getLeftMap();
                        Integer f = leftMap.get(leftStr);
                        if (f==null){
                            leftMap.put(leftStr,1);
                        }else {
                            leftMap.put(leftStr,++f);
                        }
                    }
                    if (j<text.length()-i-1){
                        char right= text.charAt(j+i);
                        String rightStr = Character.toString(right);
                        Map<String,Integer> rightMap = candidate.getRightMap();
                        Integer f = rightMap.get(rightStr);
                        if (f==null){
                            rightMap.put(rightStr,1);
                        }else {
                            rightMap.put(rightStr,++f);
                        }
                    }
                    candidatesMap.put(s,candidate);
                }else {
                    candidate.incrementFrequence();
                    if (j!=0){
                        char left= text.charAt(j-1);
                        String leftStr = Character.toString(left);
                        Map<String,Integer> leftMap = candidate.getLeftMap();
                        Integer f = leftMap.get(leftStr);
                        if (f==null){
                            leftMap.put(leftStr,1);
                        }else {
                            leftMap.put(leftStr,++f);
                        }
                    }
                    if (j<text.length()-i-1){
                        char right= text.charAt(j+i);
                        String rightStr = Character.toString(right);
                        Map<String,Integer> rightMap = candidate.getRightMap();
                        Integer f = rightMap.get(rightStr);
                        if (f==null){
                            rightMap.put(rightStr,1);
                        }else {
                            rightMap.put(rightStr,++f);
                        }
                    }


                }

            }
        }


        return candidatesMap;
    }

    public String filterText(String text){
        for (int i = 0; i < specialChar.length(); i++) {
            text = text.replace(specialChar.substring(i,i+1),"");
        }
        return text;
    }


    /**
     *
     * 计算凝固度
     * P(ABC)/(P(A)*P(BC)),P(ABC)/(P(AB)*P(C)) 求最小
     * 应该可以运用动态规划的算法
     * 字符串作为key,概率为value
     *
     * @param candidate 候选词
     * @param frequency 候选词的频次
     * @param textLength 整个文本的长度
     * @param candidatesMap 候选词的频次Map
     * @return
     */
    public double computeAgglomerationDegree(String candidate,Integer frequency,int textLength,Map<String,Candidate> candidatesMap){
        double agglomerationDegree = Double.MAX_VALUE;
        int length = candidate.length();
        BigDecimal textLengthBigDecimal = new BigDecimal(textLength);
        BigDecimal frequencyBigDecimal = new BigDecimal(frequency*length);
        BigDecimal pCandidate = frequencyBigDecimal.divide(textLengthBigDecimal,16,BigDecimal.ROUND_HALF_UP);
        for (int i = 1; i < length ; i++) {
            String a = candidate.substring(i);
            String b = candidate.substring(0, i);
            Candidate candidateA = candidatesMap.get(a);
            Candidate candidateB = candidatesMap.get(b);
            if (candidateA==null || candidateB==null){
                continue;
            }
            BigDecimal totalLengthA = new BigDecimal(a.length()*candidateA.getFrequence().intValue());
            BigDecimal totalLengthB = new BigDecimal(b.length()*candidateB.getFrequence().intValue());
            BigDecimal pA = totalLengthA.divide(textLengthBigDecimal,16,BigDecimal.ROUND_HALF_UP);
            BigDecimal pB = totalLengthB.divide(textLengthBigDecimal,16,BigDecimal.ROUND_HALF_UP);
//            double pA = a.length()*candidateA.getFrequence().intValue()/textLength;
//            double pB = b.length()*candidateB.getFrequence().intValue()/textLength;
            BigDecimal temp = pCandidate.divide(pA.multiply(pB),16,BigDecimal.ROUND_HALF_UP);
            agglomerationDegree = temp.doubleValue() < agglomerationDegree ? temp.doubleValue():agglomerationDegree;
        }

        return agglomerationDegree;
    }


    /**
     * 计算自由度
     * ABC左邻集合
     * ABC右邻集合
     * leftMap key为左邻字符串，value为出现的次数
     * rightMap key为右邻字符串，value为出现的次数
     * 信息熵=-PLog(p)
     * @param candidate
     * @return
     */
    public double computeCombinationDegree(Candidate candidate){
        double entropy = candidate.getEntropy();
        return entropy;
    }



    public String readText(String path,String docName){
        StringBuffer sb = new StringBuffer();
        File file = new File(path+"/"+docName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine())!=null) {
                // 显示行号
                if (!tempString.trim().equals("")){
                    String temp = tempString.replaceAll("[\\(（【-].*", "");
                    if (temp.length()>1){
                        sb.append(temp);
                        line++;
                    }

                }

            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        return sb.toString();
    }



    public List<String> getTokens(String query,String segName){

        String solrServerUrl = "http://localhost:8983/solr/";
        List<String> tokens = new ArrayList<String>();
        if (query==null || query.trim().equals("")){
            return tokens;
        }
        try {

            String queryEncode = URLEncoder.encode(query, "UTF8");

            String url = solrServerUrl+"foodSpu/analysis/field?q="+queryEncode+"&analysis.fieldtype="+segName+"&wt=json";
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            JsonNode node = OBJECT_MAPPER.readTree(EntityUtils.toString(response.getEntity()));
            JsonNode node2 = node.get("analysis").get("field_types").get(segName);
            JsonNode node3 = node2.get("query");
            Iterator<JsonNode> iter = node3.get(5).iterator();
            while (iter.hasNext()){
                String token = iter.next().get("text").getTextValue();
                tokens.add(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tokens;
    }
}
