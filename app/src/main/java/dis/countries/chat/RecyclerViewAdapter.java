package dis.countries.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Item> mData;
    private LayoutInflater mInflater;
    private ItemClickListener msgItemListener;
    private int layout;

    // data is passed into the constructor
    public RecyclerViewAdapter(Context context, int layout, List<Item> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.layout = layout;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
         if (msgItemListener != null)
            msgItemListener.onBinding(holder, position);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView myTextView;
        public ImageView status;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.message);
            status = itemView.findViewById(R.id.status);
//            status.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (msgItemListener != null)
                msgItemListener.onItemDeliveryStatusClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Item getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(Object itemClickListener) {
        this.msgItemListener = (ItemClickListener) itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemDeliveryStatusClick(View view, int position);
        void onBinding(@NonNull ViewHolder holder, int position);
    }
}