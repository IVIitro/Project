package com.example.mitro.notesapp;

import android.content.Context;

/**
 * Created by mitro on 03.03.2018.
 */

public class GetTime {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTime(long time, Context ctx){

        if (time < 1000000000000L) {

            time *= 1000;

        }

        long now = System.currentTimeMillis();

        if (time > now || time <= 0){

            return null;

        }

        // TODO: localize
        final long diff = now - time;

        if (diff < MINUTE_MILLIS){
            return "зараз";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "хвилину тому";
        } else if (diff < 50 * MINUTE_MILLIS){
            return (diff / MINUTE_MILLIS + " хвилин тому");
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "годину тому";
        } else if (diff < 24 * HOUR_MILLIS){
            return (diff / HOUR_MILLIS + " години тому");
        } else if (diff < 48 * HOUR_MILLIS){
            return "вчора";
        } else {
            return diff / DAY_MILLIS + " дні тому";
        }

    }
}
