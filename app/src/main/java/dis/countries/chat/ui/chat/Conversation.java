package dis.countries.chat.ui.chat;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import dis.countries.chat.Animator;
import dis.countries.chat.Controller;
import dis.countries.chat.Item;
import dis.countries.chat.MainActivity;
import dis.countries.chat.Participants;
import dis.countries.chat.R;
import dis.countries.chat.RecyclerViewAdapter;
import dis.countries.chat.toast;

public class Conversation extends Fragment {

    private RecyclerViewAdapter adapter;
    private Button sendButton;
    private ArrayList<Item> messages = new ArrayList<>();
    private EditText txt_message;
    private RecyclerView recyclerView;
    private String my_nickname, myToken;
    private HashSet<String> messageTracker = new HashSet<>();

    public Conversation(String token, String nickname){
        this.myToken = token;
        this.my_nickname = nickname;
        listeningForNewMessages();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_conversation, container, false);

        sendButton = root.findViewById(R.id.sendMessage);
        txt_message = root.findViewById(R.id.message);
        recyclerView = root.findViewById(R.id.recycleView);

        setRecycleview();
        scrollRecyclerview();
        setButtonListener();

        return root;
    }

    private void setButtonListener() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void scrollRecyclerview(){
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)){
                    MainActivity.removeBadge();
                }
            }
        });
    }


    private void listeningForNewMessages() {
        Controller.mDatabase.child("messages").child(MainActivity.myToken).addValueEventListener(new ValueEventListener() {

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

                    if (!messageTracker.contains(msgId)) {
                        String message = (String) dataMap.get("message");
                        String nickname = (String) dataMap.get("sender");
                        String messageType = (String) dataMap.get("type");

                        if (!nickname.equals(my_nickname) || nickname.equals(my_nickname) && !messageType.equals("regular")) {

                            Item item = new Item(nickname, message, messageType);
                            messages.add(item);
                            messageTracker.add(msgId);

                            if (messageType.equals("leave")) {
                                Participants.remove(nickname);
                                MainActivity.updateOnlineTitle();
                            } else if (messageType.equals("join")) {
                                Participants.add(nickname);
                                MainActivity.updateOnlineTitle();
                            }

                            if (!MainActivity.imOnPeopleTab && messageType.equals("join") && !nickname.equals(MainActivity.my_nickname))
                                MainActivity.participantsSetBadge();

                            scrollDown();

                        } else if (nickname.equals(MainActivity.my_nickname)){
                            System.out.println("!!!!!!!!!!!!!!!!!!!");
                            long tracker = (long) dataMap.get("tracker");
                            messages.get((int) tracker).setMessageStatus("sent");
                            adapter.notifyItemChanged((int)  tracker);
                        }

                        deleteMsg(msgId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void scrollDown() {
        if (!MainActivity.imOnConversationTab || recyclerView.canScrollVertically(1)) {
            MainActivity.setBadge();
            adapter.notifyItemInserted(messages.size() - 1);
        } else {
            MainActivity.removeBadge();
            adapter.notifyItemInserted(messages.size() - 1);
            recyclerView.scrollToPosition(messages.size() - 1);
        }
    }

    private void deleteMsg(String msgId) {
        Controller.mDatabase.child("messages").child(myToken).child(msgId).setValue(null);
    }

    private void setRecycleview() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerViewAdapter(getContext(), messages);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void sendMessage() {

        String message = txt_message.getText().toString().trim();

        if (message.isEmpty()){
            Animator.shake(txt_message);
            Animator.shake(sendButton);
            return;
        }
        Item item = new Item(my_nickname, message, "regular");
        item.setMessageStatus("sending");
        messages.add(item);

        adapter.notifyItemInserted(messages.size()-1);

        scrollDown();

        txt_message.setText("");
        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("token", myToken);
        data.put("nickname", my_nickname);
        data.put("MsgCounterACK", messages.size()-1);

        Controller.mFunctions
                .getHttpsCallable("sendMessage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) {

                        String answer = String.valueOf(task.getResult().getData());
                        System.out.println("answer: " + answer);

                        return "";
                    }
                });


    }
}