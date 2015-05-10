package com.note4j.nlp.alda.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.note4j.nlp.alda.conf.PathConfig;
import com.note4j.nlp.alda.util.FileUtil;
import com.note4j.nlp.alda.util.Stopwords;
/**
 * Created by changwei on 15/4/16.
 */
public class Authors {
    // 保存文档
    // ArrayList<Document> docs;
    // 保存作者信息
    ArrayList<Author> aus;
    // 保存单词与序号的对应关系，全局使用
    Map<String, Integer> termToIndexMap;
    // 保存所有文档中所有单词的集合，全局使用
    ArrayList<String> indexToTermMap;
    // 保存单词与对应数量的关系，全局使用
    Map<String, Integer> termCountMap;

    public Authors() {
        // docs = new ArrayList<Document>();
        aus = new ArrayList<Author>();
        termToIndexMap = new HashMap<String, Integer>();
        indexToTermMap = new ArrayList<String>();
        termCountMap = new HashMap<String, Integer>();
    }

    public void readAuthors(String docsPath) {
        ArrayList<String> tempAuthor = new ArrayList<String>();
        // 读取作者信息
        FileUtil.readLines(PathConfig.ldaAuthorPath, tempAuthor);
        // 保存作者与对应文章的键值对
        Map<Integer, List<Integer>> authorMap = new HashMap<Integer, List<Integer>>();
        formatAuthorInfo(authorMap, tempAuthor);
        Map<Integer, String> fileMap = new HashMap<Integer, String>();
        for (File docFile : new File(docsPath).listFiles()) {
            String fileName = docFile.getName();
            String fileIndex = fileName.substring(0, fileName.indexOf("."));
            System.out.println(fileIndex);
            fileMap.put(Integer.valueOf(fileIndex), docFile.getAbsolutePath());
        }

        for (Map.Entry<Integer, List<Integer>> entry : authorMap.entrySet()) {
            List<Integer> fileList = entry.getValue();
            Author author = new Author(fileList, fileMap, termToIndexMap,
                    indexToTermMap, termCountMap);
            aus.add(author);
        }
    }

    /**
     * 将author和对应文章的原始信息格式化为键值对形式保存
     *
     * @param authorMap
     * @param rawAuthor
     */
    public void formatAuthorInfo(Map<Integer, List<Integer>> authorMap,
                                 ArrayList<String> rawAuthor) {
        //
        List<Integer> tempDoc;
        for (int i = 0; i != rawAuthor.size(); i++) {
            String auLine = rawAuthor.get(i);
            String[] au2doc = auLine.split(",");
            tempDoc = new ArrayList<Integer>();
            for (int j = 2; j < au2doc.length; j++) {
                tempDoc.add(Integer.parseInt(au2doc[j]));
            }
            authorMap.put(i, tempDoc);
        }

    }

    public static class Author {
        private String authorName;
        private String docName;
        // docWords是文档所有单词的集合
        // int[] docWords;
        // authorWords是作者所有单词的集合
        int[] authorWords;

        public Author() {
        }

        /**
         *
         * @param docList
         * @param fileMap
         * @param termToIndexMap 单词序号
         * @param indexToTermMap 单词集
         * @param termCountMap 单词数量
         */
        public Author(List<Integer> docList, Map<Integer, String> fileMap,
                      Map<String, Integer> termToIndexMap,
                      ArrayList<String> indexToTermMap,
                      Map<String, Integer> termCountMap) {

            ArrayList<String> docLines = new ArrayList<String>();
            ArrayList<String> words = new ArrayList<String>();
            // 将作者的所有文档进行格式化
            for (Integer index : docList) {
                this.docName = fileMap.get(index);
                // Read file and initialize word index array
                FileUtil.readLines(docName, docLines);
                for (String line : docLines) {
                    FileUtil.tokenizeAndLowerCase(line, words);
                }
                // Remove stop words and noise words
                for (int i = 0; i < words.size(); i++) {
                    if (Stopwords.isStopword(words.get(i))
                            || isNoiseWord(words.get(i))) {
                        words.remove(i);
                        i--;
                    }
                }
            }
            // Transfer word to index
            // this.docWords = new int[words.size()];
            this.authorWords = new int[words.size()];
            for (int i = 0; i < words.size(); i++) {
                String word = words.get(i);
                if (!termToIndexMap.containsKey(word)) {
                    // 利用词袋容量来保存单词的编号
                    int newIndex = termToIndexMap.size();
                    // termToIndexMap用来vocabulary及对应index
                    termToIndexMap.put(word, newIndex);
                    indexToTermMap.add(word);
                    termCountMap.put(word, new Integer(1));
                    authorWords[i] = newIndex;
                } else {
                    authorWords[i] = termToIndexMap.get(word);
                    termCountMap.put(word, termCountMap.get(word) + 1);
                }
            }
            words.clear();
        }

        // 后续增强过滤规则，目前单词集中还有很多noise word
        public boolean isNoiseWord(String string) {
            // TODO Auto-generated method stub
            string = string.toLowerCase().trim();
            Pattern MY_PATTERN = Pattern.compile(".*[a-zA-Z]+.*");
            Matcher m = MY_PATTERN.matcher(string);
            // filter @xxx and URL
            if (string.matches(".*www\\..*") || string.matches(".*\\.com.*")
                    || string.matches(".*http:.*"))
                return true;
            if (!m.matches()) {
                return true;
            } else
                return false;
        }

    }

    public static void main(String[] args) {
        String strTest = "www.321a32a13";
        System.out.println(new Authors.Author().isNoiseWord(strTest));
        Integer i = 001;
        System.out.println(i);
    }
}
