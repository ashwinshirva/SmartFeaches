package smartfeaches.example.com.smartfeaches;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

public class SmartClickService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        //Create a broadcast receiver to detect screen on event
        final BroadcastReceiver mReceiver = new SmartClickReceiver();
        registerReceiver(mReceiver, filter);

        return super.onStartCommand(intent, flags, startId);
    }
}
