package com.zematix.jworldcup.backend.dto;

import javax.validation.constraints.NotNull;

public class CommonResponse {

    @NotNull
    private Boolean successful;

    private String error;

    private Boolean modified;

    private Boolean confirmed;

    public CommonResponse() {
        this.successful = true;
    }

    public CommonResponse(Boolean successful, String error, Boolean modified) {
        this.successful = successful;
        this.error = error;
        this.modified = modified;
    }

    public CommonResponse(Boolean successful, String error) {
        this.successful = successful;
        this.error = error;
    }

    public CommonResponse(Boolean successful) {
        this.successful = successful;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Boolean getModified() {
        return modified;
    }

    public void setModified(Boolean modified) {
        this.modified = modified;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

}
