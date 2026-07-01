package ai.arena.persiantodo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmScheduler {
    public static void schedule(Context c, Task task) {
        if (task.dueAt <= System.currentTimeMillis() || task.done) return;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = pending(c, task.id);
        Intent fill = new Intent(c, AlarmReceiver.class);
        fill.putExtra("taskId", task.id);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
        pi = PendingIntent.getBroadcast(c, (int) task.id, fill, flags);
        try {
            if (Build.VERSION.SDK_INT >= 23) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.dueAt, pi);
            else am.setExact(AlarmManager.RTC_WAKEUP, task.dueAt, pi);
        } catch (Exception e) {
            am.set(AlarmManager.RTC_WAKEUP, task.dueAt, pi);
        }
    }

    public static void cancel(Context c, long id) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pending(c, id));
    }

    private static PendingIntent pending(Context c, long id) {
        Intent i = new Intent(c, AlarmReceiver.class);
        i.putExtra("taskId", id);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
        return PendingIntent.getBroadcast(c, (int) id, i, flags);
    }
}
