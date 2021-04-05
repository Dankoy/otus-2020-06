package ru.dankoy.otus.diploma.clusteralgorithms.kmeans;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dankoy.otus.diploma.cluster.Cluster;
import ru.dankoy.otus.diploma.cluster.ClusterImpl;
import ru.dankoy.otus.diploma.core.model.Crash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ezelenin
 * <p>
 * Имплементация kmeans алгоритма.
 * <p>
 * Описание алгоритма:
 * Шаг 1 - первичная инициализация кластеров:
 * 1) Из существующих точек рандомно выбираются центры кластеров.
 * 2) Так же рандомно добавляются точки в кластера
 * <p>
 * Шаг 2 - поиск ближайших точек к центроиду
 * 1) Ищутся ближайшие точки к центру кластера. Рассточние от точки до центра кластера считается по формуле haversine.
 * 2) Центр кластеров пересчитывается
 * <p>
 * Шаг 2 повторяется до тех пор пока кластера из предыдущей итерации не будут отличатся от текущей итерации.
 * <p>
 * На больших данных работает достаточно медленно
 */
public class KMeansImpl {

    private static final Logger logger = LoggerFactory.getLogger(KMeansImpl.class);

    // Радиус земли в метрах
    public static final double EARTH_RADIUS = 6372.8 * 1000;

    /**
     * Главный метод класса.
     *
     * @param crashes a список из аварий {@link Crash}
     * @param k       количество кластеров
     * @return список кластеров
     */
    public List<Cluster> cluster(List<Crash> crashes, int k) throws Exception {

        // TODO: Добавить кастомное исклбчение
        if (k <= 0)
            throw new Exception();

        return cluster(crashes, k, buildRandomInitialClusters(crashes, k));
    }

    /**
     * Uses the KMeans algorithm to generate k clusters from the set of points using a predefined starting set of
     * {@link Cluster}
     *
     * @param crashes  a List of {@link Crash}
     * @param k
     * @param clusters
     * @return
     */
    private List<Cluster> cluster(List<Crash> crashes, int k, List<Cluster> clusters) {

        List<Cluster> oldClusters = new ArrayList<>(k);

        for (int i = 1; i <= k; i++) {
            oldClusters.add(new ClusterImpl());
        }

        logger.info("oldClusters size {}", oldClusters.size());
        logger.info("clusters size {}", clusters.size());

        int i = 0;
        while (!hasConverged(oldClusters, clusters)) {
            logger.info("On iteration {}", i);

            for (int j = 0; j < k; j++) {
                Cluster deepCopy = SerializationUtils.clone(clusters.get(j));
                oldClusters.set(j, deepCopy);
            }
            i++;

            assignPointsToClusters(crashes, clusters);
            adjustClusterCenters(clusters);
        }
        return clusters;
    }


    /**
     * Считает дистанцию между двумя точками на земле. Так как земля - не сфера, то используется формула haversine.
     * http://rosettacode.org/wiki/Haversine_formula#Java
     *
     * @param lat1 широта первой точки
     * @param lon1 долгота первой точки
     * @param lat2 широта второй точки
     * @param lon2 долгота второй точки
     * @return дистанция между двумя точками на земле
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math
                .cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS * c;
    }

    /**
     * Метод обертка над методом {@link KMeansImpl#haversineDistance(double, double, double, double)}
     *
     * @param crash
     * @param centroid
     * @return
     */
    private double haversineDistance(Crash crash, Cluster centroid) {
        return haversineDistance(crash.getLatitude(), crash.getLongitude(), centroid.getLatitude(),
                centroid.getLongitude());
    }

    /**
     * Генерирует центры кластеров в рандомных местах, используя существующие координаты точек {@link Cluster}
     *
     * @param crashes
     * @param k
     * @return
     */
    private List<Cluster> buildRandomInitialClusters(List<Crash> crashes, int k) {

        Collections.shuffle(crashes);
        List<Cluster> clusters = new ArrayList<>();

        for (int i = 0; i < k; i++) {

            clusters.add(new ClusterImpl(crashes.get(i).getLatitude(), crashes.get(i).getLongitude()));

        }

        // Заполняет кластера авариями
        int i = 0;
        for (Crash crash : crashes) {
            clusters.get(i).getPoints().add(crash);
            i++;
            if (i == k) {
                i = 0;
            }
        }
        return clusters;
    }


    /**
     * Распределяет точки к наиболее подходящему кластеру {@link Cluster}, то есть находит ближайшую точку к кластеру
     * по формуле haversine.
     *
     * @param crashes
     * @param clusters
     */
    private void assignPointsToClusters(List<Crash> crashes, List<Cluster> clusters) {
        // for each point, find the cluster with the closest center
        clusters.forEach(Cluster::clearPoints);

        for (Crash crash : crashes) {
            Cluster current = clusters.get(0);
            for (Cluster cluster : clusters) {
                if (haversineDistance(crash, cluster) < haversineDistance(crash, current)) {
                    current = cluster;
                }
            }
            logger.debug("Adding {} to {}", crash, current);
            current.getPoints().add(crash);
        }
    }


    /**
     * Пересчитывает центр кластера для каждого кластера {@link Cluster}
     */
    private void adjustClusterCenters(List<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            if (!cluster.getPoints().isEmpty()) {
                double newLatitude = cluster.getSumLatitude() / cluster.getPoints().size();
                double newLongitude = cluster.getSumLongitude() / cluster.getPoints().size();
                cluster.renewCoordinates(newLatitude, newLongitude);
            }
        }
    }

    /**
     * Определяет, если кластера {@link Cluster} сошлись, то есть сравнивается результат предыдущей итерации
     * кластеризации с текущей. Если списки точек {@link Crash} кластеров {@link Cluster} не отличаются друг от друга,
     * то, считаем, что распределили все точки по кластерам корректно.
     *
     * @param previous список кластеров из предыдущей операции
     * @param current  список кластеров из текущей итерации
     * @return true, если списки сошлись. false, если списки отличаются друг от друга.
     */
    private boolean hasConverged(List<Cluster> previous, List<Cluster> current) {
        logger.info("oldClusters size {}", previous.size());
        logger.info("clusters size {}", current.size());
        if (previous.size() != current.size()) {
            throw new IllegalArgumentException("Clusters must be the same size");

        }
        for (int i = 0; i < previous.size(); i++) {
            if (!previous.get(i).getPoints().equals(current.get(i).getPoints())) {
                return false;
            }
        }
        logger.debug("Converged!");
        return true;
    }


}
