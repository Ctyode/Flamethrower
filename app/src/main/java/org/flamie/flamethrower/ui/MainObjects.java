package org.flamie.flamethrower.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import org.flamie.flamethrower.CameraController;
import org.flamie.flamethrower.OnSwipeTouchListener;
import org.flamie.flamethrower.VideoFileCallback;
import org.flamie.flamethrower.activities.CropActivity;
import org.flamie.flamethrower.ui.objects.BottomPanel;
import org.flamie.flamethrower.ui.objects.Circles;
import org.flamie.flamethrower.ui.objects.buttons.ButtonAccept;
import org.flamie.flamethrower.ui.objects.buttons.ButtonCapture;
import org.flamie.flamethrower.ui.objects.buttons.ButtonChange;
import org.flamie.flamethrower.ui.objects.buttons.ButtonDecline;
import org.flamie.flamethrower.ui.objects.buttons.ButtonPlay;
import org.flamie.flamethrower.ui.objects.buttons.CropButton;
import org.flamie.flamethrower.ui.objects.buttons.FlashButtonAuto;
import org.flamie.flamethrower.ui.objects.buttons.FlashButtonOff;
import org.flamie.flamethrower.ui.objects.buttons.FlashButtonOn;
import org.flamie.flamethrower.util.ImageSaveUtils;
import org.flamie.flamethrower.util.PreviewUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.flamie.flamethrower.util.DimenUtils.dp;

public class MainObjects extends RelativeLayout implements Camera.PictureCallback, VideoFileCallback {

    // TODO: исправить дерьмовую архитектуру, утечки памяти, убиться и не кодить никогда больше

    private static final String TAG = "MainObjects";
    private byte[] data;

    private CameraController cameraController;
    private Activity activity;
    private CameraPreview mPreview;

    private FlashButtonAuto flashButtonAuto;
    private FlashButtonOn flashButtonOn;
    private FlashButtonOff flashButtonOff;
    private BottomPanel confirmationPanel;
    private ImageView photoPreview;
    private ButtonAccept buttonAccept;
    private ButtonDecline buttonDecline;
    public static boolean isRecording = false;
    public static boolean videoMode = false;
    private boolean isFront = false;
    private boolean isPlaying = false;
    private Bitmap bitmap;
    private static Bitmap newBitmap;
    private boolean flashModeAuto = true;
    private boolean flashModeOn = false;
    private boolean flashModeOff = false;
    private ButtonPlay buttonPlay;
    private VideoView videoView;
    private ButtonChange buttonChange;
//    private SeekBar seekBar;
    private boolean blocked = false;
    private CropButton cropButton;
    public static boolean safeToTakePicture = false;
    private ButtonCapture buttonCapture;
    private BottomPanel bottomPanel;
    private Circles circles;

    public MainObjects(Context context, Activity activity, CameraController cameraController, CameraPreview cameraPreview) {
        super(context);
        this.activity = activity;
        this.cameraController = cameraController;
        this.mPreview = cameraPreview;

        cameraController.setVideoFileCallback(this);
        cameraController.onPicture(this);
        init();
    }

