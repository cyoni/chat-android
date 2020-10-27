package dis.countries.chat.ui.favorite;


import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.agrawalsuneet.dotsloader.loaders.TashieLoader;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

import dis.countries.chat.Animator;
import dis.countries.chat.Controller;
import dis.countries.chat.MainActivity;
import dis.countries.chat.R;
import dis.countries.chat.toast;

public class Favorite extends AppCompatActivity {

    TashieLoader loader;
    TextInputLayout container;
    Button loginButton;
    EditText input_nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loader = findViewById(R.id.loader);
        container = findViewById(R.id.container);
        loginButton = findViewById(R.id.button);
        input_nickname = findViewById(R.id.nickname);

        setActionbar();
        setFirebase();
        setOnListener();
    }



    private void setOnListener() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitButtonClick();
            }
        });

        input_nickname.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submitButtonClick();
                    return true;
                }
                return false;
            }

        });
    }


    private void submitButtonClick() {

        String nick = input_nickname.getText().toString();
        applyAnimation();
        validateNickname();

        if (nick.trim().isEmpty()){
            stopAnimation();
        } else
            logIn(nick);
    }

    private void validateNickname() {
        if (input_nickname.getText().toString().trim().isEmpty()){
            stopAnimation();
            input_nickname.requestFocus();
            Controller.showKeyboard(input_nickname,this);
        }
    }

    private void setActionbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#ffffff\">Chat</font>"));
    }

    private void setFirebase() {
        Controller.mFunctions = FirebaseFunctions.getInstance();
        Controller.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private Task<String> logIn(final String nickname) {
        final RelativeLayout relativeLayout = findViewById(R.id.screen);


        Map<String, Object> data = new HashMap<>();
        data.put("nickname", nickname);

        return Controller.mFunctions
                .getHttpsCallable("register")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        String result = (String) task.getResult().getData();
                        if (result == null || result.isEmpty()){
                            toast.showToast(getApplicationContext(), "An error occurred (1)");
                            stopAnimation();
                        }
                        else if(result.equals("INVALID-NICKNAME")){
                            Controller.showSnackbar(relativeLayout, "Special characters are not allowed.");
                            stopAnimation();
                        }
                        else if (result.equals("busy")){
                            Controller.showSnackbar(relativeLayout, "Nickname is in use!");
                            stopAnimation();
                        }  else {
                            openChat(nickname.trim(), result);
                        }

                        return result;
                    }
                });
    }

    private void stopAnimation() {
        input_nickname.setVisibility(View.VISIBLE);
        container.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        Animator.shake(loginButton);
        Animator.shake(container);
    }

    private void applyAnimation() {
        loader.setVisibility(View.VISIBLE);
        Controller.hideKeyboard(this);
        input_nickname.setVisibility(View.INVISIBLE);
        container.setVisibility(View.INVISIBLE);
        loginButton.setVisibility(View.INVISIBLE);
    }

    private void openChat(final String nickname, final String token) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("token", token);
        intent.putExtra("nickname", nickname);
        startActivity(intent);
        finish();
    }


}