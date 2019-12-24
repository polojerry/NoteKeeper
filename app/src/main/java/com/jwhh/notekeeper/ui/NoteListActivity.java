package com.jwhh.notekeeper.ui;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jwhh.notekeeper.R;
import com.jwhh.notekeeper.dataModels.NoteInfo;
import com.jwhh.notekeeper.database.DataManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {

    /*private ArrayAdapter<NoteInfo> mNotesAdapter;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NoteListActivity.this, NoteActivity.class));
            }
        });

        initializeDisplayNotes();
    }

    private void initializeDisplayNotes() {

        final RecyclerView notesRecycler = findViewById(R.id.list_notes);
        final LinearLayoutManager notesLayoutManager = new LinearLayoutManager(this);
        notesRecycler.setLayoutManager(notesLayoutManager);


        /*final ListView noteList = findViewById(R.id.list_notes);
        final List<NoteInfo> notes = DataManager.getInstance().getNotes();


        mNotesAdapter = new ArrayAdapter<>(
                this,android.R.layout.simple_list_item_1,notes
        );

        noteList.setAdapter(mNotesAdapter);
        noteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
                intent.putExtra(NoteActivity.NOTE_POSITION, position);
                startActivity(intent);
            }
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* mNotesAdapter.notifyDataSetChanged(); */
    }
}
