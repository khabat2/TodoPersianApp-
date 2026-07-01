package ai.arena.persiantodo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra("taskId", -1);
        Intent s = new Intent(context, AlarmSoundService.class);
        s.putExtra("taskId", taskId);
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(s); else context.startService(s);
    }
}