    private void init() {
        final Context context = activity.getApplication().getApplicationContext();
        flashButtonAuto = new FlashButtonAuto(context);
        flashButtonOn = new FlashButtonOn(context);
        flashButtonOff = new FlashButtonOff(context);
        confirmationPanel = new BottomPanel(context);
        photoPreview = new ImageView(context);
        photoPreview.setBackgroundColor(Color.rgb(0, 0, 0));
        buttonAccept = new ButtonAccept(context);
        buttonDecline = new ButtonDecline(context);
        buttonPlay = new ButtonPlay(context);
//        seekBar = new SeekBar(context);
        videoView = new VideoView(activity);
        videoView.setZOrderMediaOverlay(true);
        cropButton = new CropButton(context);
        circles = new Circles(context);

        bottomPanel = new BottomPanel(context);
        buttonCapture = new ButtonCapture(context);
        buttonCapture.setLongPressed(new Runnable() {
            @Override
            public void run() {
                longTap();
                buttonCapture.isLongPressed = true;
            }
        });
        buttonChange = new ButtonChange(context);

        confirmationPanel.setVisibility(INVISIBLE);
        photoPreview.setVisibility(INVISIBLE);
        buttonAccept.setVisibility(INVISIBLE);
        buttonDecline.setVisibility(INVISIBLE);
        flashButtonOff.setVisibility(INVISIBLE);
        cropButton.setVisibility(INVISIBLE);

        LayoutParams flashAutoLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams flashOnLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams flashOffLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams photoPreviewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        LayoutParams captureButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams acceptButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams declineButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams buttonChangeParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams bottomPanelParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams previewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        LayoutParams playButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams cropButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams circlesParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        LayoutParams seekBarParams = new LayoutParams(LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        seekBar.setThumb(null);

        flashAutoLayoutParams.addRule(ALIGN_PARENT_RIGHT);
        flashAutoLayoutParams.addRule(ALIGN_PARENT_TOP);
        flashAutoLayoutParams.topMargin = dp(-20);
        flashAutoLayoutParams.rightMargin = dp(10);

        flashOnLayoutParams.addRule(ALIGN_PARENT_RIGHT);
        flashOnLayoutParams.addRule(ALIGN_PARENT_TOP);
        flashOnLayoutParams.topMargin = dp(-20);
        flashOnLayoutParams.rightMargin = dp(10);

        flashOffLayoutParams.addRule(ALIGN_PARENT_RIGHT);
        flashOffLayoutParams.addRule(ALIGN_PARENT_TOP);
        flashOffLayoutParams.topMargin = dp(-20);
        flashOffLayoutParams.rightMargin = dp(10);

        playButtonParams.addRule(ALIGN_PARENT_BOTTOM);
        playButtonParams.addRule(CENTER_HORIZONTAL);
        playButtonParams.bottomMargin = dp(30);

        bottomPanelParams.addRule(ALIGN_PARENT_BOTTOM);
        bottomPanelParams.addRule(CENTER_HORIZONTAL);

        photoPreviewParams.addRule(ALIGN_PARENT_TOP);
        photoPreview.setScaleType(ImageView.ScaleType.FIT_START);
        buttonChangeParams.addRule(ALIGN_PARENT_BOTTOM);
        buttonChangeParams.addRule(ALIGN_PARENT_LEFT);
        buttonChangeParams.bottomMargin = dp(30);
        buttonChangeParams.leftMargin = dp(35);

        captureButtonParams.addRule(ALIGN_PARENT_BOTTOM);
        captureButtonParams.addRule(CENTER_HORIZONTAL);
        captureButtonParams.bottomMargin = dp(15);

        acceptButtonParams.addRule(ALIGN_PARENT_BOTTOM);
        acceptButtonParams.addRule(ALIGN_PARENT_RIGHT);
        acceptButtonParams.bottomMargin = dp(25);
        acceptButtonParams.rightMargin = dp(32);

        declineButtonParams.addRule(ALIGN_PARENT_BOTTOM);
        declineButtonParams.addRule(ALIGN_PARENT_LEFT);
        declineButtonParams.bottomMargin = dp(25);
        declineButtonParams.leftMargin = dp(32);

        cropButtonParams.addRule(ALIGN_PARENT_BOTTOM);
        cropButtonParams.addRule(CENTER_HORIZONTAL);
        cropButtonParams.bottomMargin = dp(30);

        circlesParams.addRule(ALIGN_PARENT_BOTTOM);
        circlesParams.addRule(CENTER_HORIZONTAL);
        circlesParams.bottomMargin = dp(50);

        flashButtonOn.hide();
        flashButtonOff.hide();
        buttonPlay.setVisibility(INVISIBLE);
        videoView.setVisibility(INVISIBLE);

        flashButtonAuto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                flashButtonAuto.hide();
                flashButtonOn.show();
                flashModeAuto = false;
                flashModeOn = true;
                flashModeOff = false;
            }
        });

        flashButtonOn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                flashButtonOn.hide();
                flashButtonOff.show();
                flashModeOn = false;
                flashModeAuto = false;
                flashModeOff = true;
            }
        });

        flashButtonOff.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                flashButtonOff.hide();
                flashButtonAuto.show();
                flashModeOn = false;
                flashModeAuto = true;
                flashModeOff = false;
            }
        });


        buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blocked = true;
                if(videoMode) {
                    if(isRecording) {
                        flashMode();
                        cameraController.requireStopRecord();
                        cameraController.getMediaRecorder().setPreviewDisplay(null);
                        cameraController.getCamera().stopPreview();

                        buttonCapture.transformToVideoFromStop();
                        bottomPanel.getSpringPositionY().setEndValue(0);
                        buttonPlay.setVisibility(VISIBLE);
                        buttonChange.setVisibility(INVISIBLE);
                        buttonCapture.setVisibility(INVISIBLE);
                        buttonAccept.setVisibility(VISIBLE);
                        buttonDecline.setVisibility(VISIBLE);
                        buttonAccept.show();
                        buttonDecline.show();
                        isRecording = false;
                    } else {
                        flashMode();
                        cameraController.setOrientationHint(calculateRotation());
                        cameraController.requireStartRecord();

                        buttonCapture.transformToStop();
                        bottomPanel.getSpringPositionY().setEndValue(dp(115));
                        isRecording = true;
                    }
                } else {
                    flashMode();
                    if(safeToTakePicture) {
                        cameraController.requireCameraPicture();
                        safeToTakePicture = false;
                    }
                }
            }
         });

        buttonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFront) {
                    buttonChange.getSpringStroke().setEndValue(dp(2));
                    buttonChange.getSpringRotate().setEndValue(180);
                    buttonChange.getSpringRadius().setEndValue(dp(7));
                    isFront = false;
                } else {
                    buttonChange.getSpringStroke().setEndValue(dp(9));
                    buttonChange.getSpringRotate().setEndValue(0);
                    buttonChange.getSpringRadius().setEndValue(dp(3));
                    isFront = true;
                }
                cameraController.requireCameraCycle();
            }
         });

        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DA
                if(videoMode) {
                    buttonPlay.setVisibility(INVISIBLE);
                    videoView.setVisibility(INVISIBLE);
                    buttonCapture.setVisibility(VISIBLE);
                    buttonChange.setVisibility(VISIBLE);
                    buttonCapture.afterLongTap();
                } else {
                    ImageSaveUtils.saveImage(data);
                    bitmap.recycle();
                    newBitmap.recycle();
                    System.gc();
                    confirmationPanel.setVisibility(INVISIBLE);
                    photoPreview.setVisibility(INVISIBLE);
                    cropButton.setVisibility(INVISIBLE);
                }
                startPreview();
                buttonAccept.setVisibility(INVISIBLE);
                buttonDecline.setVisibility(INVISIBLE);
                blocked = false;
                safeToTakePicture = true;
            }
        });

        buttonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NJET
                if(videoMode) {
                    buttonCapture.afterLongTap();
                    buttonPlay.setVisibility(INVISIBLE);
                    videoView.setVisibility(INVISIBLE);
                    buttonCapture.setVisibility(VISIBLE);
                    buttonChange.setVisibility(VISIBLE);
                    ImageSaveUtils.mediaFile.delete();
                } else {
                    bitmap.recycle();
                    newBitmap.recycle();
                    System.gc();
                    photoPreview.setVisibility(INVISIBLE);
                    confirmationPanel.setVisibility(INVISIBLE);
                    cropButton.setVisibility(INVISIBLE);
                }
                startPreview();
                buttonAccept.hide();
                buttonDecline.hide();
                blocked = false;
                safeToTakePicture = true;
            }
        });

        mPreview.setOnTouchListener(new OnSwipeTouchListener(activity) {
            public void onSwipeLeft() {
                if(!blocked) {
                    bottomPanel.getSpringOpacity().setEndValue(110);
                    buttonCapture.transformToVideo();
                    videoMode = true;
                    buttonCapture.isLongPressed = false;
                }
            }

            public void onSwipeRight() {
                if(!blocked) {
                    bottomPanel.getSpringOpacity().setEndValue(255);
                    buttonCapture.transformToPhoto();
                    videoMode = false;
                    buttonCapture.isLongPressed = false;
                }
            }
        });

        buttonPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying) {
                    videoView.pause();
                    isPlaying = false;
                } else {
                    videoView.start();
                    isPlaying = true;
                }
            }
        });

        cropButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, CropActivity.class);
                activity.startActivity(intent);
            }
        });

