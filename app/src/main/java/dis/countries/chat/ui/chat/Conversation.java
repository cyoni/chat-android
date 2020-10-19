package dis.countries.chat.ui.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import dis.countries.chat.Controller;
import dis.countries.chat.Item;
import dis.countries.chat.R;
import dis.countries.chat.RecyclerViewAdapter;

public class Conversation extends AppCompatActivity {

    RecyclerViewAdapter adapter;
    Button sendButton;
    ArrayList<Item> messages = new ArrayList<>();
    EditText txt_message;
    RecyclerView recyclerView;
    String my_nickname, myToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        sendButton = findViewById(R.id.sendMessage);
        txt_message = findViewById(R.id.message);
        recyclerView = findViewById(R.id.recycleView);

        my_nickname = getIntent().getStringExtra("nickname");
        myToken = getIntent().getStringExtra("token");


        setRecycleview();

        startListening();
        keepMeAlive();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void keepMeAlive() {

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Controller.mDatabase.getRef().child("users").child(my_nickname).child("timestamp").setValue(now);
            }
        }, 0, 10000);


    }

    private void startListening() {
        Controller.mDatabase.child("messages").child(myToken).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("raw data: " + snapshot.getValue());

                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                DataSnapshot tmp;

                while (iterator.hasNext()) {
                    tmp = iterator.next();
                    //noinspection unchecked
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) tmp.getValue();

                    String msgId = tmp.getKey();
                    String message = (String) dataMap.get("message");
                    String nickname = (String) dataMap.get("sender");
                    String messageType = (String) dataMap.get("type");

                    Item item = new Item(nickname, message, messageType);
                    messages.add(item);
                    recyclerView.scrollToPosition(messages.size()-1);
                    adapter.notifyItemInserted(messages.size()-1);

                    deleteMsg(msgId);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteMsg(String msgId) {
        Controller.mDatabase.child("messages").child(myToken).child(msgId).setValue(null);
        System.out.println("token " + myToken + ", msgid: " + msgId);
    }

    private void setRecycleview() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(this, messages);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void sendMessage() {

        String message = txt_message.getText().toString();
      //  messages.add(new Item(my_nickname, message));
      //  adapter.notifyDataSetChanged();

        txt_message.setText("");

        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("token", myToken);
        data.put("nickname", my_nickname);

        Controller.mFunctions
                .getHttpsCallable("sendMessage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) {

                        String answer = String.valueOf(task.getResult().getData());
                        System.out.println("answer: " + answer);

                        if (!answer.equals("ERROR")) {

                        }
                        return "";
                    }
                });


    }
}