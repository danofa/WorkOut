package org.damcode.workout;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dm on 2/10/2014.
 */
public class WorkoutCountdownService extends Service {

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_START_TIMER = 3;
    public static final int MSG_PAUSE_TIMER = 4;
    public static final int MSG_RESUME_TIMER = 5;
    public static final int MSG_CANCEL_TIMER = 6;
    public static final int MSG_TOGGLE_CONT = 7;
    public static final int MSG_TIMER_FINISHED = 8;
    public static final int MSG_TIME_REMAINING = 9;


    private List<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    private CountDownTimer timer;
    private long totalTime = 0, remainingTime = 0;
    private long workoutId = -1;
    private ArrayDeque beepQueue;
    private boolean continuous;
    private SoundPool soundPool;
    private int sndBeep;

    public boolean isRunning = false;

    private final Messenger messenger = new Messenger(new IncomingMessageHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public void onCreate() {
        Log.d("CountdownService", "onCreate");

        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        sndBeep = soundPool.load(this, R.raw.beep, 1);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("CountdownService", "i got started!");
        return START_STICKY;
    }


    void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        totalTime = 0;
        remainingTime = 0;
        continuous = false;
        isRunning = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        cancelTimer();

        Log.d("CountdownService", "i got destroyed!");
    }


    class IncomingMessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            try {

                switch (msg.what) {
                    case MSG_REGISTER_CLIENT:

                        mClients.add(msg.replyTo);

/*
                        Log.e("Countdown handler: ", "number of clients : " + mClients.size() +
                                ", is running: " + String.valueOf(isRunning));
*/

                        if (isRunning) {
                            if (workoutId != msg.getData().getLong("workoutid")) {
                                Log.e("Countdown handler: ", "not same workout id in register");
                                cancelTimer();

                            } else {
                                Log.e("Countdown handler: ", "sending resume to client!");
                                msg.replyTo.send(Message.obtain(null, MSG_RESUME_TIMER));
                                int cont = (continuous) ? 1 : 0;
                                msg.replyTo.send(Message.obtain(null, MSG_TOGGLE_CONT, cont, 0));
                            }
                        }
                        workoutId = msg.getData().getLong("workoutid");

                        break;

                    case MSG_UNREGISTER_CLIENT:
                        mClients.remove(msg.replyTo);
                        break;

                    case MSG_START_TIMER:
                        totalTime = msg.getData().getLong("time");
                        remainingTime = totalTime;
                        beepQueue = (ArrayDeque) msg.getData().get("beepqueue");
                        startTimer();
                        break;

                    case MSG_PAUSE_TIMER:
                        timer.cancel();
                        timer = null;
                        isRunning = false;
                        break;

                    case MSG_RESUME_TIMER:
                        if (timer == null) startTimer();
                        break;

                    case MSG_CANCEL_TIMER:
                        cancelTimer();
                        break;

                    case MSG_TOGGLE_CONT:
                        continuous = msg.arg1 == 1;

                    default:
                        super.handleMessage(msg);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    private void sendMessage(Message message) {
        message.replyTo = messenger;

        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {

                mClients.get(i).send(message);

            } catch (RemoteException e) {
                Log.e("Countdown service", "Exception !");
                mClients.remove(i);
            }
        }

    }

    private void startTimer() {
        isRunning = true;
        timer = new CountDownTimer((remainingTime) * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished / 1000;
                Message msg = Message.obtain(null, MSG_TIME_REMAINING);
                Bundle b = new Bundle();
                b.putLong("remaining", remainingTime);
                msg.setData(b);
                sendMessage(msg);

                doBeep(remainingTime, totalTime);
            }

            public void onFinish() {
                timer = null;

                soundPool.play(sndBeep, 1, 1, 0, 2, 1);

                if (continuous) {
                    remainingTime = totalTime;
                    startTimer();

                } else {
                    cancelTimer();
                    sendMessage(Message.obtain(null, MSG_TIMER_FINISHED));
                }
            }
        }.start();

    }

    private void doBeep(long remainingTime, long totalTime) {

        long elapsedTime = totalTime - remainingTime;

        if ((Long) beepQueue.peek() == elapsedTime) {
            soundPool.play(sndBeep, 1, 1, 0, 1, 1);
            beepQueue.poll();
        }

    }


} //eof
