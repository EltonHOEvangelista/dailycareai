package com.example.dailycareai.ui.checkup;

public class AICameraModel {

    private int faceDetectionId;
    private int faceDetectionStatus;
    private int faceDetectionControl;
    private String faceDetectionDescription;

    public int getFaceDetectionId() {
        return faceDetectionId;
    }

    public void setFaceDetectionId(int faceDetectionId) {
        this.faceDetectionId = faceDetectionId;
    }

    public int getFaceDetectionStatus() {
        return faceDetectionStatus;
    }

    public void setFaceDetectionStatus(int faceDetectionStatus) {
        this.faceDetectionStatus = faceDetectionStatus;
    }

    public int getFaceDetectionControl() {
        return faceDetectionControl;
    }

    public void setFaceDetectionControl(int faceDetectionControl) {
        this.faceDetectionControl = faceDetectionControl;
    }

    public String getFaceDetectionDescription() {
        return faceDetectionDescription;
    }

    public void setFaceDetectionDescription(String faceDetectionDescription) {
        this.faceDetectionDescription = faceDetectionDescription;
    }
}
