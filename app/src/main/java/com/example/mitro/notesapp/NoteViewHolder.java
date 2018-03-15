package com.example.mitro.notesapp;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by mitro on 01.03.2018.
 */

public class NoteViewHolder extends RecyclerView.ViewHolder {

        View mView;

        TextView textTitle, textTime, typeText;
        CardView noteCard;

        public NoteViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            textTitle = mView.findViewById(R.id.note_title);
            textTime = mView.findViewById(R.id.note_time);
            typeText = mView.findViewById(R.id.note_type);
            noteCard = mView.findViewById(R.id.note_card);

        }

        public void setNoteTitle(String title) {
            textTitle.setText(title);
        }
        public void setNoteType(String type) {
            typeText.setText(type);
    }
        public void setNoteTime(String time) {
            textTime.setText(time);
        }

}

