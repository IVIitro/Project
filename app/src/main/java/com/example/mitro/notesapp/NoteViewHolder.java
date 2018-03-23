package com.example.mitro.notesapp;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by mitro on 01.03.2018.
 */

public class NoteViewHolder extends RecyclerView.ViewHolder {

        View mView;

        TextView textTitle, textTime;
    ImageView imageView;
        CardView noteCard;

        public NoteViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            textTitle = mView.findViewById(R.id.note_title);
            textTime = mView.findViewById(R.id.note_time);
            imageView = mView.findViewById(R.id.imageView);
            noteCard = mView.findViewById(R.id.note_card);

        }

        public void setNoteTitle(String title) {
            textTitle.setText(title);
        }
        public void setNoteType(String type) {
            if(type=="null"){
                imageView.setVisibility(View.GONE);
            }else{
                Picasso.get().load(type).fit().centerCrop().into(imageView);}
    }
        public void setNoteTime(String time) {
            textTime.setText(time);
        }

}

