package hangryhippos.cappturetheflag;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity implements
        View.OnClickListener{
    public String displayName;
    private String connectionType="4G";

    public String getDisplayName(){
        return displayName;
    }
    public void setDisplayName(String newname){
        displayName=newname;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        updateNameTextView();

        Button change = findViewById(R.id.btn_change);
        change.setOnClickListener(this);
    }

    private void updateNameTextView(){
        SharedPreferences settings = this.getSharedPreferences(HomeActivity.APP_NAME, Context.MODE_PRIVATE);
        displayName = settings.getString(HomeActivity.DISPLAY_NAME_KEY, "not set");
        String formatName = getString(R.string.format_name);
        String newName = String.format(formatName, displayName);
        TextView tvName = (TextView) findViewById(R.id.txt_view_name);
        tvName.setText(newName);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_change){
            enterDisplayName();
        }
    }

    private void enterDisplayName(){

        final EditText input = new EditText(this);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.enter_name)
                .setMessage(R.string.msg_enter_name)
                .setView(input)
                .setPositiveButton(R.string.okay, null)
                .create();

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        String displayName = input.getText().toString();
                        //Checks that a string has been entered
                        if (displayName.length() > 0) {
                            saveDisplayName(displayName);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    private void saveDisplayName(String name){
        SharedPreferences.Editor editor = this.getSharedPreferences(HomeActivity.APP_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(HomeActivity.DISPLAY_NAME_KEY, name);
        editor.apply();
        updateNameTextView();
    }
}
