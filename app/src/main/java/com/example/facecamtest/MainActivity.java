package com.example.facecamtest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class MainActivity extends Activity {

    private static final int PICK_VIDEO_REQUEST = 101;

    private TextView tvStatus;
    private VideoView videoView;
    private Button btnPickVideo;
    private Button btnToggleStream;

    private Uri selectedVideoUri = null;
    private boolean armedForCameraRequest = false;
    private boolean mirrorHorizontal = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(0xFF0F172A);

        videoView = new VideoView(this);
        FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        );
        videoView.setLayoutParams(videoParams);
        if (mirrorHorizontal) {
            videoView.setScaleX(-1f);
        }
        rootLayout.addView(videoView);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.VERTICAL);
        topBar.setBackgroundColor(0x99020617);
        topBar.setPadding(32, 32, 32, 32);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("محاكي بث كاميرا الفيديو");
        tvTitle.setTextColor(0xFFF8FAFC);
        tvTitle.setTextSize(20f);
        topBar.addView(tvTitle);

        tvStatus = new TextView(this);
        tvStatus.setText("الحالة: بانتظار اختيار فيديو");
        tvStatus.setTextColor(0xFF94A3B8);
        tvStatus.setTextSize(14f);
        tvStatus.setPadding(0, 8, 0, 0);
        topBar.addView(tvStatus);

        rootLayout.addView(topBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP
        ));

        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setBackgroundColor(0xCC020617);
        bottomBar.setPadding(24, 24, 24, 24);

        btnPickVideo = new Button(this);
        btnPickVideo.setText("اختيار فيديو");
        btnPickVideo.setBackgroundColor(0xFF2563EB);
        btnPickVideo.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams pickParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        pickParams.setMargins(0, 0, 12, 0);
        btnPickVideo.setLayoutParams(pickParams);

        btnToggleStream = new Button(this);
        btnToggleStream.setText("تشغيل عند الطلب");
        btnToggleStream.setBackgroundColor(0xFF16A34A);
        btnToggleStream.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams toggleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        toggleParams.setMargins(12, 0, 0, 0);
        btnToggleStream.setLayoutParams(toggleParams);

        bottomBar.addView(btnPickVideo);
        bottomBar.addView(btnToggleStream);

        rootLayout.addView(bottomBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
        ));

        setContentView(rootLayout);
        D
        tnPickVideo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
            startActivityForResult(intent, PICK_VIDEO_REQUEST);
        });

        btnToggleStream.setOnClickListener(v -> {
            if (selectedVideoUri == null) {
                tvStatus.setText("الحالة: اختر ملف فيديو أولاً");
                return;
            }
            armedForCameraRequest = !armedForCameraRequest;
            if (armedForCameraRequest) {
                btnToggleStream.setText("إيقاف البث");
                onCameraRequestTriggered();
            } else {
                btnToggleStream.setText("تشغيل عند الطلب");
                stopVideoStream();
                tvStatus.setText("الحالة: البث متوقف");
            }
        });
    }

    public void onCameraRequestTriggered() {
        if (selectedVideoUri == null) return;

        videoView.setVideoURI(selectedVideoUri);
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
            videoView.start();
        });
        videoView.setOnErrorListener((mp, what, extra) -> {
            tvStatus.setText("الحالة: تعذر تشغيل الفيديو");
            return true;
        });
        tvStatus.setText("الحالة: جاري بث الفيديو ككاميرا...");
    }

    private void stopVideoStream() {
        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                } catch (Exception ignored) {
                }
                selectedVideoUri = uri;
                tvStatus.setText("الحالة: تم اختيار الفيديو بنجاح");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (armedForCameraRequest && selectedVideoUri != null) {
            onCameraRequestTriggered();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopVideoStream();
    }
}