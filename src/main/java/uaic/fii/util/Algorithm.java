package uaic.fii.util;

public enum Algorithm {
    KNN_MIN_READY_TIME("greedy/knn_min_ready_time"),
    KNN_MIN_DUE_TIME("greedy/knn_min_due_time"),
    NN_MIN_READY_TIME("greedy/min_ready_time"),
    NN_MIN_DUE_TIME("greedy/min_due_time"),
    EXACT("exact"),
    GA("ga");

    Algorithm(String saveLocation) {
        this.saveLocation = saveLocation;
    }
    private String saveLocation;

    public String getSaveLocation() {
        return saveLocation;
    }
}


enum Greedy {

}