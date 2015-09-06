package me.cullycross.arbuz.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.cullycross.arbuz.R;

public class RedButtonActivity extends AppCompatActivity {
    @Bind(R.id.redButton)
    Button redButton;
    @Bind(R.id.messageTextView)
    TextView messageTextView;
    @Bind(R.id.warningImageView)
    ImageView warningImageView;
    @Bind(R.id.passwordEditText)
    EditText passwordEditText;
    @Bind(R.id.cancelCallButton)
    Button cancelCallButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_red_button);
        ButterKnife.bind(this);

        cancelCallButton.setEnabled(false);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0) {
                    cancelCallButton.setEnabled(true);
                } else {
                    cancelCallButton.setEnabled(false);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_red_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.redButton)
    public void redButtonClick() {
        redButton.setVisibility(View.INVISIBLE);
        warningImageView.setVisibility(View.VISIBLE);
        messageTextView.setVisibility(View.VISIBLE);
        passwordEditText.setVisibility(View.VISIBLE);
        cancelCallButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.cancelCallButton)
    public void finishThis() {
        finish();
    }
}
