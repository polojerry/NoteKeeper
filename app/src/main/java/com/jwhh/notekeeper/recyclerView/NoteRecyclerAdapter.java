package com.jwhh.notekeeper.recyclerView;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.dataModels.NoteInfo;
import com.jwhh.notekeeper.ui.NoteActivity;

import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.NoteViewHolder> {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final List<NoteInfo> mNotes;

    public NoteRecyclerAdapter(Context context, List<NoteInfo> notes) {
        mContext = context;
        mNotes = notes;
        mLayoutInflater = LayoutInflater.from(mContext);
    }


    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = mLayoutInflater.inflate(R.layout.item_note_list, parent,false);

        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteInfo note = mNotes.get(position);
        holder.mCourseTitle.setText(note.getTitle());
        holder.mNoteTittle.setText(note.getText());
        holder.notePosition = position;
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }


    public class NoteViewHolder extends RecyclerView.ViewHolder {

        final AppCompatTextView mCourseTitle;
        final AppCompatTextView mNoteTittle;

        int notePosition;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            mCourseTitle = itemView.findViewById(R.id.text_course_tittle);
            mNoteTittle = itemView.findViewById(R.id.text_note_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent startNoteActivity = new Intent(mContext, NoteActivity.class);
                    startNoteActivity.putExtra(NoteActivity.NOTE_POSITION, notePosition);
                    mContext.startActivity(startNoteActivity);

                }
            });
        }
    }
}
