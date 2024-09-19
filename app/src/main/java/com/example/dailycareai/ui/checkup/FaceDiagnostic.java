package com.example.dailycareai.ui.checkup;

public class FaceDiagnostic {

    private int diagnosticId;
    private int drowsiness;
    private int stableHeadPosition;
    private int regularBlinking;
    private int smiling;
    private int overallDiagnostic;
    private String dateDiagnostic;
    private int accountId;
    private String diagnosticDescription;
    private String drowsinessDescription;

    public int getDrowsiness() {
        return drowsiness;
    }

    public void setDrowsiness(int drowsiness) {
        this.drowsiness = drowsiness;
    }

    public int getStableHeadPosition() {
        return stableHeadPosition;
    }

    public void setStableHeadPosition(int stableHeadPosition) {
        this.stableHeadPosition = stableHeadPosition;
    }

    public int getRegularBlinking() {
        return regularBlinking;
    }

    public void setRegularBlinking(int regularBlinking) {
        this.regularBlinking = regularBlinking;
    }

    public int getSmiling() {
        return smiling;
    }

    public void setSmiling(int smiling) {
        this.smiling = smiling;
    }

    public int getOverallDiagnostic() {
        return overallDiagnostic;
    }

    public void setOverallDiagnostic(int overallDiagnostic) {
        this.overallDiagnostic = overallDiagnostic;
    }

    public String getDateDiagnostic() {
        return dateDiagnostic;
    }

    public void setDateDiagnostic(String dateDiagnostic) {
        this.dateDiagnostic = dateDiagnostic;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getDiagnosticId() {
        return diagnosticId;
    }

    public void setDiagnosticId(int diagnosticId) {
        this.diagnosticId = diagnosticId;
    }

    public String getDiagnosticDescription() {
        return diagnosticDescription;
    }

    public void setDiagnosticDescription(String diagnosticDescription) {
        this.diagnosticDescription = diagnosticDescription;
    }

    public String getDrowsinessDescription() {
        return drowsinessDescription;
    }

    public void setDrowsinessDescription(String drowsinessDescription) {
        this.drowsinessDescription = drowsinessDescription;
    }
}
