package com.madongfang;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.madongfang.api.ChannelApi;

/**
 *
 * Created by madongfang on 17/9/12.
 */

public class DetailDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_detail, null);
        builder.setView(dialogView);

        ChannelApi currentChannel = ((MainActivity)getActivity()).getCurrentChannel();

        TextView textView = (TextView)dialogView.findViewById(R.id.desc_text_view);
        textView.setText(currentChannel.getName());

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

    private TextView textView;
}
