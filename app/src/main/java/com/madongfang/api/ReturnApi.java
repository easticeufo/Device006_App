package com.madongfang.api;

/**
 * Created by madongfang on 17/9/6.
 */

public class ReturnApi {

    public ReturnApi() {
        super();
    }

    public ReturnApi(int returnCode, String returnMsg) {
        super();
        this.returnCode = returnCode;
        this.returnMsg = returnMsg;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    @Override
    public String toString() {
        return "ReturnApi{" +
                "returnCode=" + returnCode +
                ", returnMsg='" + returnMsg + '\'' +
                '}';
    }

    private int returnCode;

    private String returnMsg;
}
