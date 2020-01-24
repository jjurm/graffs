/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.cam.jm2186.graffs;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes an approximation to pi
 * Usage: JavaSparkPi [partitions]
 */
public final class JavaSparkPi {

    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName("JavaSparkPi")
                //.master("spark://ubuntu:7077")
                //.master("local")
                .getOrCreate();

        if (spark == null) return;

        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        int slices = (args.length == 1) ? Integer.parseInt(args[0]) : 128;
        int n = 1_000 * slices;
        List<Integer> l = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            l.add(i);
        }

        JavaRDD<Integer> dataSet = jsc.parallelize(l, slices);

        int num_trials = 1000;
        int count = dataSet.map(integer -> {
            int sum = 0;
            for (int i = 0; i < num_trials; i++) {
                double x = Math.random() * 2 - 1;
                double y = Math.random() * 2 - 1;
                sum += (x * x + y * y <= 1) ? 1 : 0;
            }
            return sum;
        }).reduce(Integer::sum);

        double piValue = (4.0 * count / n / num_trials);
        System.out.println("Pi is roughly " + piValue + " (" + (Math.abs((1 - (piValue / Math.PI)) * 100)) + "% off)");

        spark.stop();
    }
}
