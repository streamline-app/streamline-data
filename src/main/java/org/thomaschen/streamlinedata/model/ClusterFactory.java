package org.thomaschen.streamlinedata.model;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thomaschen.streamlinedata.repository.TaskDataRepository;

import java.util.List;

@Service
public class ClusterFactory {

    public static int calcK(long numTasks) {
        // TODO: Replace ROH with Bayesian Information Criterion or Gaussian Check

        double kRoh = Math.pow((numTasks/2.0), 0.5);
        int k = (int) Math.round(kRoh);
        return k;
    }

    public static List<CentroidCluster<TaskData>> cluster(List<TaskData> points) {
        KMeansPlusPlusClusterer kClusterer = new KMeansPlusPlusClusterer(calcK(points.size()));

        List<CentroidCluster<TaskData>> clusters = null;
        try {
            clusters = kClusterer.cluster(points);
        } catch (ConvergenceException ce) {
            System.err.println("Empty Cluster Encountered with Empty Strategy: ERROR");

        } catch (MathIllegalArgumentException mae) {
            System.err.println("Null Data Points or nClusters > n");
        }

        return clusters;
    }

}
