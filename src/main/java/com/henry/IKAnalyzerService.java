package com.henry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhangtao13
 * Date: 15/11/17
 * Time: 下午1:14
 */
public class IKAnalyzerService {

    public List<String> analyzer(String text){

//        Dictionary dictionary = new Dictionary();
        List<String> resultList = new ArrayList<String>();
        try {
            Analyzer analyzer = new IKAnalyzer(true);

            TokenStream tokenStream = analyzer.tokenStream("", new StringReader(text));
            CharTermAttribute term= tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while(tokenStream.incrementToken()){
//                System.out.print(term.toString() + "/");
                resultList.add(term.toString());
            }
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultList;
    }

    public static void main(String[] args) {

        IKAnalyzerService service = new IKAnalyzerService();
        List<String> tokens = service.analyzer("将太无二");
        System.out.println(tokens);
    }
}
