package hangryhippos.cappturetheflag;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity implements
        View.OnClickListener {
    private Integer pointer=0;
    private String[] hintTitles={"Title1","Title2"};
    private String[] hints={"Hint1","Hint2"};
    private String hintTitleCurrant="";
    private String hintCurrant="";
    private ImageButton LeftButton;
    private ImageButton RightButton;

    TextView hintTitleTV;
    TextView hintTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        LeftButton = findViewById(R.id.LeftButton);
        RightButton = findViewById(R.id.RightButton);

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
        }
    }
}
