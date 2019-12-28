package com.jwhh.notekeeper.recyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.dataModels.NoteInfo;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract;
import com.jwhh.notekeeper.database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.jwhh.notekeeper.ui.NoteActivity;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.NoteViewHolder> {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private Cursor mCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteIdPos;

    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(mContext);
        populateColumnPositions();
    }

    private void populateColumnPositions() {
        if (mCursor == null){
            return;
        }

        mCourseIdPos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteIdPos = mCursor.getColumnIndex(NoteInfoEntry._ID);
    }


    public void swapCursor(Cursor cursor){

        mCursor = cursor;
        populateColumnPositions();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = mLayoutInflater.inflate(R.layout.item_note_list, parent,false);

        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        String course = mCursor.getString(mCourseIdPos);
        String noteTitle = mCursor.getString(mNoteTitlePos);
        int noteId = mCursor.getInt(mNoteIdPos);


        holder.mCourseTitle.setText(course);
        holder.mNoteTittle.setText(noteTitle);
        holder.noteId = noteId;
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();

    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {

        final AppCompatTextView mCourseTitle;
        final AppCompatTextView mNoteTittle;

        int noteId;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            mCourseTitle = itemView.findViewById(R.id.text_course_tittle);
            mNoteTittle = itemView.findViewById(R.id.text_note_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent startNoteActivity = new Intent(mContext, NoteActivity.class);
                    startNoteActivity.putExtra(NoteActivity.NOTE_ID, noteId);
                    mContext.startActivity(startNoteActivity);

                }
            });
        }
    }
}
