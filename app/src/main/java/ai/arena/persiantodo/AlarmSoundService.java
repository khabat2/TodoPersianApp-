package ai.arena.persiantodo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

public class AlarmSoundService extends Service {
    private static final String CHANNEL = "task_alarm_channel";
    private static final String STOP = "STOP_ALARM";
    private MediaPlayer player;

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && STOP.equals(intent.getAction())) { stopSelf(); return START_NOT_STICKY; }
        long id = intent == null ? -1 : intent.getLongExtra("taskId", -1);
        Task task = TaskStore.find(this, id);
        createChannel();
        startForeground((int) (id == -1 ? 1001 : id), notification(task));
        play(task);
        return START_NOT_STICKY;
    }

    private Notification notification(Task task) {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(this, 1, open, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Intent stop = new Intent(this, AlarmSoundService.class); stop.setAction(STOP);
        PendingIntent stopPi = PendingIntent.getService(this, 2, stop, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
        b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("یادآور کار")
                .setContentText(task == null ? "وقت انجام کار رسیده است" : task.title)
                .setContentIntent(openPi)
                .setOngoing(true)
                .setAutoCancel(false)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "توقف", stopPi);
        return b.build();
    }

    private void play(Task task) {
        try {
            Uri uri = null;
            if (task != null && task.audioUri != null && !task.audioUri.isEmpty()) uri = Uri.parse(task.audioUri);
            if (uri == null) uri = Settings.System.DEFAULT_ALARM_ALERT_URI;
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
            player.setDataSource(this, uri);
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (Exception ignored) {}
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "یادآور کارها", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("آلارم یادآور کارهای روزمره");
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
    }

    @Override public void onDestroy() { if (player != null) { try { player.stop(); player.release(); } catch (Exception ignored) {} } super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