//        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                seekBar.setMax(videoView.getDuration());
//                seekBar.postDelayed(onEverySecond, 1000);
//            }
//        });

//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if(fromUser) {
//                    videoView.seekTo(progress);
//                }
//            }
//        });

        flashButtonAuto.setLayoutParams(flashAutoLayoutParams);
        flashButtonOn.setLayoutParams(flashOnLayoutParams);
        flashButtonOff.setLayoutParams(flashOffLayoutParams);

        buttonAccept.setLayoutParams(acceptButtonParams);
        buttonDecline.setLayoutParams(declineButtonParams);
        photoPreview.setLayoutParams(photoPreviewParams);
        buttonCapture.setLayoutParams(captureButtonParams);
        bottomPanel.setLayoutParams(bottomPanelParams);
        buttonChange.setLayoutParams(buttonChangeParams);
        buttonPlay.setLayoutParams(playButtonParams);
        videoView.setLayoutParams(photoPreviewParams);
        cropButton.setLayoutParams(cropButtonParams);
        circles.setLayoutParams(circlesParams);
//        seekBar.setLayoutParams(seekBarParams);
        setLayoutParams(previewParams);

        addView(mPreview);
        addView(photoPreview);
        addView(videoView);
        addView(bottomPanel);
        addView(buttonChange);
        addView(flashButtonOff);
        addView(flashButtonOn);
        addView(flashButtonAuto);
        addView(buttonCapture);

        addView(confirmationPanel);
        addView(buttonAccept);
        addView(buttonDecline);
        addView(buttonPlay);
        addView(cropButton);
        addView(circles);
