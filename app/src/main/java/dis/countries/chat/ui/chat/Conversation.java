package dis.countries.chat.ui.chat;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
import dis.countries.chat.Parameters;
import dis.countries.chat.Participants;
import dis.countries.chat.R;
import dis.countries.chat.RecyclerViewAdapter;
import dis.countries.chat.Time;

public class Conversation extends Fragment implements RecyclerViewAdapter.ItemClickListener {

    private RecyclerViewAdapter adapter;
    private Button sendButton;
    private ArrayList<Item> messages = new ArrayList<>();
    private EditText txt_message;
    private RecyclerView recyclerView;
    private String myNickname, myToken;
    private HashSet<String> messageTracker = new HashSet<>();
    private RelativeLayout layout;

    public Conversation(String token, String nickname){
        this.myToken = token;
        this.myNickname = nickname;
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
        layout = root.findViewById(R.id.layout);

        setRecyclerview();
        scrollRecyclerview();
        setButtonListener();

        return root;
    }

    private void setButtonListener() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick();
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
                        long timestamp = (long) dataMap.get("timestamp");

                        if   (  message == null  ||
                                nickname == null ||
                                messageType == null
                             ) return;

                        if (!nickname.equals(myNickname) || !messageType.equals("regular")) {

                            Item item = new Item(nickname, message, messageType, timestamp);
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

                            scrollDownIfPossible();

                        } else if (nickname.equals(MainActivity.my_nickname)){
                            long tracker = (long) dataMap.get("tracker");
                            changeMessageDeliveryStatus((int)tracker, Parameters.DELIVERED);
                        }
                        deleteMsgFromServer(msgId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void scrollDownIfPossible() {
        if (!MainActivity.imOnConversationTab || recyclerView.canScrollVertically(1)) {
            MainActivity.setBadge();
            adapter.notifyItemInserted(messages.size() - 1);
        } else {
            MainActivity.removeBadge();
            adapter.notifyItemInserted(messages.size() - 1);
            recyclerView.scrollToPosition(messages.size() - 1);
        }
    }

    private void deleteMsgFromServer(String msgId) {
        Controller.mDatabase.child("messages").child(myToken).child(msgId).setValue(null);
    }

    private void setRecyclerview() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerViewAdapter(getContext(), R.layout.item, messages);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void markMsgAsNotArrived(final int msgId) {
        messages.get(msgId).setMessageStatus(Parameters.DELIVERY_FAILED);
        adapter.notifyItemChanged(msgId);
    }

    @Override
    public void onItemClick(@NonNull View view, int position) {
        Item currentMsg = messages.get(position);

        if (currentMsg.getDeleveryId() == R.drawable.ic_baseline_error_24){
            changeMessageDeliveryStatus(position, Parameters.DELIVERING_MSG);
            sendMessage(currentMsg.getMessage(), position);
        }
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {

        cleanOldData(holder);

        String message = messages.get(position).getMessage();
        String nickname = messages.get(position).getNickname();
        String msgType = messages.get(position).getMsgType();
        String time = messages.get(position).getTime();
        holder.status.setOnClickListener(holder);

        if (msgType.equals("join") || msgType.equals("leave")){
            broadcast(holder, message);
        } else if (msgType.equals("status")) {
            broadcast(holder, nickname);
        }
        else{
            if (nickname.equals(MainActivity.my_nickname)){
                holder.status.setVisibility(View.VISIBLE);
                holder.status.setBackgroundResource(messages.get(position).getDeleveryId());
            }

            String msg = "<font color=\"#454545\"><b>" + nickname + "</b></font>  " + message;
            holder.time.setText(time);
            broadcast(holder, msg);
        }
    }


    private void cleanOldData(RecyclerViewAdapter.ViewHolder holder) {
        holder.status.setBackground(null);
        holder.myTextView.setText("");
    }

    private void broadcast(RecyclerViewAdapter.ViewHolder holder, String msg) {
        holder.myTextView.setText(Html.fromHtml(msg));
    }


    private void changeMessageDeliveryStatus(int position, final String status) {
        Item currentMsg = messages.get(position);
        currentMsg.setMessageStatus(status);
        adapter.notifyItemChanged(position);
    }

    private void ShakeMessageAndButton(){
        Animator.shake(txt_message);
        Animator.shake(sendButton);
    }

    private boolean isMessageValid(final String message){
        return !message.isEmpty();
    }

    private void buttonClick(){
        final String message = txt_message.getText().toString();
        if (!isMessageValid(message)){
            ShakeMessageAndButton();
        }
        else{
            Item item = new Item(myNickname, message, Parameters.REGULAR, Time.getTimeInMS());
            item.setMessageStatus(Parameters.DELIVERING_MSG);
            messages.add(item);
            final int msgId = messages.size()-1;
            changeMessageDeliveryStatus(msgId, Parameters.DELIVERING_MSG);
            scrollDownIfPossible();
            txt_message.setText("");
            sendMessage(message, msgId);
        }
    }

    private void sendMessage(final String message, final int msgId){

        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("token", myToken);
        data.put("nickname", myNickname);
        data.put("MsgCounterACK", msgId);

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
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                markMsgAsNotArrived(msgId);

                checkReason();


            }
        });
    }

    private void checkReason() {
        // 1. check internet connection
        // if (!isConnectedToInternet()) {  }
        Controller.showSnackbar(layout, "No internet connection.");
        // else if (!isStillAlive(myToken)){
        Snackbar snackbar = Snackbar.make(layout , "You are disconnected.", Snackbar.LENGTH_INDEFINITE).setAction("RECONNECT", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        snackbar.show();
        // }

        // 2. check if you are still connected to the database
    }

}