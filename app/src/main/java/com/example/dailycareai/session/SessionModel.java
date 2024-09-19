package com.example.dailycareai.session;

public class SessionModel {

    private int sessionId;
    private String timeBegin;
    private String timeEnd;
    private int isSessionOn;
    private int accountId;

    public SessionModel() {
    }

    public SessionModel(int sessionId, String timeBegin, String timeEnd, int isSessionOn, int accountId) {
        this.sessionId = sessionId;
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.isSessionOn = isSessionOn;
        this.accountId = accountId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getTimeBegin() {
        return timeBegin;
    }

    public void setTimeBegin(String timeBegin) {
        this.timeBegin = timeBegin;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    public int isSessionOn() {
        return isSessionOn;
    }

    public void setSessionOn(int sessionOn) {
        isSessionOn = sessionOn;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
}
