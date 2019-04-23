package org.thomaschen.streamlinedata.model;

import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;

@Service
public class RegressionService {

    public static double[] extractDepVars(List<TaskData> tasks) {
        double[] depVars = new double[tasks.size()];
        int i = 0;
        for (TaskData task : tasks) {
            depVars[i] = task.getPoint()[0];
            i++;
        }
        System.err.println("DEPVARS: " + Arrays.toString(depVars));
        return depVars;
    }

    public static double[][] extractIndVars(List<TaskData> tasks) {
        double[][] indVars = new double[tasks.size()][tasks.get(0).getPoint().length];

        int i = 0;
        for (TaskData task : tasks) {
            double[] tempPt = task.getPoint();
            for (int j = 1; j < tempPt.length; j++) {
                indVars[i][j] = tempPt[j];
            }
            i++;
        }

        System.err.println("INDVARS: " + Arrays.deepToString(indVars));
        return indVars;
    }

    public static double[][] extractSimpleVars(List<TaskData> tasks) {
        double[][] simpleVars = new double[tasks.size()][2];

        int i = 0;
        for (TaskData task : tasks) {
            simpleVars[i][1] = task.getPoint()[0];
            simpleVars[i][0] = task.getPoint()[1];
            i++;
        }

        System.err.println("SVARS: " + Arrays.deepToString(simpleVars));
        return simpleVars;

    }

    public static double predictFromSLSModel(List<TaskData> data, TaskData target) {
        SimpleRegression model = new SimpleRegression();
        model.addData(extractSimpleVars(data));
        return model.predict(target.getExpDuration());
    }


    public static double predictFromOLSModel(OLSMultipleLinearRegression model, double[] x) {
        System.err.println("OLS REGRESSION");
        System.err.println("REGRESSION PARAMETERS: ");

        try {
            double[] beta = model.estimateRegressionParameters();

            for (double b : beta) {
                System.err.println(b);
            }

            double pred = beta[0];
            for (int i = 1; i < beta.length; i++) {
                pred += beta[i] * x[i - 1];
            }

            System.err.println("PREDICTION: " + pred);
            return pred;

        } catch (SingularMatrixException sme) {
            System.err.println("PREDICTION: Failure");
            return -1;
        }
    }

    public static void ensembleOLSPrediction(List<CentroidCluster<TaskData>> clusters, TaskData target) {

        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();

        int i = 0;
        for (CentroidCluster<TaskData> cluster : clusters) {
            double[] center = cluster.getCenter().getPoint();
            double[] depVars = extractDepVars(cluster.getPoints());
            double[][] indVars = extractIndVars(cluster.getPoints());
            model.newSampleData(depVars, indVars);
            double clusterPred = predictFromOLSModel(model, target.getPoint());
            System.err.println("========================================================================================");
            System.err.println("CLUSTER " + i + " REGRESSION");
            System.err.println("CENTROID: " + Arrays.toString(center));
            System.err.println("PREDICTION: " + clusterPred);
            i++;
        }
    }

    public static double ensembleSLSPrediction(List<CentroidCluster<TaskData>> clusters, TaskData target) {
        double[] weights = new double[clusters.size()];
        double[] predictions = new double[clusters.size()];
        int i = 0;
        for (CentroidCluster<TaskData> cluster : clusters) {
            System.err.println("============================================");
            System.err.println("CLUSTER " + i + " REGRESSION");
            double pred = predictFromSLSModel(cluster.getPoints(), target);
            System.err.println("CENTROID: " + Arrays.toString(cluster.getCenter().getPoint()));
            System.err.println("PREDICTION: " + pred);

            target.setActualDuration((long) pred);
            double weight = target.compute(target.getPoint(), cluster.getCenter().getPoint());
            System.err.println("PREDICTION WEIGHT: " + weight);
            weights[i] = weight;
            predictions[i] = pred;

            i++;
        }

        System.err.println("========================================================================================");
        System.err.println("3. ENSEMBLE PHASE");
        System.err.println("========================================================================================");
        System.err.println("============================================");

        double[] nweights = normalizeWeights(weights.clone());
        double[] sweights = softmax(nweights.clone());

        System.err.println("NORM-WEIGHTS: " + Arrays.toString(nweights));
        System.err.println("SOFTMAX-WEIGHTS: " + Arrays.toString(sweights));
        System.err.println("PREDICTIONS: " + Arrays.toString(predictions));

        double sEnsemblePred = calcEnsemblePred(sweights, predictions);

        System.err.println("ENSEMBLE PRED: " + sEnsemblePred);
        System.err.println("============================================");
        return sEnsemblePred;
    }

    private static double calcEnsemblePred(double[] weights, double[] predictions) {
        double ensemblePred = 0.0;
        for (int j = 0; j < weights.length; j++) {
            ensemblePred += weights[j] * predictions[j];
        }
        ensemblePred /= weights.length;
        return ensemblePred;
    }

    private static double[] normalizeWeights(double[] weights) {
        DoubleSummaryStatistics stats = Arrays.stream(weights).summaryStatistics();
        double min = stats.getMin();
        double max = stats.getMax();

        for(int i = 0; i < weights.length; i++) {
            double z = (weights[i] - min)/(max - min);
            weights[i] = z;
        }

        return weights;
    }

    private static double[] softmax(double[] weights) {
        double sum = 0;
        for (int i = 0; i < weights.length; i++) {
            weights[i] = Math.exp(weights[i]);
            sum += weights[i];
        }

        for (int i = 0; i < weights.length; i++) {
            weights[i] /= sum;
        }

        return  weights;
    }
}
