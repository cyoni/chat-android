package dis.countries.chat;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Iterator;


public class Participants extends Fragment implements RecyclerViewAdapter.ItemClickListener{

    RecyclerView recyclerView;
    static ArrayList<User> participants = new ArrayList<>();
    static RecyclerViewAdapter adapter;


    public static void add(String nickname) {
        if (!nickname.equals(MainActivity.my_nickname)) {
            participants.add(new User(nickname));
            adapter.notifyItemInserted(participants.size()-1);
        }
    }

    public static void remove(String nickname) {
        for (int i=0; i<participants.size(); i++){
            User current = participants.get(i);
            if (current.getNickname().equals(nickname)){
                participants.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private void setRecyclerview() {
        adapter = new RecyclerViewAdapter(getContext(), R.layout.item_user, participants);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }


    private void getParticipants() {

        Controller.mDatabase.child("rooms").child(MainActivity.myRoom).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("(participants) raw data: " + snapshot.getValue());

                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                DataSnapshot tmp;

                while (iterator.hasNext()) {
                    tmp = iterator.next();
                 //   //noinspection unchecked
                    //HashMap<String, Object> dataMap = (HashMap<String, Object>) tmp.getValue();

                    String nickname = tmp.getKey();

                    participants.add(new User(nickname));
                    adapter.notifyItemInserted(participants.size()-1);
                }
                MainActivity.updateOnlineTitle();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_participants, container, false);
        recyclerView = root.findViewById(R.id.recycleView);

        setRecyclerview();

        recyclerView.addOnItemTouchListener(onClickListener());

        if (participants.isEmpty()){
            getParticipants();
        }

        return root;
    }

    private RecyclerView.OnItemTouchListener onClickListener() {
        return new RecyclerItemClickListener(getContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                System.out.println(position);
            }

            @Override public void onLongItemClick(View view, int position) {
                System.out.println(position);
            }
        });
    }

    @Override
    public void onItemClick(@NonNull View view, int position) { }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
            cleanOldData(holder);
            String nickname = participants.get(position).getNickname();
            holder.myTextView.setText(nickname);
            //holder. .setOnClickListener(holder);

        }

    private void cleanOldData(RecyclerViewAdapter.ViewHolder holder) {
        holder.myTextView.setText("");
    }
}
