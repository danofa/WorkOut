package org.damcode.workout;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class WorkoutSessionActivity extends Activity {

    public static final int BEEP_TYPE_REST = 1;
    public static final int BEEP_TYPE_ACTION = 0;

    int workoutId;
    int totalTime;
    boolean isStarted = false;

    ToggleButton startStopToggle, continuousRunToggle;
    ImageButton addStep;
    TextView timeleftView;

    Messenger serviceMessenger;
    SessionItemAdapter sessionAdapter;
    WorkoutDBAO workoutDBAO;

    ArrayList<Map> sessionListValues = new ArrayList<Map>();
    Messenger messenger = new Messenger(new IncomingMessageHandler());
    CountdownServiceConn myServiceConn = new CountdownServiceConn();

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("Session onPause fired", "ON PAUSE");

        try {
            Message msg = Message.obtain(null, WorkoutCountdownService.MSG_UNREGISTER_CLIENT);
            msg.replyTo = messenger;
            serviceMessenger.send(msg);
            unbindService(myServiceConn);
        } catch (Exception e) {
            e.printStackTrace(); // failed to unbind??
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        sessionListValues.clear();
        sessionAdapter.clear();

        sessionAdapter = null;
        serviceMessenger = null;
        workoutDBAO = null;
        myServiceConn = null;

        Log.e("Session Activity", "Got destroyed!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onRESUME fired", "ON RESUME");

        bindService(new Intent(this, WorkoutCountdownService.class), myServiceConn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("onCreate Fired", "ON CREATE");
        setContentView(R.layout.layout_workout_item);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(false);


        sessionAdapter = new SessionItemAdapter(this, sessionListValues);

        workoutId = getIntent().getIntExtra("WORKOUT_ID", -1);
        workoutDBAO = new WorkoutDBAO(this);
        timeleftView = (TextView) findViewById(R.id.timeLeft);

        ArrayList<HashMap> dbSessions = workoutDBAO.getSessions(workoutId);
        if (dbSessions != null) {
            sessionAdapter.addAll(dbSessions);
        }

        setTotalTime();

        startStopToggle = (ToggleButton) findViewById(R.id.action_runstop);
        continuousRunToggle = (ToggleButton) findViewById(R.id.action_once_cont);

        ListView sessionLv = (ListView) findViewById(R.id.session_list);
        sessionLv.setAdapter(sessionAdapter);

        addStep = (ImageButton) findViewById(R.id.action_add_timestep);
    }

    private void setTotalTime() {
        totalTime = 0;
        for (Map h : sessionAdapter.values) {
            totalTime += (Integer) h.get("time");
        }
        isStarted = false;
        timeleftView.setText(StaticUtils.stringifyWorkoutTime(totalTime));

    }

    public void startStopToggleClicked(View view) {

        boolean on = ((ToggleButton) view).isChecked();
        addStep.setEnabled(!on); // prevent adding session step while session is running

        continueRunClicked(continuousRunToggle);

        Log.d("TOGGLE CHANGE", String.valueOf(on) + " isStarted: " + String.valueOf(isStarted));

        if(totalTime == 0){
            return;
        }

        if (on) {
            Message msg;
            if (isStarted) {
                msg = Message.obtain(null, WorkoutCountdownService.MSG_RESUME_TIMER);
            } else {
                msg = Message.obtain(null, WorkoutCountdownService.MSG_START_TIMER);
                isStarted = true;
            }

            Bundle b = new Bundle();
            b.putInt("time", totalTime);
            b.putSerializable("beepqueue", new ArrayDeque(workoutDBAO.getBeepQueue(workoutId)));
            msg.setData(b);
            msg.replyTo = messenger;
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                // failed to send message
            }

        } else {
            Message msg = Message.obtain(null, WorkoutCountdownService.MSG_PAUSE_TIMER);
            msg.replyTo = messenger;
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                // failed to send message
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workout_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void addTimeStep(View view) {

        View test = findViewById(R.id.timeStepAdd);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View newStepView = mInflater.inflate(R.layout.add_timestep, null);
        final NumberPicker npm = (NumberPicker) newStepView.findViewById(R.id.numberPickerMin);
        final NumberPicker nps = (NumberPicker) newStepView.findViewById(R.id.numberPickerSec);
        npm.setMinValue(0);
        nps.setMinValue(0);
        npm.setMaxValue(60);
        nps.setMaxValue(59);
        final Spinner sp = (Spinner) newStepView.findViewById(R.id.sessionTypeSpinner);

        new AlertDialog.Builder(this)
                .setTitle(R.string.action_add_step)
                .setView(newStepView)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int time = (npm.getValue() * 60) + nps.getValue();
                        int beepType = sp.getSelectedItemPosition();
                        Log.d("WorkoutSession","Add session step: " + time +", "+beepType);

                        int id = workoutDBAO.addSession(workoutId, time, beepType);
                        Log.d("row id from session", Integer.toString(id));
                        Map newRow = new HashMap();
                        newRow.put("time", time);
                        newRow.put("id", id);
                        newRow.put("type", beepType);
                        sessionAdapter.add(newRow);

                        setTotalTime();



                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show(); //
    }

    public void continueRunClicked(View view) {
        try {
            int val = ((ToggleButton) view).isChecked() ? 1 : 0;
            serviceMessenger.send(Message.obtain(null, WorkoutCountdownService.MSG_TOGGLE_CONT, val, -1));
        } catch (RemoteException e) {
            // message send failed
        }
    }


    class SessionItemAdapter extends ArrayAdapter<Map> {

        ArrayList<Map> values;
        Context context;

        SessionItemAdapter(Context context, ArrayList<Map> values) {
            super(context, R.layout.session_list_layout_row, values);
            this.values = values;
            this.context = context;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.session_list_layout_row, parent, false);

            TextView tv = (TextView) rowView.findViewById(R.id.stepTime);
            String time = StaticUtils.stringifyWorkoutTime((Integer) values.get(position).get("time"));
            final int rowId = (Integer) values.get(position).get("id");
            int beepStringId = getResources().getIdentifier("session_beep_type_" + values.get(position).get("type"),"string",getPackageName());

            rowView.setTag(rowId);

            tv.setText(time + " - " + getResources().getString(beepStringId));

            ImageButton delSession = (ImageButton) rowView.findViewById(R.id.session_item_del);
            delSession.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!startStopToggle.isChecked()) {
                        workoutDBAO.removeSession(rowId);
                        sessionAdapter.remove(values.get(position));
                        setTotalTime();
                    }
                }
            });

            return rowView;
        }
    }

    private class IncomingMessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            try {

                switch (msg.what) {
                    case WorkoutCountdownService.MSG_RESUME_TIMER:
                        Log.d("Workout session", "got resume timer");
                        startStopToggle.setChecked(true);
                        addStep.setEnabled(false);
                        isStarted = true;

                        serviceMessenger.send(Message.obtain(null, WorkoutCountdownService.MSG_RESUME_TIMER));
                        break;

                    case WorkoutCountdownService.MSG_TIME_REMAINING:
                        timeleftView.setText(StaticUtils.stringifyWorkoutTime(msg.getData().getInt("remaining")));
                        break;

                    case WorkoutCountdownService.MSG_TIMER_FINISHED:
                        if (startStopToggle.isChecked()) {
                            addStep.setEnabled(true);
                            startStopToggle.setChecked(false);
                            isStarted = false;
                        }

                        setTotalTime();
                        break;

                    case WorkoutCountdownService.MSG_TOGGLE_CONT:
                        if(msg.arg1 == 1 && !continuousRunToggle.isChecked()){
                            continuousRunToggle.toggle();
                        }
                    break;

                    default:
                        super.handleMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class CountdownServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder target) {

            Log.d("BIND SERVICE", "connected");

            serviceMessenger = new Messenger(target);
            try {
                Message msg = Message.obtain(null, WorkoutCountdownService.MSG_REGISTER_CLIENT);
                Bundle b = new Bundle();
                b.putInt("workoutid", workoutId);
                msg.setData(b);
                msg.replyTo = messenger;
                serviceMessenger.send(msg);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }



        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("BIND SERVICE", "disconnected");
        }

    }

}