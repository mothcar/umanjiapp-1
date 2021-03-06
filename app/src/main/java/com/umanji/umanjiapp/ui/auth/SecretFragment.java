package com.umanji.umanjiapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.android.gms.maps.model.LatLng;
import com.umanji.umanjiapp.R;
import com.umanji.umanjiapp.helper.FileHelper;
import com.umanji.umanjiapp.helper.Helper;
import com.umanji.umanjiapp.model.AuthData;
import com.umanji.umanjiapp.model.ChannelData;
import com.umanji.umanjiapp.model.SuccessData;
import com.umanji.umanjiapp.ui.BaseFragment;

import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

public class SecretFragment extends BaseFragment {
    private static final String TAG = "SecretFragment";


    /****************************************************
     *  View
     ****************************************************/
    private AutoCompleteTextView mEmail;
    private EditText mPassword;
    private Button mSubmit;
    private TextView mSignIn;


    /****************************************************
     *  Etc
     ****************************************************/
    ChannelData mAddressChannel;
    private LatLng mCurrentMyPosition;


    public static SecretFragment newInstance(Bundle bundle) {
        SecretFragment fragment = new SecretFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            double latitude = getArguments().getDouble("latitude");
            double longitude = getArguments().getDouble("longitude");
            mCurrentMyPosition = new LatLng(latitude, longitude);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.activity_change_password, container, false);
    }

    @Override
    public void initWidgets(View view) {
        mEmail = (AutoCompleteTextView)view.findViewById(R.id.email);
        mPassword = (EditText)view.findViewById(R.id.password);

        mSubmit = (Button)view.findViewById(R.id.submit);
        mSubmit.setOnClickListener(this);

        mSignIn = (TextView)view.findViewById(R.id.signup);
        mSignIn.setOnClickListener(this);


        String email = FileHelper.getString(mActivity, "pre_email");
        if(!TextUtils.isEmpty(email)) {
            mEmail.setText(email);
        }

        if(mCurrentMyPosition != null) {
            loadData();
        }
    }

    @Override
    public void loadData() {
        try {
            JSONObject params = new JSONObject();
            params.put("latitude", mCurrentMyPosition.latitude);
            params.put("longitude", mCurrentMyPosition.longitude);

            mApi.call(api_channels_getByPoint, params, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    mAddressChannel = new ChannelData(json);
                }
            });
        } catch(JSONException e) {
            Log.e(TAG, "error " + e.toString());
        }
    }

    @Override
    public void updateView() {

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:
                boolean isValid = isValidLoginForm(mEmail, mPassword);
                if(isValid) {
                    signin();
                }
                break;

            case R.id.signup:
                Intent signIn = new Intent(mActivity, SignupActivity.class);

                Bundle bundle = new Bundle();

                if(mCurrentMyPosition != null) {
                    bundle.putDouble("latitude", mCurrentMyPosition.latitude);
                    bundle.putDouble("longitude", mCurrentMyPosition.longitude);
                }
                signIn.putExtra("bundle", bundle);

                startActivity(signIn);

                break;
        }
    }



    /****************************************************
     *  Event Bus
     ****************************************************/

    public void onEvent(SuccessData event){
        AuthData auth;
        switch (event.type) {
            case api_signin:
                auth = new AuthData(event.response);
                if(auth.user != null) {
                    mActivity.finish();
                    EventBus.getDefault().post(new SuccessData(EVENT_UPDATEVIEW));
                }else {
                    signup();
                }
                break;
            case api_signup:
                auth = new AuthData(event.response);
                if(auth.user != null) {
                    mActivity.finish();
                    EventBus.getDefault().post(new SuccessData(EVENT_UPDATEVIEW));
                }else {
                    //TODO: 회원가입 오류 처리
                }
                break;
        }
    }




    /****************************************************
     *  Submit Methods
     ****************************************************/

    private void signin() {
        final String fEmail     = mEmail.getText().toString();
        final String fPassword  = mPassword.getText().toString();

        try {
            JSONObject params = new JSONObject();
            params.put("email", fEmail);
            params.put("password", fPassword);

            mApi.call(api_password_update, params, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    AuthData auth = new AuthData(json);
                    if(auth.user != null) {
                        FileHelper.setString(mActivity, "pre_email", fEmail);
                        EventBus.getDefault().post(new SuccessData(api_signin, json));
                        mActivity.finish();
                    }else {
                        Toast.makeText(mActivity, "이메일과 비밀번호를 확인해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch(JSONException e) {
            Log.e(TAG, "error " + e.toString());
        }
    }

    private void signup() {
        final String fEmail     = mEmail.getText().toString();
        final String fPassword  = mPassword.getText().toString();

        try {

            JSONObject params;
            if(mAddressChannel != null) {
                params = mAddressChannel.getAddressJSONObject();
            } else {
                params = new JSONObject();
            }

            params.put("email", fEmail);
            params.put("password", fPassword);

            mApi.call(api_signup, params, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    FileHelper.setString(mActivity, "pre_email", fEmail);
                    AuthData auth = new AuthData(json);
                    if(auth.user != null && !TextUtils.isEmpty(auth.user.getId())) {
                        EventBus.getDefault().post(new SuccessData(api_signup, json));
                        mActivity.finish();
                    }else {
                        Toast.makeText(mActivity, "이미 존재하는 이메일 주소입니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch(JSONException e) {
            Log.e(TAG, "error " + e.toString());
        }
    }




    /****************************************************
     *  Private Methods
     ****************************************************/

    public boolean isValidLoginForm(AutoCompleteTextView emailInput, EditText passwordInput) {

        emailInput.setError(null);
        passwordInput.setError(null);

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !Helper.isPasswordValid(password)) {
            passwordInput.setError(mActivity.getString(R.string.sign_error_invalid_password));
            focusView = passwordInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError(mActivity.getString(R.string.error_field_required));
            focusView = emailInput;
            cancel = true;
        } else if (!Helper.isEmailValid(email)) {
            emailInput.setError(mActivity.getString(R.string.sign_error_invalid_email));
            focusView = emailInput;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }


}
