package com.nacho.padtranslate.ui;

import android.content.pm.PackageInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Element versionElement = new Element();
        String version = "unknown";
        String playstore = "com.nacho.padtranslate";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            playstore = pInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        versionElement.setTitle("Version " + version);


        View aboutPage = new AboutPage(this)
            .isRTL(false)
            .setDescription(getString(R.string.action_about_description))
            .setImage(R.mipmap.ic_launcher)
            .addItem(versionElement)
            .addGroup("Connect with us")
            .addEmail("padtranslate@gmail.com")
            .addPlayStore(playstore)
//            .addGitHub("")
            .create();

        setContentView(aboutPage);
    }
}
