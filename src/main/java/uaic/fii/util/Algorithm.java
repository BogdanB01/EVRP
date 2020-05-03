package uaic.fii.util;

public enum Algorithm {
    GREEDY("greedy"),
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