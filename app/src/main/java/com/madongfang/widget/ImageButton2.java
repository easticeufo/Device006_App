package com.madongfang.widget;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.madongfang.R;

/**
 * Created by madongfang on 17/9/11.
 */

public class ImageButton2 extends LinearLayout {

    public ImageButton2(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.image_button_2, this);

        imageView = (ImageView)findViewById(R.id.image_view);
        buttonLeft = (Button)findViewById(R.id.button_left);
        buttonRight = (Button)findViewById(R.id.button_right);
    }

    public void setImageURI(Uri uri)
    {
        imageView.setImageURI(uri);
    }

    public void setImageResource(int resId)
    {
        imageView.setImageResource(resId);
    }

    public void setOnLeftClickListener(android.view.View.OnClickListener l)
    {
        buttonLeft.setOnClickListener(l);
    }

    public void setOnRightClickListener(android.view.View.OnClickListener l)
    {
        buttonRight.setOnClickListener(l);
    }

    private ImageView imageView;

    private Button buttonLeft;

    private Button buttonRight;
}
