package com.umanji.umanjiapp.ui.channel.info;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.umanji.umanjiapp.R;
import com.umanji.umanjiapp.ui.BaseActivity;


public class InfoActivity extends BaseActivity {
    private static final String TAG = "SpotActivity";

    int mEnterAnim = 0;
    int mExitAnim = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.overridePendingTransition(mEnterAnim, R.anim.move_back);
    }

    protected Fragment createFragment() {
        return InfoFragment.newInstance(getIntent().getBundleExtra("bundle"));
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.move_base, mExitAnim);
    }
}
