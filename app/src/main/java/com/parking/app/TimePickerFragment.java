/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    int hour, min;

    public interface TimePickerListener {
        void onTimeSet(TimePickerFragment timePickerFragment);
    }

    TimePickerListener timePickerListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            timePickerListener = (TimePickerListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + "must implement NoticeDialogListener");
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values of the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        this.hour = hourOfDay;
        this.min = minute;
        timePickerListener.onTimeSet(TimePickerFragment.this);
    }

    public int getHour() {
        return hour;
    }

    public int getMin() {
        return min;
    }
}
