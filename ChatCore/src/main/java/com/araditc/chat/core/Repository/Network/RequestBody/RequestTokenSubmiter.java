package com.araditc.chat.core.Repository.Network.RequestBody;

import com.google.gson.annotations.SerializedName;

public class RequestTokenSubmiter {

    @SerializedName("token")
    private String token;

    @SerializedName("packageName")
    private String pakcageName;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("deviceName")
    private String deviceName;

    public RequestTokenSubmiter(String token, String pakcageName, String deviceId, String deviceName) {
        this.token = token;
        this.pakcageName = pakcageName;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }
}
