package com.note4j.nlp.alda.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.note4j.nlp.alda.conf.PathConfig;

/**
 * Calculate the topicSimilarity by the matrix theta A*K(author*topic).
 *
 * @author changwei
 *
 */
public class TopicSimilarity {
    // save the simility between two authors.
    private double[][] smlt;
    // save the average topic probability of two authors.
    private double[][] M;
    private double[][] dist;
    private double[][] Dkl;

    public double[][] getSmlt() {
        return smlt;
    }

    public void setSmlt(double[][] smlt) {
        this.smlt = smlt;
    }

    public void getSimilar(double theta[][], int author, int topic) {
        smlt = new double[author][author];
        M = new double[author][topic];
        Dkl = new double[author][author];
        dist = new double[author][author];
        // Jensen-Shannon 散度
        for (int i = 0; i != author - 1; i++) {
            for (int j = i + 1; j != author; j++) {
                for (int k = 0; k != topic; k++) {
                    // 计算从i到j得平均值
                    M[i][k] = (theta[i][k] + theta[j][k]) / 2;
                    // 计算 从 i到M 的Kullback-Leibler 散度
                    Dkl[i][j] += theta[i][k]
                            * Math.log10(theta[i][k] / M[i][k]);
                    Dkl[j][i] += theta[j][k]
                            * Math.log10(theta[j][k] / M[i][k]);
                }
            }
        }

        for (int i = 0; i != author - 1; i++) {
            for (int j = i + 1; j != author; j++) {
                // calculate the Dissimilarity between user i,j.
                dist[i][j] = Math.sqrt(Dkl[i][j] + Dkl[j][i]);
                // calculate the similarity between user i,j.
                smlt[i][j] = 1 - dist[i][j];
            }
        }
    }

    public void saveSimilarity(double smlt[][], int a) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(
                PathConfig.LdaResultsPath + "result.data"));
        for (int i = 0; i != a - 1; i++) {
            for (int j = i + 1; j != a; j++) {
                if(smlt[i][j]>0.5){
                    writer.write(i + "\t" + j + "\t" + smlt[i][j] + "\n");
//                    writer.write(i + "\t" + j + "\t" + 1 + "\n");
                }
            }
        }
        writer.close();
    }
}