//        addView(seekBar);
    }

//    private Runnable onEverySecond = new Runnable() {
//        @Override
//        public void run() {
//            if(seekBar != null) {
//                seekBar.setProgress(videoView.getCurrentPosition());
//            }
//
//            if(videoView.isPlaying()) {
//                seekBar.postDelayed(onEverySecond, 1000);
//            }
//
//        }
//    };

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        this.data = data;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 3;
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        Matrix matrix = new Matrix();
        if (cameraController.getCameraInfo().facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            float[] mirrorY = { -1, 0, 0, 0, 1, 0, 0, 0, 1 };
            Matrix matrixMirrorY = new Matrix();
            matrixMirrorY.setValues(mirrorY);
            matrix.postConcat(matrixMirrorY);
        }
        matrix.postRotate(PreviewUtils.cameraRotation(cameraController.getCameraInfo(),
                          activity.getWindowManager().getDefaultDisplay().getRotation()));

        newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        this.data = stream.toByteArray();
        photoPreview.setImageBitmap(newBitmap);

        confirmationPanel.setVisibility(VISIBLE);
        photoPreview.setVisibility(VISIBLE);
        buttonAccept.setVisibility(VISIBLE);
        buttonDecline.setVisibility(VISIBLE);

        buttonAccept.show();
        buttonDecline.show();
        cropButton.setVisibility(VISIBLE);
        blocked = true;
    }

    private int calculateRotation() {
        int rotation = PreviewUtils.cameraRotation(cameraController.getCameraInfo(),
                     activity.getWindowManager().getDefaultDisplay().getRotation());
        if(cameraController.getCameraInfo().facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return rotation + 180;
        } else {
            return rotation;
        }
    }

    @Override
    public void videoFile(String path) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoView.setVideoPath(ImageSaveUtils.mediaFile.getAbsolutePath());
                videoView.setMediaController(null);
                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        return true;
                    }
                });
                videoView.setVisibility(VISIBLE);
            }
        });
    }

    public void startPreview() {
        try {
            cameraController.getCamera().reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cameraController.getCamera().startPreview();
    }

    public void longTap() {
        isRecording = true;
        videoMode = true;
        cameraController.setOrientationHint(calculateRotation());
        cameraController.requireStartRecord();

        buttonCapture.transformFromPhotoToStop();
        bottomPanel.getSpringPositionY().setEndValue(dp(115));
    }

    public void flashMode() {
        if(cameraController.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            if(!videoMode) {
                if (flashModeOff) {
                    Camera.Parameters parameters = cameraController.getCamera().getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    cameraController.getCamera().setParameters(parameters);
                } else if (flashModeOn) {
                    Camera.Parameters parameters = cameraController.getCamera().getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    cameraController.getCamera().setParameters(parameters);
                } else if (flashModeAuto) {
                    Camera.Parameters parameters = cameraController.getCamera().getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    cameraController.getCamera().setParameters(parameters);
                }
            } else {
                if(!isRecording) {
                    if (flashModeOff || flashModeAuto) {
                        Camera.Parameters parameters = cameraController.getCamera().getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        cameraController.getCamera().setParameters(parameters);
                    } else if (flashModeOn) {
                        Camera.Parameters parameters = cameraController.getCamera().getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        cameraController.getCamera().setParameters(parameters);
                    }
                } else {
                    Camera.Parameters parameters = cameraController.getCamera().getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    cameraController.getCamera().setParameters(parameters);
                }
            }
        }
    }

    public static Bitmap getBitmap() {
        return newBitmap;
    }
}
