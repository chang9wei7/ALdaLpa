package com.note4j.nlp.alda.main;

/**Class for Lda model
 * @author yangliu
 * @blog http://blog.csdn.net/yangliuy
 * @mail yangliuyx@gmail.com
 */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.note4j.nlp.alda.conf.PathConfig;
import com.note4j.nlp.alda.util.FileUtil;

public class ALdaModel {

    // author index array
    int[][] author;
    // vocabulary size, author number,topic number
    int V, A, K;
    // topic label array
    int[][] z;
    // doc-topic dirichlet prior parameter 狄雷克斯先验条件α
    float alpha;
    // topic-word dirichlet prior parameter 狄雷克斯先验条件β
    float beta;
    // given author a, count times of topic k. A*K
    int[][] nak;
    // given topic k, count times of term t. K*V
    int[][] nkt;
    // Sum for each row in nak
    int[] nakSum;
    // Sum for each row in nkt
    int[] nktSum;
    // Parameters for topic-word distribution K*V
    double[][] phi;
    //  Parameters for author-topic distribution A*K
    double[][] theta;
    // Times of iterations
    int iterations;
    // The number of iterations between two saving
    int saveStep;
    // Begin save model at this iteration
    int beginSaveIters;

    public ALdaModel(ALdaGibbsSampling.modelparameters modelparam) {
        // TODO Auto-generated constructor stub
        alpha = modelparam.alpha;
        beta = modelparam.beta;
        iterations = modelparam.iteration;
        K = modelparam.topicNum;
        saveStep = modelparam.saveStep;
        beginSaveIters = modelparam.beginSaveIters;
    }

    /**
     * 初始化模型
     *
     * @param docSet
     */
    public void initializeModel(Authors authorSet) {
        // TODO Auto-generated method stub
        //  作者的总数
        A = authorSet.aus.size();
        // 单词的总数
        V = authorSet.termToIndexMap.size();
        // 作者*主题矩阵
        nak = new int[A][K];
        // 主题*单词矩阵
        nkt = new int[K][V];

        nakSum = new int[A];
        nktSum = new int[K];
        // 结果Φ的值
        phi = new double[K][V];
        // 结果θ的值
        theta = new double[A][K];

        // initialize author index array,将处理好的数据转存到二维数组中
        author = new int[A][];
        for (int a = 0; a < A; a++) {
            // Notice the limit of memory
            // 单词个数
            int N = authorSet.aus.get(a).authorWords.length;
            author[a] = new int[N];
            // 将单词转储到，[作者][单词]的二维数组里
            for (int n = 0; n < N; n++) {
                author[a][n] = authorSet.aus.get(a).authorWords[n];
            }
        }
        // initialize topic lable z for each word，初始化每个单词的主题标签
        z = new int[A][];
        for (int a = 0; a < A; a++) {
            int N = authorSet.aus.get(a).authorWords.length;
            z[a] = new int[N];
            for (int n = 0; n < N; n++) {
                // 随机给每个单词生成主题，根据预定的主题个数取整
                int initTopic = (int) (Math.random() * K);// From 0 to K - 1
                z[a][n] = initTopic;
                // number of words in doc m assigned to topic initTopic add
                // 1，作者*主题矩阵
                nak[a][initTopic]++;
                // number of terms doc[m][n] assigned to topic initTopic add
                // 1，主题*单词矩阵
                nkt[initTopic][author[a][n]]++;
                // total number of words assigned to topic initTopic add
                // 1，各主题对应的单词个数
                nktSum[initTopic]++;
            }
            // total number of words with author a is N，每篇文章对应的单词个数
            nakSum[a] = N;
        }
    }

    public void inferenceModel(Authors authorSet) throws IOException {
        // TODO Auto-generated method stub2259767478
        if (iterations < saveStep + beginSaveIters) {
            System.err
                    .println("Error: the number of iterations should be larger than "
                            + (saveStep + beginSaveIters));
            System.exit(0);
        }
        for (int i = 0; i < iterations; i++) {
            System.out.println("Iteration " + i);
            if ((i >= beginSaveIters)
                    && (((i - beginSaveIters) % saveStep) == 0)) {
                // Saving the model
                System.out.println("Saving model at iteration " + i + " ... ");
                // Firstly update parameters
                updateEstimatedParameters();
                // Secondly print model variables
                saveIteratedModel(i, authorSet);
            }

            // Use Gibbs Sampling to update z[][]
            for (int a = 0; a < A; a++) {
                int N = authorSet.aus.get(a).authorWords.length;
                for (int n = 0; n < N; n++) {
                    // Sample from p(z_i|z_-i, w)
                    int newTopic = sampleTopicZ(a, n);
                    z[a][n] = newTopic;
                }
            }
        }
    }

    /**
     * 计算phi和theta的值
     */
    private void updateEstimatedParameters() {
        // TODO Auto-generated method stub
        for (int k = 0; k < K; k++) {
            for (int t = 0; t < V; t++) {
                phi[k][t] = (nkt[k][t] + beta) / (nktSum[k] + V * beta);
            }
        }

        for (int a = 0; a < A; a++) {
            for (int k = 0; k < K; k++) {
                theta[a][k] = (nak[a][k] + alpha) / (nakSum[a] + K * alpha);
            }
        }
    }

