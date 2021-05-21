package com.example.task71;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void Create_Note(View view) {
        Intent intent = new Intent(MainActivity.this, AddActivity.class);
        startActivity(intent);
    }

    public void Show_Note(View view) {
        Intent intent = new Intent(MainActivity.this, Notes.class);
        startActivity(intent);
    }

    public static class Notes extends AppCompatActivity {

        RecyclerView recyclerView;
        Button addButton;
        MyDatabase myDatabase;
        ArrayList<String> Note;
        NewAdapter newAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_notes);

            recyclerView = findViewById(R.id.recyclerview);
            addButton = findViewById(R.id.add_button);

            addButton.setOnClickListener(v -> {
                Intent intent = new Intent(Notes.this, AddActivity.class);
                startActivity(intent);
            });

            myDatabase = new MyDatabase(Notes.this);
            Note = new ArrayList<>();
            showData();

            newAdapter = new NewAdapter(this,Note);
            recyclerView.setAdapter(newAdapter);
            LinearLayoutManager manager = new LinearLayoutManager(Notes.this);
            recyclerView.setLayoutManager(manager);

        }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == 1){
                recreate();
            }
        }

        void showData(){
            Cursor cursor = myDatabase.readAllData();
            if(cursor.getCount() == 0){

            }else{
                while (cursor.moveToNext()){
                    Note.add(cursor.getString(0));
                }

            }
        }



        void confirmDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete All?");
            builder.setMessage("Delete All?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MyDatabase myDB = new MyDatabase(Notes.this);
                    myDB.deleteAllData();
                    //Refresh Activity
                    Intent intent = new Intent(Notes.this, Notes.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.create().show();
        }


    }

    public static class MyDatabase extends SQLiteOpenHelper {

        private Context context;
        private  static final String DATABASE_NAME = "Notes.db";
        private  static final int DATABASE_VERSION = 1;

        private static final String TABLE_NAME = "mynotes";
        private static final String COLUMN_NOTE = "notes";

        MyDatabase(@Nullable Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String query = "CREATE TABLE " + TABLE_NAME +
                            " (" + COLUMN_NOTE + " TEXT);";

            db.execSQL(query);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

        void addNote(String notes){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();

            cv.put(COLUMN_NOTE, notes);
            long result = db.insert(TABLE_NAME, null, cv);
            if(result == -1){
                Toast.makeText(context,"Fail to add !", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context," new notes Added !!", Toast.LENGTH_SHORT).show();
            }

        }

        Cursor readAllData()
        {
            String query = "SELECT * FROM " + TABLE_NAME;
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = null;
            if(db != null){
                cursor = db.rawQuery(query, null);
            }
            return cursor;
        }

        void updateData(String notes){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();

            cv.put(COLUMN_NOTE, notes);

            long result = db.update(TABLE_NAME, cv, "Notes=?", new String[]{notes});
            if(result == -1){
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(context, "Updated!!!", Toast.LENGTH_SHORT).show();
            }

        }

        void delete(String notes){
            SQLiteDatabase db = this.getWritableDatabase();
            long result = db.delete(TABLE_NAME, "notes=?", new String[]{notes});
            if(result == -1){
                Toast.makeText(context, "Failed to Delete.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, "Successfully for  Delete.", Toast.LENGTH_SHORT).show();
            }
        }

        void deleteAllData(){
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + TABLE_NAME);
        }

    }

    public static class NewAdapter  extends RecyclerView.Adapter<NewAdapter.MyViewHolder> {

         Context context;
         Activity activity;
         ArrayList notess;

        NewAdapter( Context context, ArrayList notes){
            this.context = context;
            this.notess = notes;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.recycleview, parent, false);
    /*        View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycleview, parent,false);
            MyViewHolder MyVH = new MyViewHolder(itemView);*/

            return new MyViewHolder(view);
        }


        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.note.setText(String.valueOf(notess.get(position)));

            holder.note.setOnClickListener(view -> {
                Intent intent = new Intent(context, UpdateActivity.class);
                intent.putExtra("notes", String.valueOf(notess.get(position)));

                activity.startActivityForResult(intent, 1);

            });
        }

        @Override
        public int getItemCount() {
            return notess.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView note;
            LinearLayout LY;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);

                note = itemView.findViewById(R.id.content);
                LY = itemView.findViewById(R.id.myLY);

                Animation translate_anim = AnimationUtils.loadAnimation(context, R.anim.translate_anim);
                LY.setAnimation(translate_anim);

            }
        }
    }

    public static class AddActivity extends AppCompatActivity {

        EditText inputs;
        Button add_button ;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add);

            inputs = findViewById(R.id.input);
            add_button = findViewById(R.id.add_button);

            add_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyDatabase myDatabase = new MyDatabase(AddActivity.this);
                    myDatabase.addNote(inputs.getText().toString().trim());

                }
            });

        }

    }

    public static class UpdateActivity extends AppCompatActivity {

        EditText note_content;
        Button Update_btn, Delete_btn;

        String Note;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_update);

            note_content = findViewById(R.id.Note_content);
            Update_btn = findViewById(R.id.Update);
            Delete_btn = findViewById(R.id.Delete);

            getAndSetIntentData();

            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setTitle(Note);
            }

            Update_btn.setOnClickListener(view -> {

                MyDatabase myDB = new MyDatabase(UpdateActivity.this);
                Note = note_content.getText().toString().trim();
                myDB.updateData(Note);
            });
            Delete_btn.setOnClickListener(view -> confirmDialog());
        }

        void getAndSetIntentData(){
            if(getIntent().hasExtra("id") ){
                Note = getIntent().getStringExtra("Note");
                ;
                note_content.setText(Note);
                Log.d("stev", Note);
            }else{
                Toast.makeText(this, "No data.", Toast.LENGTH_SHORT).show();
            }
        }
        void confirmDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete " + Note + " ?");
            builder.setMessage("Are you want to delete " + Note + " ?");
            builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                MyDatabase myDB = new MyDatabase(UpdateActivity.this);
                myDB.delete(Note);
                finish();
            });
            builder.setNegativeButton("No", (dialogInterface, i) -> {

            });
            /*builder.create().show();*/

        }
    }
}