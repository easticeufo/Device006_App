package com.madongfang.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.madongfang.api.ReturnApi;

import java.io.IOException;

/**
 * Created by madongfang on 17/9/15.
 */

public class YilingUtil {

    public static final int TYPE_WECHAT = 1;
    public static final int TYPE_ALIPAY = 2;

    public static void login(String username, String password, final LoginListener loginListener)
    {
        HttpUtil.get(SERVER_PATH + "?" + String.format("service=%s&username=%s&password=%s&dynamic_type=2", CMD_LOGIN, username, password),
                new HttpUtil.ResponseListener(LoginResult.class) {
                    @Override
                    public void onSuccess(Object obj) {
                        LoginResult loginResult = (LoginResult)obj;
                        if ("SUCCESS".equals(loginResult.getResultCode()))
                        {
                            loginListener.onSuccess(loginResult);
                        }
                        else
                        {
                            loginListener.onFailure(loginResult.getResultMsg());
                        }
                    }

                    @Override
                    public void onFailure(ReturnApi returnApi) {
                        loginListener.onFailure(returnApi.getReturnMsg());
                    }
                });
    }

    public static void qrcodePay(String merchantNumber, String cashierNumber, int totalFee, String uniqueNumber, int payType, final QrcodePayListener qrcodePayListener)
    {
        HttpUtil.get(SERVER_PATH + "?" + String.format("service=%s&merchant_num=%s&total_fee=%.2f&terminal_unique_no=%s&cashier_num=%s&dynamic_type=%d", CMD_QRCODE_PAY, merchantNumber, (float)totalFee/100, uniqueNumber, cashierNumber, payType),
                new HttpUtil.ResponseListener(QrcodePayResult.class) {
                    @Override
                    public void onSuccess(Object obj) {
                        QrcodePayResult qrcodePayResult = (QrcodePayResult)obj;
                        if ("SUCCESS".equals(qrcodePayResult.getResultCode()))
                        {
                            qrcodePayListener.onSuccess(qrcodePayResult);
                        }
                        else
                        {
                            qrcodePayListener.onFailure(qrcodePayResult.getResultMsg());
                        }
                    }

                    @Override
                    public void onFailure(ReturnApi returnApi) {
                        qrcodePayListener.onFailure(returnApi.getReturnMsg());
                    }
                });
    }

    public static PayQueryResult payQuery(String merchantNumber, String tradeNumber, String uniqueNumber) throws IOException {
        return HttpUtil.get(SERVER_PATH + "?" + String.format("service=%s&merchant_num=%s&terminal_unique_no=%s&trace_num=%s", CMD_PAY_QUERY, merchantNumber, uniqueNumber, tradeNumber), PayQueryResult.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoginResult
    {
        public String getResultCode() {
            return resultCode;
        }

        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public String getResultMsg() {
            return resultMsg;
        }

        public void setResultMsg(String resultMsg) {
            this.resultMsg = resultMsg;
        }

        public String getCashierNumber() {
            return cashierNumber;
        }

        public void setCashierNumber(String cashierNumber) {
            this.cashierNumber = cashierNumber;
        }

        public String getMerchantNumber() {
            return merchantNumber;
        }

        public void setMerchantNumber(String merchantNumber) {
            this.merchantNumber = merchantNumber;
        }

        @JsonProperty(value = "result_code")
        private String resultCode;

        @JsonProperty(value = "result_msg")
        private String resultMsg;

        @JsonProperty(value = "cashier_num")
        private String cashierNumber;

        @JsonProperty(value = "merchant_num")
        private String merchantNumber;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QrcodePayResult
    {
        public String getResultCode() {
            return resultCode;
        }

        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public String getResultMsg() {
            return resultMsg;
        }

        public void setResultMsg(String resultMsg) {
            this.resultMsg = resultMsg;
        }

        public String getQrcodeString() {
            return qrcodeString;
        }

        public void setQrcodeString(String qrcodeString) {
            this.qrcodeString = qrcodeString;
        }

        public String getTradeNumber() {
            return tradeNumber;
        }

        public void setTradeNumber(String tradeNumber) {
            this.tradeNumber = tradeNumber;
        }

        @JsonProperty(value = "result_code")
        private String resultCode;

        @JsonProperty(value = "result_msg")
        private String resultMsg;

        @JsonProperty(value = "qr_code")
        private String qrcodeString;

        @JsonProperty(value = "trace_num")
        private String tradeNumber;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayQueryResult
    {
        public String getResultCode() {
            return resultCode;
        }

        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public String getResultMsg() {
            return resultMsg;
        }

        public void setResultMsg(String resultMsg) {
            this.resultMsg = resultMsg;
        }

        public String getTradeNumber() {
            return tradeNumber;
        }

        public void setTradeNumber(String tradeNumber) {
            this.tradeNumber = tradeNumber;
        }

        public String getTradeTime() {
            return tradeTime;
        }

        public void setTradeTime(String tradeTime) {
            this.tradeTime = tradeTime;
        }

        public String getTradeAmount() {
            return tradeAmount;
        }

        public void setTradeAmount(String tradeAmount) {
            this.tradeAmount = tradeAmount;
        }

        public String getTotalFee() {
            return totalFee;
        }

        public void setTotalFee(String totalFee) {
            this.totalFee = totalFee;
        }

        @JsonProperty(value = "result_code")
        private String resultCode;

        @JsonProperty(value = "result_msg")
        private String resultMsg;

        @JsonProperty(value = "trace_num")
        private String tradeNumber;

        @JsonProperty(value = "trans_time")
        private String tradeTime;

        @JsonProperty(value = "trans_amount")
        private String tradeAmount;

        @JsonProperty(value = "total_fee")
        private String totalFee;
    }

    public interface LoginListener
    {
        void onSuccess(LoginResult loginResult);

        void onFailure(String msg);
    }

    public interface QrcodePayListener
    {
        void onSuccess(QrcodePayResult qrcodePayResult);

        void onFailure(String msg);
    }

    private static final String TAG = "YilingUtil";

    private static final String SERVER_PATH = "https://mapi.020leader.com/index.php";

    private static final String CMD_LOGIN = "unipay.checkstand.login";

    private static final String CMD_QRCODE_PAY = "unipay.acquire.precreate";

    private static final String CMD_PAY_QUERY = "unipay.acquire.query";
}
