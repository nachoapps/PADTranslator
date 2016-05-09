package com.nacho.padtranslate.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;

import com.nacho.padtranslate.fulltranslate.FullTranslateDisplayService;
import com.nacho.padtranslate.monsterinfo.MonsterInfoDisplayService;

/**
 */
public class StartActivity extends AppCompatActivity {

    private static final String TAG = StartActivity.class.getSimpleName();

    private static final int REQUEST_OVERLAY_PERMISSION = 101;
    private static final int REQUEST_SCREENSHOT_PERMISSION = 102;
    private static final int REQUEST_FILE_ACCESS_PERMISSION = 103;

    private Switch translateSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.partial_base_content);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.content_frame);

        // Adding our layout to parent class frame layout.
        getLayoutInflater().inflate(R.layout.fragment_start, frameLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_title);

        translateSwitch = (Switch) findViewById(R.id.startDrawOverSwitch);
        translateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                populateWindow();
                if (!isChecked) {
                    stopService(new Intent(getApplicationContext(), WindowService.class));
                    stopService(new Intent(getApplicationContext(), MonsterInfoDisplayService.class));
                    stopService(new Intent(getApplicationContext(), FullTranslateDisplayService.class));
                } else if (missingAnyPermissions()) {
                    translateSwitch.setChecked(false);
                } else {
                    translateSwitch.setChecked(true);
                    startService(new Intent(getApplicationContext(), WindowService.class));
                    startService(new Intent(getApplicationContext(), MonsterInfoDisplayService.class));
                    startService(new Intent(getApplicationContext(), FullTranslateDisplayService.class));
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getBaseContext(), SettingsActivity.class));
                return true;
//            case R.id.action_help:
//                startActivity(new Intent(getBaseContext(), HelpActivity.class));
//                return true;
            case R.id.action_about:
                startActivity(new Intent(getBaseContext(), AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        boolean shouldTryLaunching = false;
        switch (requestCode) {
            case REQUEST_SCREENSHOT_PERMISSION:
                MainApp.setScreenshotIntentResult(resultCode, data);
                shouldTryLaunching = resultCode == Activity.RESULT_OK;
                break;
            case REQUEST_OVERLAY_PERMISSION:
                // For some reason this intent comes back with canceled always, so just check
                // for the the permission instead of the result code
                shouldTryLaunching = hasDrawOver();
                break;
        }

        if (shouldTryLaunching) {
            tryStarting();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean shouldTryLaunching = false;
        switch (requestCode) {
            case REQUEST_FILE_ACCESS_PERMISSION:
                System.out.println("in here " + grantResults[0] + " + " + grantResults[1]);
                shouldTryLaunching = grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }

        if (shouldTryLaunching) {
            tryStarting();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateWindow();
    }

    private boolean missingAnyPermissions() {
        if (!hasDrawOver()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            return true;
        }

        if (!MainApp.canTakeScreenshotsIfNecessary()) {
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mpm.createScreenCaptureIntent(), REQUEST_SCREENSHOT_PERMISSION);
            return true;
        }

        if (!hasReadAndWrite()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    }, REQUEST_FILE_ACCESS_PERMISSION);
            return true;
        }

        return false;
    }

    private boolean hasDrawOver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(StartActivity.this);
        } else {
            // earlier versions don't care
            return true;
        }
    }

    private boolean hasReadAndWrite() {
        boolean hasRead = (ContextCompat.checkSelfPermission(StartActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        boolean hasWrite = (ContextCompat.checkSelfPermission(StartActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        return hasRead && hasWrite;
    }

    private void tryStarting() {
        translateSwitch.setChecked(true);
    }


    private void populateWindow() {
        // must happen after application has shown!
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        WindowService.setWindowRect(rectangle);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
