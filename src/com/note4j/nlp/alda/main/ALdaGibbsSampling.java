package com.note4j.nlp.alda.main;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.note4j.nlp.alda.conf.ConstantConfig;
import com.note4j.nlp.alda.conf.PathConfig;
import com.note4j.nlp.alda.util.FileUtil;

/**
 * Created by changwei on 15/4/16.
 */
public class ALdaGibbsSampling {
    public static class modelparameters {
        float alpha = 0.5f; // usual value is 50 / K
        float beta = 0.1f;// usual value is 0.1
        int topicNum = 100;
        int iteration = 100;
        int saveStep = 10;
        int beginSaveIters = 50;
    }

    /**
     * Get parameters from configuring file. If the configuring file has value
     * in it, use the value. Else the default value in program will be used
     *
     * @param ldaparameters
     * @param parameterFile
     * @return void
     */
    private static void getParametersFromFile(modelparameters ldaparameters,
                                              String parameterFile) {
        // TODO Auto-generated method stub
        ArrayList<String> paramLines = new ArrayList<String>();
        /**
         * 关于配置读取的for循环中的代码可以用Property类代替，在文件中以键值对的形式给出
         */
        FileUtil.readLines(parameterFile, paramLines);

        for (String line : paramLines) {
            String[] lineParts = line.split("=");
            switch (parameters.valueOf(lineParts[0])) {
                case alpha:
                    ldaparameters.alpha = Float.valueOf(lineParts[1]);
                    break;
                case beta:
                    ldaparameters.beta = Float.valueOf(lineParts[1]);
                    break;
                case topicNum:
                    ldaparameters.topicNum = Integer.valueOf(lineParts[1]);
                    break;
                case iteration:
                    ldaparameters.iteration = Integer.valueOf(lineParts[1]);
                    break;
                case saveStep:
                    ldaparameters.saveStep = Integer.valueOf(lineParts[1]);
                    break;
                case beginSaveIters:
                    ldaparameters.beginSaveIters = Integer.valueOf(lineParts[1]);
                    break;
            }
        }
    }

    public enum parameters {
        alpha, beta, topicNum, iteration, saveStep, beginSaveIters;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        String originalDocsPath = PathConfig.ldaDocsPath;
        String resultPath = PathConfig.LdaResultsPath;
        String parameterFile = ConstantConfig.LDAPARAMETERFILE;

        modelparameters ldaparameters = new modelparameters();
        getParametersFromFile(ldaparameters, parameterFile);
        Authors authorSet = new Authors();
        authorSet.readAuthors(originalDocsPath);
        System.out.println("author size " + authorSet.aus.size());
        FileUtil.mkdir(new File(resultPath));
        ALdaModel model = new ALdaModel(ldaparameters);
        System.err.println("1 Initialize the model ...");
        model.initializeModel(authorSet);
        System.err.println("2 Learning and Saving the model ...");
        model.inferenceModel(authorSet);
        System.err.println("3 Learning and Saving the similarity ...");
        model.getSimilarity();
        System.err.println("3 Output the final model ...");
        model.saveIteratedModel(ldaparameters.iteration, authorSet);
        System.err.println("Done!");
    }
}
