package smartfeaches.example.com.smartfeaches;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class ScreenReceiver extends BroadcastReceiver {


    //Camera variables
    //a surface holder
    private SurfaceHolder sHolder;
    //a variable to control the camera
    private Camera mCamera;
    //the camera parameters
    private Camera.Parameters parameters;
    /**
     * Called when the activity is first created.
     */
    public static boolean wasScreenOn = true;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.e("LOB", "onReceive");
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            try {
                takePictureNoPreview(context);
            } catch (IOException e) {

            } catch (InterruptedException e) {

            }

        } else if (intent.getAction().equals()) {

        }
    }

   /* @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_POWER) {
            // Do something here...
            event.startTracking(); // Needed to track long presses
            return true;
        }
        return new Activity().onKeyDown(keyCode, event);
    }*/

    public void takePictureNoPreview(final Context context) throws IOException, InterruptedException {
        // open back facing camera by default
        final Camera myCamera = Camera.open(findFrontFacingCamera());

        if (myCamera != null) {
            try {
                SurfaceTexture surfaceTexture = new SurfaceTexture(10);
                myCamera.setPreviewTexture(surfaceTexture);
                myCamera.startPreview();
                Camera.Parameters parameters = myCamera.getParameters();
                parameters.setRotation(270);
                myCamera.setParameters(parameters);

                myCamera.takePicture(null, null, getJpegCallback(context, myCamera));
            } catch (Exception e) {
                Toast.makeText(context, "Error taking the picture", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(context, "No Front Facing Camera!", Toast.LENGTH_LONG).show();
        }
    }


    private Camera.PictureCallback getJpegCallback(final Context context, final Camera myCamera) {
        Camera.PictureCallback mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    myCamera.release();

                    int faceCount = getFaceCount(pictureFile.getPath().toString());
                    if (faceCount > 0) {
                        String faceCountMessage = "Face count is: " + faceCount;
                        Toast.makeText(context, faceCountMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        String faceCountMessage = "No face found";
                        Toast.makeText(context, faceCountMessage, Toast.LENGTH_SHORT).show();
                    }
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
            }
        };
        return mPicture;
    }

    //Check if the device has a front facing camera
    private int findFrontFacingCamera() {

        int cameraId = 0;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                //cameraFront = true;
                break;
            }
        }
        return cameraId;
    }
///////////////////////////////////////////////////////////////////////////

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }


    /**
     * Create a File for saving an image or video
     */
    public static File getOutputMediaFile(int type) {
        //Store the image in the internal storage
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "SmartFeaches");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("SmartFeaches", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    public int getFaceCount(String image_fn) {
        int face_count;
        FaceDetector.Face[] faces;
        Bitmap background_image;
        final int MAX_FACES = 10;
        // Set internal configuration to RGB_565
        BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
        bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;

        background_image = BitmapFactory.decodeFile(image_fn, bitmap_options);
        FaceDetector face_detector = new FaceDetector(
                background_image.getWidth(), background_image.getHeight(),
                MAX_FACES);

        faces = new FaceDetector.Face[MAX_FACES];
        // The bitmap must be in 565 format (for now).
        face_count = face_detector.findFaces(background_image, faces);
        Log.d("Face_Detection", "Face Count: " + String.valueOf(face_count));

        return face_count;
    }
}
