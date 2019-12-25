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
import com.jwhh.notekeeper.dataModels.CourseInfo;
import com.jwhh.notekeeper.dataModels.NoteInfo;
import com.jwhh.notekeeper.ui.NoteActivity;

import java.util.List;

public class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.CourseViewHolder> {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final List<CourseInfo> mCourses;

    public CourseRecyclerAdapter(Context context, List<CourseInfo> notes) {
        mContext = context;
        mCourses = notes;
        mLayoutInflater = LayoutInflater.from(mContext);
    }


    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = mLayoutInflater.inflate(R.layout.item_course_list, parent,false);

        return new CourseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseInfo course = mCourses.get(position);
        holder.mCourse.setText(course.getTitle());
    }

    @Override
    public int getItemCount() {
        return mCourses.size();
    }


    public class CourseViewHolder extends RecyclerView.ViewHolder {

        final AppCompatTextView mCourse;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);

            mCourse = itemView.findViewById(R.id.text_course);
        }
    }
}
