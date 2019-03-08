package com.amazonaws.kinesisvideo.demoapp.fragment;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.amazonaws.kinesisvideo.client.KinesisVideoClient;
import com.amazonaws.kinesisvideo.client.mediasource.CameraMediaSourceConfiguration;
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;
import com.amazonaws.kinesisvideo.demoapp.KinesisVideoDemoApp;
import com.amazonaws.kinesisvideo.demoapp.R;
import com.amazonaws.kinesisvideo.demoapp.activity.SimpleNavActivity;
import com.amazonaws.kinesisvideo.demoapp.constant.Constants;
import com.amazonaws.kinesisvideo.demoapp.ui.adapter.ToStrings;
import com.amazonaws.kinesisvideo.demoapp.ui.widget.StringSpinnerWidget;
import com.amazonaws.kinesisvideo.producer.StreamInfo;
import com.amazonaws.mobileconnectors.kinesisvideo.client.KinesisVideoAndroidClientFactory;
import com.amazonaws.mobileconnectors.kinesisvideo.data.MimeType;
import com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android.AndroidCameraMediaSourceConfiguration;

import java.util.ArrayList;
import java.util.List;

import static com.amazonaws.mobileconnectors.kinesisvideo.util.CameraUtils.getCameras;
import static com.amazonaws.mobileconnectors.kinesisvideo.util.CameraUtils.getSupportedResolutions;
import static com.amazonaws.mobileconnectors.kinesisvideo.util.VideoEncoderUtils.getSupportedMimeTypes;

public class StreamConfigurationFragment extends Fragment {
    private static final String TAG = StreamConfigurationFragment.class.getSimpleName();
    private static final Size RESOLUTION_320x240 = new Size(320, 240);
    private static final int FRAMERATE_20 = 20;
    private static final int BITRATE_384_KBPS = 384 * 1024;
    private static final int RETENTION_PERIOD_48_HOURS = 2 * 24;

    private Button mStartStreamingButton;
    private CheckBox mMotionDetectionCheckBox;
    private CheckBox mNotificationListenCheckBox;
    private CheckBox mFrontBackViewCheckBox;
    private boolean is360Enabled = false;
    private EditText mStreamName;
    private KinesisVideoClient mKinesisVideoClient;

    private StringSpinnerWidget<CameraMediaSourceConfiguration> mCamerasDropdown;
    private StringSpinnerWidget<Size> mResolutionDropdown;
    private StringSpinnerWidget<MimeType> mMimeTypeDropdown;
    List<CameraMediaSourceConfiguration> cameras = new ArrayList<>();

    private SimpleNavActivity navActivity;

