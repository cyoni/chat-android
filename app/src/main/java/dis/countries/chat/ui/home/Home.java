package dis.countries.chat.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

import dis.countries.chat.Controller;
import dis.countries.chat.R;
import dis.countries.chat.ui.chat.Conversation;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        setFirebase();
        Button buttun = findViewById(R.id.button);


        buttun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText txt_nickname = findViewById(R.id.nickname);
                String nickname = txt_nickname.getText().toString();
                addMessage(nickname);
            }
        });
    }

    private void setFirebase() {
        Controller.mFunctions = FirebaseFunctions.getInstance();
        Controller.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private Task<String> addMessage(final String nickname) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", nickname);

        return Controller.mFunctions
                .getHttpsCallable("register")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        if (!result.equals("busy")){
                            login(nickname, result);
                        }
                        else{
                            System.out.println("error");
                        }
                        return result;
                    }
                });
    }

    private void login(final String nickname, final String token) {

        Intent intent = new Intent(this, Conversation.class);
        intent.putExtra("token", token);
        intent.putExtra("nickname", nickname);

        startActivity(intent);
        finish();
    }


}