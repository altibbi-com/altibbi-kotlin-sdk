package com.altibbi.cdsSdk;

public interface NetResult {
    void onSuccess(String response);
    void onFailure(String error);
}
