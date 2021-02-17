package md.intelectsoft.quickpos.verifone.Utilities;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import md.intelectsoft.quickpos.R;

public class ToastUtil {
    private static final long TOAST_LAST_THRESHOLD = 2000;
    private static long previous = 0;
    private static Toast toast;
    private static Context context;
    private static TextView tipTv;

    private String toastMessage;

    public static void init(Context ctx) {
        context = ctx;
    }

    public static void toastOnUiThread(Activity activityContext, final String message) {
        toastOnUiThread(activityContext, message, Toast.LENGTH_SHORT, 0, 30);
    }

    public static void toastOnUiThread(Activity activityContext, final String message, final int duration, final int xOffset, final int yOffset) {
        if (activityContext == null) {
            toast(message, duration, xOffset, yOffset);
        } else {
            activityContext.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    toast(message, duration, xOffset, yOffset);
                }
            });
        }
    }

    public static void toastOnUiThread(Activity activityContext, final String message, final int duration) {
        if (activityContext == null) {
            toast(message, duration);
        } else {
            activityContext.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    toast(message, duration);
                }
            });
        }
    }

    public static void toast(String message) {
        toast(message, Toast.LENGTH_LONG);
    }

    public static void toast(int id) {
        toast(id, Toast.LENGTH_LONG);
    }

    public static void toast(int id, int duration) {
        String message = context.getString(id);
        toast(message, duration);
    }

    public static void toast(String text, int duration) {
        long now = System.currentTimeMillis();
        if (now - previous < TOAST_LAST_THRESHOLD) {
            tipTv.setText(text);
            toast.show();
        } else {
            toast = new Toast(context);
            View view = LayoutInflater.from(context).inflate(R.layout.toast_view, null);
            tipTv = (TextView) view.findViewById(R.id.toast_textView_tip);
            tipTv.setText(text);
            toast.setDuration(duration);
            toast.setView(view);
            toast.show();
        }
        previous = now;
    }

    public static void toast(String text, int duration, int xOffset, int yOffset) {
        long now = System.currentTimeMillis();
        if (now - previous < TOAST_LAST_THRESHOLD) {
            tipTv.setText(text);
            toast.show();
        } else {
            toast = new Toast(context);
            View view = LayoutInflater.from(context).inflate(
                    R.layout.toast_view, null);
            tipTv = (TextView) view.findViewById(R.id.toast_textView_tip);
            tipTv.setText(text);
            toast.setDuration(duration);
            toast.setView(view);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, xOffset, yOffset);
            toast.show();
        }
        previous = now;
    }

    public static void cancel() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
