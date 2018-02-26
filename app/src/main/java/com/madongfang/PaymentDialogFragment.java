package com.madongfang;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.madongfang.api.ChannelApi;
import com.madongfang.util.YilingUtil;

import java.io.IOException;

/**
 *
 * Created by madongfang on 17/9/15.
 */

public class PaymentDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "PaymentDialogFragment onCreateDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_payment, null);
        builder.setView(dialogView);

        wechatQrcodeImageView = dialogView.findViewById(R.id.wechat_qrcode);
        alipayQrcodeImageView = dialogView.findViewById(R.id.alipay_qrcode);

        tradeNumbers[0] = tradeNumbers[1] = null;

        YilingUtil.qrcodePay(merchantNumber, cashierNumber, totalFee, uniqueNumber, YilingUtil.TYPE_WECHAT,
                new YilingUtil.QrcodePayListener() {
                    @Override
                    public void onSuccess(YilingUtil.QrcodePayResult qrcodePayResult) {
                        Bitmap qrcodeBitmap = generateQrcode(qrcodePayResult.getQrcodeString(), 400, 400);
                        wechatQrcodeImageView.setImageBitmap(qrcodeBitmap);
                        tradeNumbers[0] = qrcodePayResult.getTradeNumber();
                    }

                    @Override
                    public void onFailure(String msg) {
                        Log.d(TAG, "qrcodePay onFailure: "+msg);
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                    }
                });

        YilingUtil.qrcodePay(merchantNumber, cashierNumber, totalFee, uniqueNumber, YilingUtil.TYPE_ALIPAY,
                new YilingUtil.QrcodePayListener() {
                    @Override
                    public void onSuccess(YilingUtil.QrcodePayResult qrcodePayResult) {
                        Bitmap qrcodeBitmap = generateQrcode(qrcodePayResult.getQrcodeString(), 400, 400);
                        alipayQrcodeImageView.setImageBitmap(qrcodeBitmap);
                        tradeNumbers[1] = qrcodePayResult.getTradeNumber();
                    }

                    @Override
                    public void onFailure(String msg) {
                        Log.d(TAG, "qrcodePay onFailure: "+msg);
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                    }
                });

        stopCheckPaymentThread = false;
        checkPaymentThread.start();

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();

        if (dialog != null)
        {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

            dialog.getWindow().setLayout((int)(dm.widthPixels * 0.6), (int)(dm.heightPixels * 0.5));
        }
    }

    @Override
    public void onDestroy()
    {
        if (!stopCheckPaymentThread)
        {
            stopCheckPaymentThread = true;
            Message msg = new Message();
            msg.what = MainActivity.TRADE_CLOSE;
            ((MainActivity)getActivity()).getMainHandler().sendMessage(msg);
        }
        super.onDestroy();
    }

    public void open(FragmentManager fragmentManager, String merchantNumber, String cashierNumber, int totalFee, String uniqueNumber)
    {
        this.merchantNumber = merchantNumber;
        this.cashierNumber = cashierNumber;
        this.totalFee = totalFee;
        this.uniqueNumber = uniqueNumber;
        show(fragmentManager, getClass().getSimpleName());
    }

    private static final String TAG = "PaymentDialogFragment";

    private String merchantNumber;
    private String cashierNumber;
    private int totalFee;
    private String uniqueNumber;
    private String[] tradeNumbers = new String[2];

    private ImageView wechatQrcodeImageView;

    private ImageView alipayQrcodeImageView;

    private boolean stopCheckPaymentThread = false;

    private Thread checkPaymentThread = new Thread() {
        @Override
        public void run() {
            int count = 0;
            while (!stopCheckPaymentThread)
            {
                for (final String tradeNumber : tradeNumbers)
                {
                    if (tradeNumber != null)
                    {
                        try {
                            YilingUtil.PayQueryResult payQueryResult = YilingUtil.payQuery(merchantNumber, tradeNumber, uniqueNumber);
                            if ("SUCCESS".equals(payQueryResult.getResultCode()))
                            {
                                stopCheckPaymentThread = true;
                                dismiss();
                                Message msg = new Message();
                                msg.what = MainActivity.PAY_SUCCESS;
                                ((MainActivity)getActivity()).getMainHandler().sendMessage(msg);
                                return;
                            }
                            else
                            {
                                Log.d(TAG, "订单号："+tradeNumber+"尚未支付");
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "查询支付结果失败：", e);
                        }
                    }
                }

                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "catch InterruptedException：", e);
                }

                count++;

                if (count >= 30)
                {
                    dismiss();
                }
            }
        }
    };

    private Bitmap generateQrcode(String content, int width, int height)
    {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            int[] pixels = new int[width*height];
            for (int i = 0; i < height; i++)
            {
                for (int j = 0; j < width; j++)
                {
                    if (encode.get(j, i))
                    {
                        pixels[i * width + j] = 0x00000000;
                    }
                    else
                    {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }

            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            Log.e(TAG, "generateQrcode Exception:", e);
            return null;
        }

    }
}
