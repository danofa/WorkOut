package org.damcode.workout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


@SuppressWarnings("SpellCheckingInspection")

public class MainActivity extends Activity {

    ListView lv;
    ArrayList<HashMap> listdata = new ArrayList<HashMap>();
    MyActionAdapter myAdapter;
    Context context;
    WorkoutDBAO workoutDBAO;
    Intent countDownService;

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "i got paused!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "i got destroyed!");
        stopService(countDownService);
    }

    @Override
    protected void onResume() {
        super.onResume();
        myAdapter.notifyDataSetChanged();
        Log.d("MainActivity", "i got resumed!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("MainActivity", "i got created!");

        countDownService = new Intent(MainActivity.this, WorkoutCountdownService.class);
        startService(countDownService);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        myAdapter = new MyActionAdapter(this, listdata);
        workoutDBAO = new WorkoutDBAO(this);

        lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(myAdapter);

        lv.setOnItemClickListener(new ListViewItemClickHandler());
        lv.setOnItemLongClickListener(new ListViewItemLongClickHandler(workoutDBAO));

        ArrayList<HashMap> workouts = workoutDBAO.getWorkouts();
        if (workouts != null)
            myAdapter.addAll(workouts);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_add:
                actionAdd();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void actionAdd() {
        final EditText input = new EditText(this);
        input.setMaxLines(1);
        input.setText(R.string.new_workout);
        input.selectAll();

        new AlertDialog.Builder(this)
                .setTitle(R.string.action_add)
                .setMessage(R.string.workout_enternewname)
                .setView(input)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = input.getText().toString();
                        long rowId = workoutDBAO.addWorkout(name);
                        HashMap rowVals = new HashMap();
                        rowVals.put("name", name);
                        rowVals.put("id", rowId);
                        myAdapter.add(rowVals);
                    }
                }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();


    }


    class ListViewItemClickHandler implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
            Intent intent = new Intent(view.getContext(), WorkoutSessionActivity.class);
            intent.putExtra("WORKOUT_ID", (Long) view.getTag());

            ((MainActivity) parent.getContext()).startActivityForResult(intent, 1);
        }
    }

    class MyActionAdapter extends ArrayAdapter<HashMap> {

        private Context context;
        private ArrayList<HashMap> values;

        public MyActionAdapter(Context context, ArrayList<HashMap> values) {
            super(context, R.layout.list_row, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int i, View view, ViewGroup parent) {
            HashMap rowValues = values.get(i);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_row, parent, false);
            TextView tv = (TextView) rowView.findViewById(R.id.mainText);
            TextView workoutTime = (TextView) rowView.findViewById(R.id.workout_time);

            tv.setText((String) rowValues.get("name"));
            Long rowId = (Long) rowValues.get("id");
            rowView.setTag(rowId);


            workoutTime.setText(StaticUtils.stringifyWorkoutTime(workoutDBAO.getWorkoutTotalTime(rowId)));
            return rowView;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }
    }



}