    public static StreamConfigurationFragment newInstance(SimpleNavActivity navActivity) {
        StreamConfigurationFragment s = new StreamConfigurationFragment();
        s.navActivity = navActivity;
        return s;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.CAMERA}, 9393);
        }

        getActivity().setTitle(getActivity().getString(R.string.title_fragment_stream));

        final View view = inflater.inflate(R.layout.fragment_stream_configuration, container, false);

        try {
            mKinesisVideoClient = KinesisVideoAndroidClientFactory.createKinesisVideoClient(
                    getActivity(),
                    KinesisVideoDemoApp.KINESIS_VIDEO_REGION,
                    KinesisVideoDemoApp.getCredentialsProvider());
        } catch (KinesisVideoException e) {
            Log.e(TAG, "Failed to create Kinesis Video client", e);
        }

        cameras = getCameras(mKinesisVideoClient);
        mCamerasDropdown = new StringSpinnerWidget<>(
                getActivity(),
                view,
                R.id.cameras_spinner,
                ToStrings.CAMERA_DESCRIPTION,
                cameras);

        mCamerasDropdown.setItemSelectedListener(
                new StringSpinnerWidget.ItemSelectedListener<CameraMediaSourceConfiguration>() {
                    @Override
                    public void itemSelected(final CameraMediaSourceConfiguration mediaSource) {
                        mResolutionDropdown = new StringSpinnerWidget<>(
                                getActivity(),
                                view,
                                R.id.resolutions_spinner,
                                getSupportedResolutions(getActivity(), mediaSource.getCameraId()));
                        select640orBelow();
                    }
                });

        mMimeTypeDropdown = new StringSpinnerWidget<>(
                getActivity(),
                view,
                R.id.codecs_spinner,
                getSupportedMimeTypes());

        return view;
    }

    private void select640orBelow() {
        Size tmpSize = new Size(0, 0);
        int indexToSelect = 0;
        for (int i = 0; i < mResolutionDropdown.getCount(); i++) {
            final Size resolution = mResolutionDropdown.getItem(i);
            if (resolution.getWidth() <= RESOLUTION_320x240.getWidth()
                    && tmpSize.getWidth() <= resolution.getWidth()
                    && resolution.getHeight() <= RESOLUTION_320x240.getHeight()
                    && tmpSize.getHeight() <= resolution.getHeight()) {

                tmpSize = resolution;
                indexToSelect = i;
            }
        }

        mResolutionDropdown.selectItem(indexToSelect);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mStartStreamingButton = (Button) view.findViewById(R.id.start_streaming);
        mStartStreamingButton.setOnClickListener(startStreamingActivityWhenClicked());

        mMotionDetectionCheckBox = (CheckBox) view.findViewById(R.id.checkBoxMotionDetection);
        mMotionDetectionCheckBox.setOnClickListener(toggleMotionDetectionWhenClicked());

        mNotificationListenCheckBox = (CheckBox) view.findViewById(R.id.checkBoxNotificationListen);
        mNotificationListenCheckBox.setOnClickListener(toggleNotificationListenWhenClicked());

        mFrontBackViewCheckBox = (CheckBox) view.findViewById(R.id.checkBoxFrontBackView);
        mFrontBackViewCheckBox.setOnClickListener(toggle360ViewWhenClicked());
        mStreamName = (EditText) view.findViewById(R.id.stream_name);
    }

    private View.OnClickListener startStreamingActivityWhenClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                startStreamingActivity();
            }
        };
    }

    private View.OnClickListener toggleMotionDetectionWhenClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(((CompoundButton) view).isChecked()){
                    navActivity.startBackgroundMotionDetectionService();
                } else {
                    navActivity.stopBackgroundMotionDetectionService();
                }
            }
        };
    }

    private View.OnClickListener toggleNotificationListenWhenClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(((CompoundButton) view).isChecked()){
                    navActivity.setRemoteControlEnable(true);
                } else {
                    navActivity.setRemoteControlEnable(false);
                }
            }
        };
    }

    private View.OnClickListener toggle360ViewWhenClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(((CompoundButton) view).isChecked()){
                    is360Enabled = true;
                } else {
                    is360Enabled = false;
                }
            }
        };
    }

    public void startStreamingActivity() {
        final Bundle extras = new Bundle();

        extras.putParcelable(
                StreamingFragment.KEY_MEDIA_SOURCE_CONFIGURATION_1,
                getCurrentConfiguration1());

        extras.putParcelable(
                StreamingFragment.KEY_MEDIA_SOURCE_CONFIGURATION_2,
                getCurrentConfiguration2());

        extras.putString(
                StreamingFragment.KEY_STREAM_NAME,
                mStreamName.getText().toString());

        navActivity.startStreamingFragment(extras);
    }

    private AndroidCameraMediaSourceConfiguration getCurrentConfiguration1() {
        if (mCamerasDropdown.getSelectedItem().getCameraFacing() == ToStrings.FACING_FRONT || is360Enabled) {
            CameraMediaSourceConfiguration selectedCamera = cameras.get(1); // Front camera
            return new AndroidCameraMediaSourceConfiguration(
                    AndroidCameraMediaSourceConfiguration.builder()
                            .withCameraId(selectedCamera.getCameraId())
                            .withEncodingMimeType(mMimeTypeDropdown.getSelectedItem().getMimeType())
                            .withHorizontalResolution(mResolutionDropdown.getSelectedItem().getWidth())
                            .withVerticalResolution(mResolutionDropdown.getSelectedItem().getHeight())
                            .withCameraFacing(selectedCamera.getCameraFacing())
                            .withIsEncoderHardwareAccelerated(
                                    selectedCamera.isEndcoderHardwareAccelerated())
                            .withFrameRate(FRAMERATE_20)
                            .withRetentionPeriodInHours(RETENTION_PERIOD_48_HOURS)
                            .withEncodingBitRate(BITRATE_384_KBPS)
                            .withCameraOrientation(-selectedCamera.getCameraOrientation())
                            .withNalAdaptationFlags(StreamInfo.NalAdaptationFlags.NAL_ADAPTATION_ANNEXB_CPD_AND_FRAME_NALS)
                            .withIsAbsoluteTimecode(false));
        } else {
            return null;
        }
    }

    private AndroidCameraMediaSourceConfiguration getCurrentConfiguration2() {
        if (mCamerasDropdown.getSelectedItem().getCameraFacing() == ToStrings.FACING_BACK || is360Enabled) {
            CameraMediaSourceConfiguration selectedCamera = cameras.get(0); // Back camera
            return new AndroidCameraMediaSourceConfiguration(
                    AndroidCameraMediaSourceConfiguration.builder()
                            .withCameraId(selectedCamera.getCameraId())
                            .withEncodingMimeType(mMimeTypeDropdown.getSelectedItem().getMimeType())
                            .withHorizontalResolution(mResolutionDropdown.getSelectedItem().getWidth())
                            .withVerticalResolution(mResolutionDropdown.getSelectedItem().getHeight())
                            .withCameraFacing(selectedCamera.getCameraFacing())
                            .withIsEncoderHardwareAccelerated(
                                    selectedCamera.isEndcoderHardwareAccelerated())
                            .withFrameRate(FRAMERATE_20)
                            .withRetentionPeriodInHours(RETENTION_PERIOD_48_HOURS)
                            .withEncodingBitRate(BITRATE_384_KBPS)
                            .withCameraOrientation(-selectedCamera.getCameraOrientation())
                            .withNalAdaptationFlags(StreamInfo.NalAdaptationFlags.NAL_ADAPTATION_ANNEXB_CPD_AND_FRAME_NALS)
                            .withIsAbsoluteTimecode(false));
        } else {
            return null;
        }
    }
}