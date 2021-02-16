package com.example.mybluetoothcon;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MyViewHolder>{

    private List<MsgModel> msgList;
    public  MsgAdapter(){
        msgList = new ArrayList<>();

    }
    public void addMessage (String text , boolean isMine){
        MsgModel msg = new MsgModel(text , isMine);
        msgList.add(msg);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_row,parent,false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MsgModel msg = msgList.get(position);
        holder.tv.setText(msg.getText());
        RelativeLayout.LayoutParams parmas = (RelativeLayout.LayoutParams) holder.tv.getLayoutParams();
        if(msg.isMine()){
            holder.tv.setBackgroundResource(R.drawable.out_bg);
            parmas.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }else {
            holder.tv.setBackgroundResource(R.drawable.in_bg);
            parmas.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }
        holder.tv.setLayoutParams(parmas);

    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv ;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv =(TextView) itemView.findViewById(R.id.text);
        }
    }
}
