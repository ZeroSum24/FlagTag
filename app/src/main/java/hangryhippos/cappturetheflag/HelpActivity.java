package hangryhippos.cappturetheflag;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity implements
        View.OnClickListener {
    private Integer pointer=0;
    private String[] hintTitles={getString(R.string.hint_intro_title),getString(R.string.hint_get_started_title), getString(R.string.hint_rules_title)};
    private String[] hints={getString(R.string.hint_intro_msg),getString(R.string.hint_get_started_msg), getString(R.string.hint_rules_msg)};
    private String hintTitleCurrent="";
    private String hintCurrent="";
    private ImageButton LeftButton;
    private ImageButton RightButton;
    private ImageView homeButton;

    TextView hintTitleTV;
    TextView hintTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        LeftButton = findViewById(R.id.LeftButton);
        RightButton = findViewById(R.id.RightButton);
        homeButton = findViewById(R.id.circle);

        hintTitleTV = (TextView)findViewById(R.id.HintText);
        hintTV = (TextView)findViewById(R.id.TitleText);

        updateTextView(0);
    }

    private void updateTextView(int i) {
        if ((pointer + i > 0) && (pointer + i < hints.length)) {
            pointer += i;
            hintTitleTV.setText(hintTitles[pointer]);
            hintTV.setText(hints[pointer]);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.LeftButton:
                updateTextView(-1);
                break;
            case R.id.RightButton:
                updateTextView(1);
                break;
            case R.id.circle:
                Intent homeIntent = new Intent(this, HomeActivity.class);
                startActivity(homeIntent);
        }
    }
}
