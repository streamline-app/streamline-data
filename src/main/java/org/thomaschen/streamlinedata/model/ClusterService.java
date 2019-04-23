package org.thomaschen.streamlinedata.model;

import javafx.concurrent.Task;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thomaschen.streamlinedata.repository.TaskDataRepository;

import java.util.Arrays;
import java.util.List;

@Service
public class ClusterService {

    public static int calcK(long entityCount) {
        // TODO: Replace ROH with Bayesian Information Criterion or Gaussian Check
        double kRoh = Math.pow((entityCount/2.0), 0.5);
        int k = (int) Math.round(kRoh);
        return k;
    }

    public static double clusterPredict(List<TaskData> points, TaskData target, UserData owner) {
        int k = calcK( points.size() );
        System.err.println("K FACTOR: " + k);

        List<CentroidCluster<TaskData>> currentClusters = exeKmeansCluster(points, k);
        printClusterResults(currentClusters);

        double minDistance = Double.MAX_VALUE;
        CentroidCluster<TaskData> targetCluster = null;
        for (CentroidCluster<TaskData> cc : currentClusters) {
            double tempDist = target.compute(target.getPoint(), cc.getCenter().getPoint());

            if (tempDist < minDistance) {
                minDistance = tempDist;
                targetCluster = cc;
            }
        }

        System.err.println("========================================================================================");
        System.err.println("TARGET DATA POINT: " + Arrays.toString(target.getPoint()));
        System.err.println("========================================================================================");
        System.err.println("SELECTED CLUSTER: ");
        System.err.println("CENTROID: " + Arrays.toString(targetCluster.getCenter().getPoint()));
        for (TaskData td : targetCluster.getPoints()) {
            System.err.println(Arrays.toString(td.getPoint()));
        }

        return targetCluster.getCenter().getPoint()[0];

    }

    public static List<CentroidCluster<TaskData>> exeKmeansCluster(List<TaskData> points, int k) {

        List<CentroidCluster<TaskData>> clusters = null;

        KMeansPlusPlusClusterer kClusterer = new KMeansPlusPlusClusterer(k);

        try {
            clusters = kClusterer.cluster(points);
        } catch (ConvergenceException ce) {
            System.err.println("Empty Cluster Encountered with Empty Strategy: ERROR");

        } catch (MathIllegalArgumentException mae) {
            System.err.println("Null Data Points or nClusters > n");
        }

        return clusters;
    }


    public static List<TaskData> findCluster(TaskData target, List<CentroidCluster<TaskData>> clusters) {
        for (CentroidCluster<TaskData> cc : clusters) {
            List<TaskData> cluster = cc.getPoints();
            if (cluster.contains(target)) {
                return cluster;
            }
        }

        return null;
    }

    public static double findCentroidEstimate(TaskData target, List<CentroidCluster<TaskData>> clusters) {
        for (CentroidCluster<TaskData> cc : clusters) {
            List<TaskData> cluster = cc.getPoints();
            if (cluster.contains(target)) {
                return cc.getCenter().getPoint()[0];
            }
        }

        return 0.0;
    }

    public static void printClusterResults(List<CentroidCluster<TaskData>> clusters) {
        System.err.println("CLUSTER RESULTS:");
        int i = 0;
        for (CentroidCluster<TaskData> cc : clusters) {
            System.err.println("============================================");
            System.err.println("CLUSTER " + i);
            System.err.println("============================================");
            System.err.println("CENTROID: " + cc.getCenter().toString());

            for (TaskData td : cc.getPoints()) {
                if (td.getTaskId() == null) {
                    System.err.print("TARGET TASK HER: ");
                    System.err.println(Arrays.toString(td.getPoint()));
                } else
                    System.err.println(Arrays.toString(td.getPoint()));
            }

            i++;
        }
    }

}
