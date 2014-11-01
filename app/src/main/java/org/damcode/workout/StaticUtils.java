package org.damcode.workout;

/**
 * Created by dm on 2/10/2014.
 */
public class StaticUtils {

    public static String stringifyWorkoutTime(int time){
        int hours = (time / 60) / 60;
        int mins =  (time / 60) % 60;
        int secs =  (time % 60);

        if(hours == 0 && mins == 0 && secs == 0){
            return new String("--:--:--");
        }
        return new String(hours + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
    }


}
