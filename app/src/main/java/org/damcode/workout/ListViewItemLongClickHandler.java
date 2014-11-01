package org.damcode.workout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

/**
 * Created by dm on 2/10/2014.
 */
class ListViewItemLongClickHandler implements AdapterView.OnItemLongClickListener {

    WorkoutDBAO workoutDBAO;

    ListViewItemLongClickHandler(WorkoutDBAO workoutDBAO) {
        this.workoutDBAO = workoutDBAO;
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, final View view,final int i, long l) {
        new AlertDialog.Builder(parent.getContext())
                .setTitle(R.string.remove_workout)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ((ArrayAdapter)parent.getAdapter()).remove(parent.getAdapter().getItem(i));

                        workoutDBAO.removeWorkout((Integer) view.getTag());
                    }
                }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
        return true;
    }
}
