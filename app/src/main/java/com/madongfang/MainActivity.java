package com.madongfang;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.madongfang.api.ChannelApi;
import com.madongfang.api.DeviceApi;
import com.madongfang.api.InvestorApi;
import com.madongfang.api.ReturnApi;
import com.madongfang.util.HttpUtil;
import com.madongfang.util.SerialPortUtil;
import com.madongfang.util.YilingUtil;
import com.madongfang.widget.ImageButton2;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PAY_SUCCESS = 1;

    public static final int TRADE_CLOSE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverUsername = getString(R.string.server_username);
        serverPassword = getString(R.string.server_password);
        serverPath = getString(R.string.server_path);

        videoView = (VideoView)findViewById(R.id.video_view);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        else
        {
            playAdvert();
        }

        menuButton = (ImageButton) findViewById(R.id.menu_button);
        menuButton.setOnClickListener(this);
        optionsLayout = (LinearLayout) findViewById(R.id.options_layout);

        HttpUtil.get(serverPath + "api/android/devices/"+deviceId, serverUsername, serverPassword, new HttpUtil.ResponseListener(DeviceApi.class){

            @Override
            public void onSuccess(Object obj) {
                Log.d(TAG, "onSuccess: "+obj);
                DeviceApi device = (DeviceApi)obj;
                InvestorApi investor = device.getInvestor();
                if (investor != null)
                {
                    cashierUsername = investor.getCashierUsername();
                    cashierPassword = investor.getCashierPassword();
                }
                for (final ChannelApi channelAPi : device.getChannels()) {
                    ImageButton2 imageButton2 = new ImageButton2(MainActivity.this);
                    imageButton2.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
                    imageButton2.setOnLeftClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: "+channelAPi.getPosition());
                            currentChannel = channelAPi;
                            new DetailDialogFragment().show(getFragmentManager(), "DetailDialogFragment");
                        }
                    });
                    imageButton2.setOnRightClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: "+channelAPi.getPosition());
                            currentChannel = channelAPi;
                            new GetLiquidDialogFragment().open(getFragmentManager(), new GetLiquidDialogFragment.DialogResultListener() {
                                @Override
                                public void getPaymentAmount(int amount) {
                                    Log.d(TAG, "getPaymentAmount: amount="+amount);

                                    paymentDialogFragment.open(getFragmentManager(), merchantNumber, cashierNumber, amount, deviceId);
                                }
                            });


                        }
                    });

                    optionsLayout.addView(imageButton2);
                }

                YilingUtil.login(cashierUsername, cashierPassword,
                        new YilingUtil.LoginListener() {
                            @Override
                            public void onSuccess(YilingUtil.LoginResult loginResult) {
                                merchantNumber = loginResult.getMerchantNumber();
                                cashierNumber = loginResult.getCashierNumber();
                            }

                            @Override
                            public void onFailure(String msg) {
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onFailure(ReturnApi returnApi) {
                Log.w(TAG, "onFailure: "+returnApi);
                Toast.makeText(MainActivity.this, returnApi.getReturnMsg(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    playAdvert();
                }
                else
                {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null)
        {
            videoView.suspend();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.menu_button:
                menuButton.setVisibility(View.GONE);
                optionsLayout.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
    }

    public ChannelApi getCurrentChannel()
    {
        return currentChannel;
    }

    public Handler getMainHandler() {
        return mainHandler;
    }

    private static final String TAG = "MainActivity";

    private String deviceId = "test12345";

    private VideoView videoView;
    private ImageButton menuButton;
    private LinearLayout optionsLayout;
    private String serverUsername;
    private String serverPassword;
    private String serverPath;
    private ChannelApi currentChannel;
    private String cashierUsername;
    private String cashierPassword;
    private String merchantNumber;
    private String cashierNumber;
    private PaymentDialogFragment paymentDialogFragment = new PaymentDialogFragment();;

    private Handler mainHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case PAY_SUCCESS:
                    Toast.makeText(MainActivity.this, "支付成功", Toast.LENGTH_LONG).show();
                    break;
                case TRADE_CLOSE:
                    Toast.makeText(MainActivity.this, "订单关闭", Toast.LENGTH_LONG).show();
                    break;
                default:
            }
        }
    };

    private void playAdvert()
    {
        File videoFile = new File(Environment.getExternalStorageDirectory(), "advert.mp4");
        videoView.setVideoPath(videoFile.getPath());
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
    }
}
