package com.note4j.nlp.alda.modularity;

import com.note4j.nlp.alda.conf.PathConfig;
import com.note4j.nlp.alda.util.FileUtil;
import com.note4j.nlp.alda.util.MathUtil;

import java.util.*;

/**
 * 根据已知边和社区进行模块度Q的计算
 * Created by changwei on 15/4/16.
 */

public class Modularity {
    /**
     * 读取文件中所有的社区
     *
     * @param file
     * @return
     */
    public Map<Integer, Set<Integer>> readCommunity(String file) {
        Map<Integer, Set<Integer>> comMap = new HashMap<Integer, Set<Integer>>();
        ArrayList<String> lineList = new ArrayList<String>();
        Set<Integer> nodeSet;
        FileUtil.readLines(file, lineList);
        int count = 0;
        for (String tempStr : lineList) {
            count++;
            String[] nodeArr = tempStr.split(" ");
            nodeSet = new HashSet<>();
            for (int i = 1; i != nodeArr.length; i++) {
                nodeSet.add(Integer.parseInt(nodeArr[i]));
                System.out.print(nodeArr[i] + ",");
            }
            comMap.put(count, nodeSet);
            System.out.println();
        }

//        for(Map.Entry<Integer,Set<Integer>> entry:comMap.entrySet()){
//           System.out.println(entry.getKey());
//            for(int i:entry.getValue()){
//                System.out.print(i+",");
//            }
//            System.out.println();
//        }
        return comMap;
    }

    /**
     * 读取所有的边
     *
     * @param file
     * @return
     */
    public Map<Integer, List<Node>> readEdge(String file) {
        Map<Integer, List<Node>> edgeMap = new HashMap<Integer, List<Node>>();
        ArrayList<String> lineList = new ArrayList<String>();
        List<Node> nodeList;
        FileUtil.readLines(file, lineList);
        int count = 0;
        for (String tempStr : lineList) {
            count++;
            String[] nodeArr = tempStr.split("\\s");
            nodeList = new ArrayList<Node>();
            for (int i = 0; i != nodeArr.length - 1; i++) {
                Node node = new Node(Integer.parseInt(nodeArr[i]));
                nodeList.add(node);
//                System.out.print(nodeArr[i] + ",");
            }
            edgeMap.put(count, nodeList);
            System.out.println();
        }
        return edgeMap;
    }

    /**
     * 获取模块间的相似度矩阵
     *
     * @param comMap
     * @param edgeMap
     * @return
     */
    public double[][] getComMatrix(Map<Integer, Set<Integer>> comMap, Map<Integer, List<Node>> edgeMap) {

        if (null == comMap || null == edgeMap || comMap.size() == 0 || edgeMap.size() == 0) {
            return null;
        }
        int matrixSize = comMap.size();
        double[][] comMatrix = new double[matrixSize][matrixSize];
        double sum = 0;
        /**
         * 对边进行社区归类
         */
        for (Map.Entry<Integer, List<Node>> edgeEntry : edgeMap.entrySet()) {
            List<Node> nodeList = edgeEntry.getValue();
            for (Node nodeTemp : nodeList) {
                for (Map.Entry<Integer, Set<Integer>> comEntry : comMap.entrySet()) {
                    Set<Integer> tempCom = comEntry.getValue();
                    if (tempCom.contains(nodeTemp.getValue())) {
                        nodeTemp.setCommunity(comEntry.getKey());
                    }
                }
            }
        }
        /**
         * debug 边顶点所在的社区
         */
//        for (Map.Entry<Integer, List<Node>> edgeEntry : edgeMap.entrySet()) {
//            List<Node> nodeList = edgeEntry.getValue();
//            for (Node nodeTemp : nodeList) {
//                System.out.print("value: "+nodeTemp.getValue()+"  "+"com: "+nodeTemp.getCommunity()+" ");
//            }
//            System.out.println();
//        }
        for (Map.Entry<Integer, List<Node>> edgeEntry : edgeMap.entrySet()) {
            List<Node> nodeList = edgeEntry.getValue();
            Node bNode = nodeList.get(0);
            Node eNode = nodeList.get(1);
            comMatrix[bNode.getCommunity() - 1][eNode.getCommunity() - 1]++;
        }

        for (int i = 0; i != comMatrix.length; i++) {
            for (int j = 0; j != comMatrix[i].length; j++) {
                sum += comMatrix[i][j];
            }
        }
        for (int i = 0; i != comMatrix.length; i++) {
            for (int j = i; j != comMatrix[i].length; j++) {
                double temp = (comMatrix[i][j] + comMatrix[j][i]) / sum / 2;
                comMatrix[i][j] = comMatrix[j][i] = temp;
            }
        }

        return comMatrix;
    }

    /**
     * 计算最终的模块度
     *
     * @param matrix
     * @return
     */
    public double calModulelarity(double[][] matrix) {
        double tre = 0;
        double[][] squareMatrix = MathUtil.mMatrix(matrix, matrix);
        double squareSum = 0;
        double modularity = -1;

        for (int i = 0; i != matrix.length; i++) {
            tre += matrix[i][i];
        }

        for (int i = 0; i != squareMatrix.length; i++) {
            for (int j = 0; j != squareMatrix[i].length; j++) {
                squareSum += squareMatrix[i][j];
            }
        }
        modularity = tre - squareSum;
        return modularity;

    }

    public double getModularity(String communityFile, String edgeFile) {
        Map<Integer, Set<Integer>> communityMap = readCommunity(PathConfig.LdaResultsPath + "com.data");
        Map<Integer, List<Node>> edgeMap = readEdge(PathConfig.LdaResultsPath + "result.data");
        double[][] communityMatrix = getComMatrix(communityMap, edgeMap);
        double modularity = calModulelarity(communityMatrix);
        return modularity;
    }

    public static void main(String[] args) {
        String communityPath = PathConfig.LdaResultsPath + "com.data";
        String edgePath = PathConfig.LdaResultsPath + "result.data";
        System.out.println(new Modularity().getModularity(communityPath, edgePath));
    }
}
