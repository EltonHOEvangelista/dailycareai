package com.example.dailycareai.ui.checkup;

public class FaceModel {

    private float leftEyeOpenProbability;
    private float rightEyeOpenProbability;
    private float smileProbability;
    private float headEulerAngleY;
    private float headEulerAngleX;
    private float headEulerAngleZ;

    //default constructor
    public FaceModel(float leftEyeOpenProbability, float rightEyeOpenProbability, float smileProbability,
                     float headEulerAngleY, float headEulerAngleX, float headEulerAngleZ) {
        this.leftEyeOpenProbability = leftEyeOpenProbability;
        this.rightEyeOpenProbability = rightEyeOpenProbability;
        this.smileProbability = smileProbability;
        this.headEulerAngleX = headEulerAngleX;
        this.headEulerAngleY = headEulerAngleY;
        this.headEulerAngleY = headEulerAngleZ;
    }

    public FaceModel() {
    }

    public float getLeftEyeOpenProbability() {
        return leftEyeOpenProbability;
    }

    public void setLeftEyeOpenProbability(float leftEyeOpenProbability) {
        this.leftEyeOpenProbability = leftEyeOpenProbability;
    }

    public float getRightEyeOpenProbability() {
        return rightEyeOpenProbability;
    }

    public void setRightEyeOpenProbability(float rightEyeOpenProbability) {
        this.rightEyeOpenProbability = rightEyeOpenProbability;
    }

    public float getSmileProbability() {
        return smileProbability;
    }

    public void setSmileProbability(float smileProbability) {
        this.smileProbability = smileProbability;
    }

    public float getHeadEulerAngleY() {
        return headEulerAngleY;
    }

    public void setHeadEulerAngleY(float headEulerAngleY) {
        this.headEulerAngleY = headEulerAngleY;
    }

    public float getHeadEulerAngleX() {
        return headEulerAngleX;
    }

    public void setHeadEulerAngleX(float headEulerAngleX) {
        this.headEulerAngleX = headEulerAngleX;
    }

    public float getHeadEulerAngleZ() {
        return headEulerAngleZ;
    }

    public void setHeadEulerAngleZ(float headEulerAngleZ) {
        this.headEulerAngleZ = headEulerAngleZ;
    }
}
