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
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class SmartClickReceiver extends BroadcastReceiver {
    /**
     * Called when the activity is first created.
     */

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.e("LOB", "onReceive");
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            //NOT YET IMPLEMENTED
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            try {
                noPreviewPictureCapture(context);
            } catch (IOException e) {
                Log.d("IO Exception", "onReceive: " + e);
            } catch (InterruptedException e) {
                Log.d("Interrupted Exception", "onReceive: " + e);
            }
        }
    }

    public void noPreviewPictureCapture(final Context context) throws IOException, InterruptedException {
        // Opens back facing camera by default
        // Initialize camera variable with FFC object
        final Camera myCamera = Camera.open(findFrontFacingCamera());

        if (myCamera != null) {
            try {
                SurfaceTexture surfaceTexture = new SurfaceTexture(10);
                myCamera.setPreviewTexture(surfaceTexture);
                myCamera.startPreview();

                //Set camera parameters
                Camera.Parameters parameters = myCamera.getParameters();
                parameters.setRotation(270);
                myCamera.setParameters(parameters);

                myCamera.takePicture(null, null, getJpegCallback(context, myCamera));
            } catch (Exception e) {
                Toast.makeText(context, "Error taking the picture", Toast.LENGTH_SHORT).show();
                Log.d("FFC CAPTURE FAILED", "noPreviewPictureCapture: " + e);
            }
        } else {
            Toast.makeText(context, "No Front Facing Camera!", Toast.LENGTH_LONG).show();
            Log.d("FFC NOT FOUND", "noPreviewPictureCapture: FFC not found");
        }
    }

    private Camera.PictureCallback getJpegCallback(final Context context, final Camera camera) {
        Camera.PictureCallback mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File imageFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (imageFile == null) {
                    return;
                }
                try {
                    //Save the image file
                    FileOutputStream fos = new FileOutputStream(imageFile);
                    fos.write(data);
                    fos.close();

                    //Release camera object
                    camera.release();

                    int faceCount = getFaceCount(imageFile.getPath().toString());
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
                break;
            }
        }
        return cameraId;
    }


    //Create a File for saving an image or video
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

    //Count the no. of faces in the given image
    public int getFaceCount(String ffcImage) {
        int face_count;
        FaceDetector.Face[] faces;
        Bitmap ffcImageBitmap;
        final int MAX_FACES = 10;
        // Set internal configuration to RGB_565
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        ffcImageBitmap = BitmapFactory.decodeFile(ffcImage, bitmapOptions);
        FaceDetector face_detector = new FaceDetector(
                ffcImageBitmap.getWidth(), ffcImageBitmap.getHeight(),
                MAX_FACES);

        faces = new FaceDetector.Face[MAX_FACES];
        // The bitmap must be in 565 format (for now).
        face_count = face_detector.findFaces(ffcImageBitmap, faces);
        Log.d("Face_Detection", "Face Count: " + String.valueOf(face_count));

        return face_count;
    }
}
