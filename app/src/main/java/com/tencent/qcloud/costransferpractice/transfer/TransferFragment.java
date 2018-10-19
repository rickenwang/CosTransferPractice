package com.tencent.qcloud.costransferpractice.transfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.qcloud.costransferpractice.COSConfigManager;
import com.tencent.qcloud.costransferpractice.R;
import com.tencent.qcloud.costransferpractice.common.FilePathHelper;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by rickenwang on 2018/10/18.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class TransferFragment extends Fragment implements TransferContract.View, View.OnClickListener{

    private final int OPEN_FILE_CODE = 10002;

    TransferContract.Presenter transferPresenter;

    View contentView;

    private ProgressBar uploadProgress;
    private ProgressBar downloadProgress;

    private TextView uploadState;
    private TextView downloadState;

    private TextView uploadProgressText;
    private TextView downloadProgressText;

    private Spinner regionSpinner;
    private Spinner bucketSpinner;

    private COSConfigManager cosConfigManager;

//    private String bucket;
//    private String region;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        contentView = inflater.inflate(R.layout.fragment_transfer, container, false);
        initContentView(contentView);
        cosConfigManager = COSConfigManager.getInstance();

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        transferPresenter.start();
    }

    @Override
    public void setPresenter(TransferContract.Presenter presenter) {

        this.transferPresenter = presenter;
    }

    private void initContentView(View contentView) {

        uploadProgress = contentView.findViewById(R.id.upload_progress);
        downloadProgress = contentView.findViewById(R.id.download_progress);
        uploadState = contentView.findViewById(R.id.upload_state);
        downloadState = contentView.findViewById(R.id.download_state);
        uploadProgressText = contentView.findViewById(R.id.upload_progress_text);
        downloadProgressText = contentView.findViewById(R.id.download_progress_text);

        regionSpinner = contentView.findViewById(R.id.region_spinner);
        bucketSpinner = contentView.findViewById(R.id.bucket_spinner);

        Button startUpload = contentView.findViewById(R.id.upload_start);
        Button pauseUpload = contentView.findViewById(R.id.upload_pause);
        Button resumeUpload = contentView.findViewById(R.id.upload_resume);
        Button cancelUpload = contentView.findViewById(R.id.upload_cancel);

        Button startDownload = contentView.findViewById(R.id.download_start);
        Button pauseDownload = contentView.findViewById(R.id.download_pause);
        Button resumeDownload = contentView.findViewById(R.id.download_resume);
        Button cancelDownload = contentView.findViewById(R.id.download_cancel);

        Button chooseFile = contentView.findViewById(R.id.choose_file);

        startUpload.setOnClickListener(this);
        pauseUpload.setOnClickListener(this);
        resumeUpload.setOnClickListener(this);
        cancelUpload.setOnClickListener(this);

        startDownload.setOnClickListener(this);
        pauseDownload.setOnClickListener(this);
        resumeDownload.setOnClickListener(this);
        cancelDownload.setOnClickListener(this);

        chooseFile.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.upload_start:
                transferPresenter.startUpload();
                break;
            case R.id.download_start:
                transferPresenter.startDownload();
                break;

            case R.id.upload_pause:
                transferPresenter.pauseUpload();
                break;
            case R.id.download_pause:
                transferPresenter.pauseDownload();
                break;

            case R.id.upload_resume:
                transferPresenter.resumeUpload();
                break;
            case R.id.download_resume:
                transferPresenter.resumeDownload();
                break;

            case R.id.upload_cancel:
                transferPresenter.cancelUpload();
                break;
            case R.id.download_cancel:
                transferPresenter.cancelDownload();
                break;

            case R.id.choose_file:
                openFileSelector();
                break;
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
    public void refreshUploadState(final TransferState state) {

        contentView.post(new Runnable() {
            @Override
            public void run() {
                uploadState.setText(state.toString());
            }
        });

    }

    @Override
    public void refreshDownloadState(final TransferState state) {

        contentView.post(new Runnable() {
            @Override
            public void run() {
                downloadState.setText(state.toString());
            }
        });
    }

    @Override
    public void refreshUploadProgress(final long progress, final long total) {

        contentView.post(new Runnable() {
            @Override
            public void run() {
                uploadProgress.setProgress((int) (100 * progress / total));
                uploadProgressText.setText(size(progress) + "/" + size(total));
            }
        });
    }

    @Override
    public void refreshDownloadProgress(final long progress, final long total) {

        contentView.post(new Runnable() {
            @Override
            public void run() {
                downloadProgress.setProgress((int) (100 * progress / total));
                downloadProgressText.setText(size(progress) + "/" + size(total));
            }
        });
    }

    @Override
    public void setLoading(boolean loading) {

    }

    @Override
    public void showRegionAndBucket(final Map<String, List<String>> buckets) {

        final List<String> regions = new LinkedList<>(buckets.keySet());

        regionSpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item, R.id.item,
                regions));

        if (regions.size() > 0) {


            bucketSpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item, R.id.item,
                    buckets.get(regions.get(0))));

            String region = regions.get(0);
            cosConfigManager.setRegion(region);
            cosConfigManager.setBucket(buckets.get(region).size() > 0 ? buckets.get(region).get(0) : null);
        }

        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                bucketSpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item, R.id.item,
                        buckets.get(regions.get(position))));

                cosConfigManager.setRegion(regions.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bucketSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cosConfigManager.setBucket((String) bucketSpinner.getAdapter().getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        transferPresenter.release();
    }

    private String size(long size) {

        float realSize = size;
        int index = 0;
        String [] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};

        while (realSize > 1000 && index < 5) {

            index++;
            realSize /= 1024;
        }

        String capacityText =  new DecimalFormat("###,###,###.##").format(realSize);
        return String.format(Locale.ENGLISH, "%s%s", capacityText, units[index]);
    }

    void openFileSelector() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_FILE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_FILE_CODE && resultCode == Activity.RESULT_OK) {

            cosConfigManager.setLocalFilePath(FilePathHelper.getPath(getActivity(), data.getData()));
        }
    }
}
