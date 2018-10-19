package com.tencent.qcloud.costransferpractice.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qcloud.costransferpractice.MainActivity;
import com.tencent.qcloud.costransferpractice.R;

import java.util.List;
import java.util.Map;

/**
 * Created by rickenwang on 2018/10/19.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class LoginFragment extends Fragment implements LoginContract.View {

    private View contentView;

    private EditText appid;
    private EditText url;
    private EditText secretId;
    private EditText secretKey;

    private TextView urlName;
    private TextView secretIdName;
    private TextView secretKeyName;

    private CheckBox signType;
    private boolean isTemporary;

    private Button confirm;

    private LoginContract.Presenter presenter;

    String appidText;
    String signUrlText;
    String secretIdText;
    String secretKeyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        contentView = inflater.inflate(R.layout.fragment_login, container, false);
        initContentView(contentView);
        presenter = new LoginPresenter(getContext(),this);
        isTemporary = true;

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    private void initContentView(View contentView) {

        appid = contentView.findViewById(R.id.appid);
        url = contentView.findViewById(R.id.sign_url);
        secretId = contentView.findViewById(R.id.secret_id);
        secretKey = contentView.findViewById(R.id.secret_key);

        urlName = contentView.findViewById(R.id.url_name);
        secretIdName = contentView.findViewById(R.id.secret_id_name);
        secretKeyName = contentView.findViewById(R.id.secret_key_name);

        signType = contentView.findViewById(R.id.sign_type);

        confirm = contentView.findViewById(R.id.confirm);

        signType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                refreshLoginMode(!isChecked);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (configComplete()) {

                    if (isTemporary) {
                        presenter.confirmWithTemporaryKey(appidText, signUrlText);
                    } else {
                        presenter.confirmWithForeverKey(appidText, secretIdText, secretKeyText);
                    }

                } else {
                    toastMessage("请先完成配置信息");
                }
            }
        });
    }

    @Override
    public void refreshLoginMode(boolean isTemporary) {

        this.isTemporary = isTemporary;

        if (isTemporary) {
            url.setVisibility(View.VISIBLE);
            urlName.setVisibility(View.VISIBLE);

            secretId.setVisibility(View.GONE);
            secretIdName.setVisibility(View.GONE);
            secretKey.setVisibility(View.GONE);
            secretKeyName.setVisibility(View.GONE);
        } else {

            url.setVisibility(View.GONE);
            urlName.setVisibility(View.GONE);

            secretId.setVisibility(View.VISIBLE);
            secretIdName.setVisibility(View.VISIBLE);
            secretKey.setVisibility(View.VISIBLE);
            secretKeyName.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void toastMessage(final String message) {

        contentView.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void setLoading(boolean loading) {

    }

    @Override
    public void loginSuccess(Map<String, List<String>> regionAndBuckets) {

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.transferFragment(regionAndBuckets);
    }

    @Override
    public void config(String appid, String signUrl, String secretId, String secretKey) {

        this.appid.setText(appid);
        this.url.setText(signUrl);
        this.secretId.setText(secretId);
        this.secretKey.setText(secretKey);
    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {

        this.presenter = presenter;
    }

    private boolean configComplete() {

        appidText = appid.getText().toString();
        signUrlText = url.getText().toString();
        secretIdText = secretId.getText().toString();
        secretKeyText = secretKey.getText().toString();



        if (isTemporary) {
            return !TextUtils.isEmpty(appidText)
                    && !TextUtils.isEmpty(signUrlText);
        } else {

            return !TextUtils.isEmpty(appidText)
                    && !TextUtils.isEmpty(secretIdText)
                    && !TextUtils.isEmpty(secretKeyText);
        }
    }
}
