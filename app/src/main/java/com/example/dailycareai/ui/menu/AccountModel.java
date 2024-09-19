package com.example.dailycareai.ui.menu;

public class AccountModel {

    private int accountId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirmPassword;
    private int accountEnable;
    private int savePassword;

    public AccountModel() {
    }

    public AccountModel(int accountId, String firstName, String lastName,
                        String email, String password, String confirmPassword, int accountEnable) {
        this.accountId = accountId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.accountEnable = accountEnable;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public int isAccountEnable() {
        return accountEnable;
    }

    public void setAccountEnable(int accountEnable) {
        this.accountEnable = accountEnable;
    }

    public int getAccountEnable() {
        return accountEnable;
    }

    public int isSavePassword() {
        return savePassword;
    }

    public void setSavePassword(int savePassword) {
        this.savePassword = savePassword;
    }
}
