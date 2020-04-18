package com.example.filemanager;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class Adapter extends BaseAdapter {
    private ArrayList<String> data = new ArrayList<>();
    private  boolean[] selection;

    public void setData(ArrayList<String> data){
        if(data!=null){
            this.data.clear();
            if(data.size()>0){
                this.data.addAll(data);
            }
            notifyDataSetChanged();
        }
    }
    public void setSelection(boolean[] selection){
        if(selection!=null){
            this.selection = new boolean[selection.length];
            for(int i=0;i<selection.length;i++){
                this.selection[i]= selection[i];
            }
            notifyDataSetChanged();
        }
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.txtitem)));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        final String item = getItem(position);
        holder.info.setText(item.substring(item.lastIndexOf('/') + 1));
        Log.e("SARTHSK","here"+selection);
        if (selection != null) {
            if (selection[position]) {
                Log.e("SARTHAK","this works");
                holder.info.setBackgroundColor(Color.YELLOW);
            }

            else{
                Log.e("SARTHAK","pos is"+position);
                holder.info.setBackgroundColor(Color.WHITE);
            }
//            boolean leastoneselected = false;
//            for (int i = 0; i < selection.length; i++) {
//                if (selection[i]) {
//                    leastoneselected = true;
//                    break;
//                }
//            }
//            if (leastoneselected) {
//               convertView.findViewById(R.id.btnbar).setVisibility(View.VISIBLE);
//            } else {
//                convertView.findViewById(R.id.btnbar).setVisibility(View.GONE);
//            }
        }
            return convertView;
        }


}
