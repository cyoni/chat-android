package dis.countries.chat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Time {

    public static String convertTime(long timestamp) {
        String AM_PM = "";
        if (TimeZone.getTimeZone("America") == TimeZone.getDefault())
            AM_PM = "a";
        String pattern = "MMM dd,HH:mm "+ AM_PM +",EEEE";
        DateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
        formatter.setTimeZone(TimeZone.getDefault());
        String[] local_time_and_date = formatter.format(new Date(timestamp)).split(",");
        //   String date = local_time_and_date[0];
        String time = local_time_and_date[1];
        //   String day = local_time_and_date[2];
        return time;
    }

    public static long getTimeInMS() {
        return System.currentTimeMillis();
    }
}
