package hangryhippos.cappturetheflag;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;


public class AlertDialogManager {

    private AlertDialog alertDialog;
    private View.OnClickListener positiveButtonListener;

    /**
     * Method builds an alert dialog which is used to prompt the user at times. It is adjustable to
     * the needs of the various classes if called by them.
     *
     * @param packageContext the current state of the app on initialisation
     * @param title the number of the alert, if any
     * @param message the main body text of the alert
     * @param positiveButton the text for the affirmative button action
     * @param negativeButtonNeeded whether an alert requires the use of the negative cancel button
     * @param fedListener a listener for the positive button
     */
    public void showAlertDialog(Context packageContext, String title, String message, String positiveButton, final boolean negativeButtonNeeded, View.OnClickListener fedListener) {
        this.positiveButtonListener = fedListener;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(packageContext, R.style.AlertDialogCustom))
                .setTitle(title).setMessage(message).setCancelable(false);

        if (negativeButtonNeeded) {
            alertDialogBuilder.setNegativeButton(R.string.cQuitCancelButton, null);
        }

        alertDialog = alertDialogBuilder.setPositiveButton(positiveButton, null).create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(positiveButtonListener);

                if (negativeButtonNeeded) {
                    Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    negativeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dismissAlertDialog();
                        }
                    });
                }
            }
        });
        alertDialog.show();
    }

    /**
     * Method used to dismiss the alert dialog
     */
    public void dismissAlertDialog() {
        alertDialog.dismiss();
    }

}




