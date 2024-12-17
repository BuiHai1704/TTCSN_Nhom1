/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tsp;

import java.util.*;

public class TravelingSalesmanProblem {
    // Lớp để lưu trữ ma trận khoảng cách
    public static class DistanceMatrix {
        private int[][] matrix;
        private int size;

        public DistanceMatrix(int matrix[][],int size) {
            this.size = size;
            this.matrix = matrix;
        }

        

        public int getDistance(int from, int to) {
            return matrix[from][to];
        }

        public int getSize() {
            return size;
        }

        // In ma trận khoảng cách
        public void printMatrix() {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    System.out.printf("%4d ", matrix[i][j]);
                }
                System.out.println();
            }
        }
    }

    // Thuật toán tham lam
    public static class GreedyTSP {
        public static Result solve(DistanceMatrix distanceMatrix) {
            int size = distanceMatrix.getSize();
            boolean[] visited = new boolean[size];
            int[] route = new int[size + 1];
            int totalDistance = 0;

            // Bắt đầu từ thành phố 0
            visited[0] = true;
            route[0] = 0;
            int currentCity = 0;

            for (int i = 1; i < size; i++) {
                int nextCity = findNearestUnvisitedCity(currentCity, visited, distanceMatrix);
                visited[nextCity] = true;
                route[i] = nextCity;
                totalDistance += distanceMatrix.getDistance(currentCity, nextCity);
                currentCity = nextCity;
            }

            // Quay lại thành phố ban đầu
            route[size] = 0;
            totalDistance += distanceMatrix.getDistance(currentCity, 0);

            return new Result(route, totalDistance);
        }

        private static int findNearestUnvisitedCity(int currentCity, boolean[] visited, DistanceMatrix distanceMatrix) {
            int nearestCity = -1;
            int minDistance = Integer.MAX_VALUE;

            for (int city = 0; city < visited.length; city++) {
                if (!visited[city]) {
                    int distance = distanceMatrix.getDistance(currentCity, city);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestCity = city;
                    }
                }
            }

            return nearestCity;
        }
    }

    // Thuật toán đàn kiến
    public static class AntColonyOptimization {
        private DistanceMatrix distanceMatrix;
        private int numAnts;
        private int numIterations;
        private double[][] pheromones;
        private double alpha;  // Tầm quan trọng của pheromone
        private double beta;   // Tầm quan trọng của khoảng cách
        private double evaporationRate;

        public AntColonyOptimization(DistanceMatrix distanceMatrix, int numAnts, int numIterations) {
            this.distanceMatrix = distanceMatrix;
            this.numAnts = numAnts;
            this.numIterations = numIterations;
            this.alpha = 1.0;
            this.beta = 2.0;
            this.evaporationRate = 0.5;

            // Khởi tạo ma trận pheromone
            this.pheromones = new double[distanceMatrix.getSize()][distanceMatrix.getSize()];
            for (double[] row : pheromones) {
                Arrays.fill(row, 1.0);
            }
        }

        public Result solve() {
            Result bestResult = null;

            for (int iteration = 0; iteration < numIterations; iteration++) {
                List<Result> antSolutions = constructAntSolutions();
                updatePheromones(antSolutions);

                // Cập nhật lời giải tốt nhất
                for (Result solution : antSolutions) {
                    if (bestResult == null || solution.getTotalDistance() < bestResult.getTotalDistance()) {
                        bestResult = solution;
                    }
                }
            }

            return bestResult;
        }

        private List<Result> constructAntSolutions() {
            List<Result> solutions = new ArrayList<>();

            for (int ant = 0; ant < numAnts; ant++) {
                solutions.add(constructSingleAntSolution());
            }

            return solutions;
        }

        private Result constructSingleAntSolution() {
            int size = distanceMatrix.getSize();
            boolean[] visited = new boolean[size];
            int[] route = new int[size + 1];
            int totalDistance = 0;

            // Bắt đầu từ thành phố 0
            route[0] = 0;
            visited[0] = true;
            int currentCity = 0;

            for (int i = 1; i < size; i++) {
                int nextCity = chooseNextCity(currentCity, visited);
                route[i] = nextCity;
                visited[nextCity] = true;
                totalDistance += distanceMatrix.getDistance(currentCity, nextCity);
                currentCity = nextCity;
            }

            // Quay lại thành phố ban đầu
            route[size] = 0;
            totalDistance += distanceMatrix.getDistance(currentCity, 0);

            return new Result(route, totalDistance);
        }

        private int chooseNextCity(int currentCity, boolean[] visited) {
            double[] probabilities = new double[visited.length];
            double totalProbability = 0.0;

            // Tính xác suất cho từng thành phố
            for (int city = 0; city < visited.length; city++) {
                if (!visited[city]) {
                    probabilities[city] = Math.pow(pheromones[currentCity][city], alpha) *
                            Math.pow(1.0 / distanceMatrix.getDistance(currentCity, city), beta);
                    totalProbability += probabilities[city];
                }
            }

            // Chuẩn hóa xác suất
            for (int city = 0; city < probabilities.length; city++) {
                if (!visited[city]) {
                    probabilities[city] /= totalProbability;
                }
            }

            // Chọn thành phố theo xác suất
            double rand = Math.random();
            double cumulativeProbability = 0.0;
            for (int city = 0; city < probabilities.length; city++) {
                if (!visited[city]) {
                    cumulativeProbability += probabilities[city];
                    if (rand <= cumulativeProbability) {
                        return city;
                    }
                }
            }

            // Trường hợp không chọn được thành phố (hiếm khi xảy ra)
            for (int city = 0; city < visited.length; city++) {
                if (!visited[city]) {
                    return city;
                }
            }

            throw new IllegalStateException("Không thể tìm thành phố tiếp theo");
        }

        private void updatePheromones(List<Result> solutions) {
            // Bay hơi pheromone
            for (int i = 0; i < pheromones.length; i++) {
                for (int j = 0; j < pheromones[i].length; j++) {
                    pheromones[i][j] *= (1 - evaporationRate);
                }
            }

            // Bổ sung pheromone
            for (Result solution : solutions) {
                int[] route = solution.getRoute();
                double pheromoneDeposit = 1.0 / solution.getTotalDistance();

                for (int i = 0; i < route.length - 1; i++) {
                    pheromones[route[i]][route[i + 1]] += pheromoneDeposit;
                    pheromones[route[i + 1]][route[i]] += pheromoneDeposit;
                }
            }
        }
    }

    // Lớp lưu trữ kết quả
    public static class Result {
        private int[] route;
        private int totalDistance;
        
        public Result(int[] route, int totalDistance) {
            this.route = route;
            this.totalDistance = totalDistance;
        }

        public int[] getRoute() {
            
            return route;
        }

        public int getTotalDistance() {
            return totalDistance;
        }

        public void printRoute() {
            //Chuyển đổi mảng số thành mảng kí tự
            char[] routeCharaters = new char[route.length];
            for(int i = 0; i < route.length; i++) {
                routeCharaters[i] = (char) ('A' + route[i]);
            }
            System.out.println("Lộ trình: " + Arrays.toString(routeCharaters));
            System.out.println("Tổng khoảng cách: " + totalDistance);
        }
    }
   
 
    
}