    private int sampleTopicZ(int a, int n) {
        // TODO Auto-generated method stub
        // Sample from p(z_i|z_-i, w) using Gibbs upde rule

        // Remove topic label for w_{m,n}
        int oldTopic = z[a][n];
        nak[a][oldTopic]--;
        nkt[oldTopic][author[a][n]]--;
        nakSum[a]--;
        nktSum[oldTopic]--;

        // Compute p(z_i = k|z_-i, w)
        double[] p = new double[K];
        for (int k = 0; k < K; k++) {
            p[k] = (nkt[k][author[a][n]] + beta) / (nktSum[k] + V * beta)
                    * (nak[a][k] + alpha) / (nakSum[a] + K * alpha);
        }

        // Sample a new topic label for w_{m, n} like roulette
        // Compute cumulated probability for p
        for (int k = 1; k < K; k++) {
            p[k] += p[k - 1];
        }
        double u = Math.random() * p[K - 1]; // p[] is unnormalised
        int newTopic;
        // 选择一个权值最大的当做本次迭代的主题，保存到newTopic
        for (newTopic = 0; newTopic < K; newTopic++) {
            if (u < p[newTopic]) {
                break;
            }
        }

        // Add new topic label for w_{m, n}
        nak[a][newTopic]++;
        nkt[newTopic][author[a][n]]++;
        nakSum[a]++;
        nktSum[newTopic]++;
        return newTopic;
    }

    /**
     * 保存实验参数，theta,phi等实验结果
     *
     * @param iters
     * @param docSet
     * @throws IOException
     */
    public void saveIteratedModel(int iters, Authors docSet) throws IOException {
        // TODO Auto-generated method stub
        // lda.params lda.phi lda.theta lda.tassign lda.twords
        // lda.params
        String resPath = PathConfig.LdaResultsPath;
        String modelName = "lda_" + iters;
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("alpha = " + alpha);
        lines.add("beta = " + beta);
        lines.add("topicNum = " + K);
        lines.add("authorNum = " + A);
        lines.add("termNum = " + V);
        lines.add("iterations = " + iterations);
        lines.add("saveStep = " + saveStep);
        lines.add("beginSaveIters = " + beginSaveIters);
        FileUtil.writeLines(resPath + modelName + ".params", lines);

        // lda.phi K*V
        BufferedWriter writer = new BufferedWriter(new FileWriter(resPath
                + modelName + ".phi"));
        for (int i = 0; i < K; i++) {
            for (int j = 0; j < V; j++) {
                writer.write(phi[i][j] + "\t");
            }
            writer.write("\n");
        }
        writer.close();

        // atlda.theta
        writer = new BufferedWriter(new FileWriter(resPath + modelName
                + ".theta"));
        for (int i = 0; i < A; i++) {
            for (int j = 0; j < K; j++) {
                writer.write(theta[i][j] + "\t");
            }
            writer.write("\n");
        }
        writer.close();

        // lda.tassign
        writer = new BufferedWriter(new FileWriter(resPath + modelName
                + ".tassign"));
        for (int a = 0; a < A; a++) {
            for (int n = 0; n < author[a].length; n++) {
                writer.write(author[a][n] + ":" + z[a][n] + "\t");
            }
            writer.write("\n");
        }
        writer.close();

        // lda.twords phi[][] K*V
        writer = new BufferedWriter(new FileWriter(resPath + modelName
                + ".twords"));
        int topNum = 20; // Find the top 20 topic words in each topic
        for (int i = 0; i < K; i++) {
            List<Integer> tWordsIndexArray = new ArrayList<Integer>();
            for (int j = 0; j < V; j++) {
                tWordsIndexArray.add(new Integer(j));
            }
            Collections.sort(tWordsIndexArray, new ALdaModel.TwordsComparable(
                    phi[i]));
            writer.write("topic " + i + "\t:\t");
            for (int t = 0; t < topNum; t++) {
                writer.write(docSet.indexToTermMap.get(tWordsIndexArray.get(t))
                        + " " + phi[i][tWordsIndexArray.get(t)] + "\t");
            }
            writer.write("\n");
        }
        writer.close();
    }

    public class TwordsComparable implements Comparator<Integer> {

        public double[] sortProb; // Store probability of each word in topic k

        public TwordsComparable(double[] sortProb) {
            this.sortProb = sortProb;
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            // TODO Auto-generated method stub
            // Sort topic word index according to the probability of each word
            // in topic k
            if (sortProb[o1] > sortProb[o2])
                return -1;
            else if (sortProb[o1] < sortProb[o2])
                return 1;
            else
                return 0;
        }
    }

    public void getSimilarity() throws IOException {
        TopicSimilarity ts = new TopicSimilarity();
        ts.getSimilar(theta, A, K);
        ts.saveSimilarity(ts.getSmlt(), A);
    }
}
