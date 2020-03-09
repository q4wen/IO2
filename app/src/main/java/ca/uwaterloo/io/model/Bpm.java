package ca.uwaterloo.io.model;

public class Bpm {
    private Integer bpm;
    private String createdDate;

    public Bpm(int bpm, String createdDate) {
        this.bpm = bpm;
        this.createdDate = createdDate;
    }

    Integer getBpm() {
        return bpm;
    }

    void setBpm(Integer bpm) {
        this.bpm = bpm;
    }

    String getCreatedDate() {
        return createdDate;
    }

    void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
