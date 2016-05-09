package com.nacho.padtranslate.cloudvision;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageContext;
import com.google.common.base.Function;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class CloudVisionScanAsyncTask implements Function<CloudVisionParams, CloudVisionResult> {
    private static final String TAG = CloudVisionScanAsyncTask.class.getSimpleName();
    // ANDROID KEY
//    private static String CLOUD_VISION_API_KEY = "AIzaSyCtXe_IDuVGLsQn-blStZFgMx5Bb0yyjsU";
    // SERVER KEY
    private static String CLOUD_VISION_API_KEY = "AIzaSyDsZI83jY6umCqqfCNmrln5sVADaF3tqHw";

    @Nullable
    @Override
    public CloudVisionResult apply(@Nullable final CloudVisionParams input) {
        try {
            HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
            builder.setVisionRequestInitializer(new
                    VisionRequestInitializer(CLOUD_VISION_API_KEY));
            builder.setApplicationName("PAD_Translate");
            Vision vision = builder.build();

            BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
            List<AnnotateImageRequest> requests = new ArrayList<>();
            AnnotateImageRequest request = new AnnotateImageRequest();
            request.setImage(bitmapToImage(input.getData()));
            request.setImageContext(new ImageContext()
                    .setLanguageHints(Collections.singletonList(input.getTargetLang())));
            request.setFeatures(new ArrayList<Feature>() {{
                add(new Feature().setType("TEXT_DETECTION"));
            }});
            requests.add(request);
            batchRequest.setRequests(requests);

            Vision.Images.Annotate annotateRequest = vision.images().annotate(batchRequest);
            // Due to a bug: requests to Vision API containing large images fail when GZipped.
            annotateRequest.setDisableGZipContent(true);
            Log.d(TAG, "created Cloud Vision request object, sending request");

            BatchAnnotateImagesResponse response = annotateRequest.execute();
            return convertResponse(response, input);

        } catch (GoogleJsonResponseException e) {
            Log.d(TAG, "failed to make API request because " + e.getContent());
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
        return null;
    }

    private CloudVisionResult convertResponse(BatchAnnotateImagesResponse response, CloudVisionParams params) {
        Preconditions.checkArgument(response.getResponses().size() == 1);
        return new CloudVisionResult(
                params.getData(), response.getResponses().get(0).getTextAnnotations());
    }

    private Image bitmapToImage(Bitmap bitmap) {
        // Add the image
        Image image = new Image();
        // Convert the bitmap to a JPEG
        // Just in case it's a format that Android understands but Cloud Vision
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Base64 encode the JPEG
        image.encodeContent(imageBytes);
        return image;
    }
    

}
