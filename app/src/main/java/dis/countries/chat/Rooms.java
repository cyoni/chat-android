package dis.countries.chat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;


public class Rooms extends Fragment  implements RecyclerViewAdapter.ItemClickListener {
    private RecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<String> rooms = new ArrayList<>();

    public Rooms() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rooms, container, false);
        recyclerView = root.findViewById(R.id.recycleView);

        setRecyclerview();

        rooms.add("yoni");
        adapter.notifyDataSetChanged();

      //  Button b = root.findViewById(R.id.button2);
      /*  b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("FF");
            }
        });*/

        return root;
    }

    private void setRecyclerview() {
        adapter = new RecyclerViewAdapter(getContext(), R.layout.item_room, rooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        String nickname = rooms.get(position);
        holder.myTextView.setText(nickname);
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {
        System.out.println("Connecting to #" + position);
        connect_to_room(position);
    }
}