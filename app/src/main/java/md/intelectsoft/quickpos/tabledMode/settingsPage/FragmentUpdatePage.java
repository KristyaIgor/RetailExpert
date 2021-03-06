package md.intelectsoft.quickpos.tabledMode.settingsPage;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;


import md.intelectsoft.quickpos.BuildConfig;
import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.POSApplication;
import md.intelectsoft.quickpos.utils.UpdateHelper;
import md.intelectsoft.quickpos.utils.UpdateInformation;

import static android.content.Context.MODE_PRIVATE;
import static md.intelectsoft.quickpos.POSApplication.SharedPrefSettings;

/**
 * Created by Igor on 13.05.2020
 */

public class FragmentUpdatePage extends Fragment implements UpdateHelper.OnUpdateCheckListener{

    ConstraintLayout cslCheckUpdate, cslSetAutoUpdate, cslLastUpdate;
    Switch autoUpdate, autoUpdateTrial;
    Context context;
    private ProgressDialog pgH;

    private SharedPreferences sharedPrefSettings;
    TextView textVersion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_update_page_v0, container, false);

        cslCheckUpdate = rootViewAdmin.findViewById(R.id.csl_download_install);
        cslSetAutoUpdate = rootViewAdmin.findViewById(R.id.csl_auto_check_updates);
        cslLastUpdate = rootViewAdmin.findViewById(R.id.csl_last_update);
        autoUpdate = rootViewAdmin.findViewById(R.id.switch_auto_update);
        autoUpdateTrial = rootViewAdmin.findViewById(R.id.switch_auto_update_trial_version);
        textVersion = rootViewAdmin.findViewById(R.id.textView_current_version);

        context = getContext();
        pgH = new ProgressDialog(context);
        sharedPrefSettings = context.getSharedPreferences(SharedPrefSettings,MODE_PRIVATE);

        boolean autoUpTrial = sharedPrefSettings.getBoolean("autoUpdateTrial",false);
        boolean autoUp = sharedPrefSettings.getBoolean("autoUpdate",false);

        autoUpdateTrial.setChecked(autoUpTrial);
        autoUpdate.setChecked(autoUp);


        cslCheckUpdate.setOnClickListener(view -> {
            pgH.setMessage("loading ...");
            pgH.setIndeterminate(true);
            pgH.show();
            POSApplication.getApplication().checkUpdates();
            UpdateHelper.with(context).onUpdateCheck(this).check();
        });

        cslSetAutoUpdate.setOnClickListener(view -> {
            if(autoUpdate.isChecked()){
                autoUpdate.setChecked(false);
            }
            else {
                autoUpdate.setChecked(true);
                autoUpdateTrial.setChecked(false);
            }
        });

        autoUpdate.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked){
                sharedPrefSettings.edit().putBoolean("autoUpdate",true).apply();
                autoUpdateTrial.setChecked(false);
            }
            else{
                sharedPrefSettings.edit().putBoolean("autoUpdate",false).apply();
            }
        });

        autoUpdateTrial.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked){
                sharedPrefSettings.edit().putBoolean("autoUpdateTrial",true).apply();
                autoUpdate.setChecked(false);
            }
            else{
                sharedPrefSettings.edit().putBoolean("autoUpdateTrial",false).apply();
            }
        });

        textVersion.setText("SalesEPOS version: " + getAppVersion(context));


        return rootViewAdmin;
    }

    private String getAppVersion(Context context){
        String result = "";
        try{
            result = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
            result = result.replaceAll("[a-zA-Z] |-","");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onUpdateCheckListener(UpdateInformation information) {
        if (information.isUpdateTrial() && !information.getNewVersionTrial().equals(information.getCurrentVersion())){
            pgH.dismiss();
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(context)
                    .setTitle("New version " + information.getNewVersionTrial() + " available")
                    .setMessage("Please update to new version to continue use.Current version: " + information.getCurrentVersion())
                    .setPositiveButton("UPDATE",(dialogInterface, i) -> {
                        pgH.setMessage("download new trial version...");
                        pgH.setIndeterminate(true);
                        pgH.show();
                        downloadAndInstallApk(information.getUrlTrial());
                    })
                    .setNegativeButton("No,thanks", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .create();
            alertDialog.show();
        }
        else{
            pgH.dismiss();
            Toast.makeText(context, "Don't exist preview updates!", Toast.LENGTH_SHORT).show();
        }
    }
    private void downloadAndInstallApk(String url){
        //get destination to update file and set Uri
        //TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
        //aplication with existing package from there. So for me, alternative solution is Download directory in external storage. If there is better

        String destination = Environment.getExternalStorageDirectory()+ "/IntelectSoft";
        String fileName = "/cash_trial.apk";
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(destination);
        if (file.exists())
            //file.delete() - test this, I think sometimes it doesnt work
            file.delete();

        //set download manager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Download trial version...");
        request.setTitle("Android cash");

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                pgH.dismiss();
                File file = new File(Environment.getExternalStorageDirectory()+ "/IntelectSoft","/cash_trial.apk"); // mention apk file path here

                Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider",file);
                if(file.exists()){
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(uri, "application/vnd.android.package-archive");
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(install);
                }
                context.unregisterReceiver(this);
               //TODO finish app

            }
        };
        //register receiver for when .apk download is compete
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}
