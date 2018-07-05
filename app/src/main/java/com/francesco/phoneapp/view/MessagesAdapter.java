package com.francesco.phoneapp.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.francesco.phoneapp.R;
import com.francesco.phoneapp.viewmodel.MainActivityVM;
import com.francesco.phoneapp.viewmodel.Message;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MyViewHolder> {
    private final MainActivityVM mainActivityVM;

    public MessagesAdapter(MainActivityVM mainActivityVM) {
        this.mainActivityVM = mainActivityVM;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView key;
        private final TextView text;
        private final ImageView seen;

        MyViewHolder(View itemView) {
            super(itemView);

            key = itemView.findViewById(R.id.key);
            text = itemView.findViewById(R.id.text);
            seen = itemView.findViewById(R.id.seen);
        }
    }

    @NonNull
    @Override
    public MessagesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.MyViewHolder holder, int position) {
        String key = mainActivityVM.getKeyFromMessagesMapByIndex(position);
        Message message = mainActivityVM.getMessageFromMessagesMapByIndex(position);

        holder.key.setText(key);
        holder.text.setText(message.getText());
        holder.seen.setImageResource(message.isSeen() ? R.drawable.true_c : R.drawable.true_bw);
    }

    @Override
    public int getItemCount() {
        return mainActivityVM.getMessagesMapSize();
    }
}
