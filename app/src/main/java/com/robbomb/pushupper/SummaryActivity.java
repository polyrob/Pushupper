package com.robbomb.pushupper;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.CalendarView;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import hirondelle.date4j.DateTime;

public class SummaryActivity extends FragmentActivity {

    CalendarView calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        //CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
        //initializes the calendarview
        try {
            initializeCalendar();
        } catch (ParseException e) {
            e.printStackTrace();
        }




    }


    public void initializeCalendar() throws ParseException {
//        calendar = (CalendarView) findViewById(R.id.calendarView1);
//
//        // sets whether to show the week number.
//        calendar.setShowWeekNumber(false);
//
//        // sets the first day of week according to Calendar.
//        // here we set Monday as the first day of the Calendar
//        calendar.setFirstDayOfWeek(2);
//
//        //The background color for the selected week.
//        calendar.setSelectedWeekBackgroundColor(getResources().getColor(R.color.green));
//
//        //sets the color for the dates of an unfocused month.
//        calendar.setUnfocusedMonthDateColor(getResources().getColor(R.color.transparent));
//
//        //sets the color for the separator line between weeks.
//        calendar.setWeekSeparatorLineColor(getResources().getColor(R.color.transparent));
//
//        //sets the color for the vertical bar shown at the beginning and at the end of the selected date.
//        calendar.setSelectedDateVerticalBar(R.color.colorPrimary);
//
//        //sets the listener to be notified upon selected date change.
//        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
//            //show the selected date as a toast
//            @Override
//            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
//                Toast.makeText(getApplicationContext(), day + "/" + month + "/" + year, Toast.LENGTH_LONG).show();
//            }
//        });

        CaldroidFragment caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
//        t.replace(R.id.calendarView1, caldroidFragment);
        t.add(R.id.summaryLayout, caldroidFragment, "caldroid");
        t.commit();

        caldroidFragment.setBackgroundResourceForDate(R.color.caldroid_darker_gray, formatter.parse("2016-01-22"));
        caldroidFragment.setSelectedDates(cal.getTime(), formatter.parse("2016-01-15"));
        caldroidFragment.refreshView();
    }
}
