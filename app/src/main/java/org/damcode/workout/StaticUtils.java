package org.damcode.workout;

/**
 * Created by dm on 2/10/2014.
 */
public class StaticUtils {

    public static String stringifyWorkoutTime(long time){
        int hours = (int) (time / 60) / 60;
        int mins = (int) (time / 60) % 60;
        int secs = (int) (time % 60);

        if(hours == 0 && mins == 0 && secs == 0){
            return new String("--:--:--");
        }
        return new String(hours + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
    }


}
