package com.madongfang;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.madongfang.api.ChannelApi;

/**
 *
 * Created by madongfang on 17/9/13.
 */

public class GetLiquidDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_get_liquid, null);
        builder.setView(dialogView);

        final ChannelApi currentChannel = ((MainActivity)getActivity()).getCurrentChannel();

        nameTextView = (TextView)dialogView.findViewById(R.id.name_text_view);
        nameTextView.setText(currentChannel.getName());
        priceTextView = (TextView)dialogView.findViewById(R.id.price_text_view);
        priceTextView.setText(String.format("单价：%.2f元/100ml", currentChannel.getPrice()/100.0));
        quantityTextView = (TextView)dialogView.findViewById(R.id.quantity_text_view);
        addButton = (ImageButton)dialogView.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity++;
                quantityTextView.setText(String.valueOf(quantity));
            }
        });
        subButton = (ImageButton)dialogView.findViewById(R.id.sub_button);
        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity > 1)
                {
                    quantity--;
                    quantityTextView.setText(String.valueOf(quantity));
                }
            }
        });
        paymentButton = (Button)dialogView.findViewById(R.id.payment_button);
        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (dialogResultListener != null)
                {
                    dialogResultListener.getPaymentAmount(quantity * currentChannel.getPrice());
                }
            }
        });

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

    public void open(FragmentManager fragmentManager, DialogResultListener dialogResultListener)
    {
        this.dialogResultListener = dialogResultListener;
        show(fragmentManager, getClass().getSimpleName());
    }

    public interface DialogResultListener
    {
        public void getPaymentAmount(int amount);
    }

    private DialogResultListener dialogResultListener;

    private TextView nameTextView;

    private TextView priceTextView;

    private TextView quantityTextView;

    private ImageButton subButton;

    private ImageButton addButton;

    private Button paymentButton;

    private int quantity = 1;
}
