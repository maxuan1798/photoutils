package com.example.maxuan.photoutils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoUtils {

    public static final int REQUEST_PHOTO = 12;
    public static final int REQUEST_CAMERA = 13;
    public static final int REQUEST_PHOTO_CROP = 14;
    private static String rootDir = Environment.getExternalStorageDirectory().getPath() + File.separator +"aikeapp";

    public static String picture_dir = "picture";

    public static String voice_dir = "voice";

    public static boolean checkRootDir() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            throw new IllegalStateException("the external storage unavailable!");
        }
        File root = new File(rootDir);
        if (!root.exists()) {
            if (!root.mkdirs()) {
                Log.e("TAG", "fail to create root directory!");
                return false;
            }
        }
        return true;
    }

    public static Uri getOutputStorageFileUri(String directory, String filename) {
        if (checkRootDir()) {
            File storageDir = new File(rootDir + File.separator + directory);
            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.e("TAG", "file to create directory" + directory);
                    return null;
                }
            }
            File storageFile = new File(storageDir.getPath() + File.separator + filename);
            return Uri.fromFile(storageFile);
        }
        return null;
    }

    public static Uri getOutputSoundUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return getOutputStorageFileUri(picture_dir, "#" + timeStamp);
    }

    public static Uri getOutputPictureUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return getOutputStorageFileUri(picture_dir, "IMG_" + timeStamp + ".jpg");
    }

    public static Uri getOutputPictureUri(String token) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return getOutputStorageFileUri(picture_dir, "IMG_" + token + timeStamp + ".jpg");
    }

    public static Uri getOutputVoiceUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return getOutputStorageFileUri(voice_dir, "voice_" + timeStamp);
    }

    public static String getOutputVoiceDir(String filename) {
        return getOutputStorageFileUri(voice_dir, filename).getPath();
    }

    public static Uri getOutputMediaFileUri() { //获取公共照片存储Uri
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "aikeapp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("aikeapp", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp
                + ".jpg");

        return Uri.fromFile(mediaFile);
    }

    public static BitmapFactory.Options getBitmapOptions(String filePath) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);
        return opts;
    }

    public static File compressImage(String filePath) {
        String newPath = getOutputPictureUri(new File(filePath).getName()).getPath();
        return new File(compressImage(filePath, newPath));
    }

    public static String compressImage(String path, String newPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int inSampleSize = 1;
        int maxSize = 3000;
        if (options.outWidth > maxSize || options.outHeight > maxSize) {
            int widthScale = (int) Math.ceil(options.outWidth * 1.0 / maxSize);
            int heightScale = (int) Math.ceil(options.outHeight * 1.0 / maxSize);
            inSampleSize = Math.max(widthScale, heightScale);
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (bitmap == null) return path;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int newW = w;
        int newH = h;
        if (w > maxSize || h > maxSize) {
            if (w > h) {
                newW = maxSize;
                newH = (int) (newW * h * 1.0 / w);
            } else {
                newH = maxSize;
                newW = (int) (newH * w * 1.0 / h);
            }
        }
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, newW, newH, false);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(newPath);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        recycle(newBitmap);
        recycle(bitmap);
        return newPath;
    }

    public static void recycle(Bitmap bitmap) {
        // 先判断是否已经回收
        if (bitmap != null && !bitmap.isRecycled()) {
            // 回收并且置为null
            bitmap.recycle();
        }
        System.gc();
    }

    public static void deleteFiles(String[] paths) {
        for (String path: paths) {
            File file = new File(path);
            if (file.exists()) file.delete();
        }
    }

    public static Uri camera(Activity activity) {
        Uri newUri = getOutputMediaFileUri();
        Log.e("TAG", newUri.getPath());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, newUri);
        activity.startActivityForResult(intent, REQUEST_PHOTO);
        return newUri;
    }

    public static void photo(Activity activity) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(i, REQUEST_PHOTO);
    }

    public static Uri cropImageUri(Activity activity, Uri data, int aspectX, int aspectY, int outputX, int outputY){
        Uri newUri = getOutputPictureUri();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, newUri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        activity.startActivityForResult(intent, REQUEST_PHOTO_CROP);
        return newUri;
    }

    //图片解码
    public static ImageDecodeOptions getImageDecodeOptions(){
        ImageDecodeOptions decodeOptions = ImageDecodeOptions.newBuilder()
//            .setBackgroundColor(Color.TRANSPARENT)//图片的背景颜色
//            .setDecodeAllFrames(decodeAllFrames)//解码所有帧
//            .setDecodePreviewFrame(decodePreviewFrame)//解码预览框
//            .setForceOldAnimationCode(forceOldAnimationCode)//使用以前动画
//            .setFrom(options)//使用已经存在的图像解码
//            .setMinDecodeIntervalMs(intervalMs)//最小解码间隔（分位单位）
                .setUseLastFrameForPreview(true)//使用最后一帧进行预览
                .build();
        return decodeOptions;
    }

    public static ImageRequest getImageRequest(SimpleDraweeView view, String uri, int w, int h){
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(parseUrl(uri)))
                .setAutoRotateEnabled(true)//自动旋转图片方向
                .setImageDecodeOptions(getImageDecodeOptions())//  图片解码库
                .setImageType(ImageRequest.ImageType.SMALL)//图片类型，设置后可调整图片放入小图磁盘空间还是默认图片磁盘空间
                .setLocalThumbnailPreviewsEnabled(true)//缩略图预览，影响图片显示速度（轻微）
//                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)//请求经过缓存级别  BITMAP_MEMORY_CACHE，ENCODED_MEMORY_CACHE，DISK_CACHE，FULL_FETCH
//            .setPostprocessor(postprocessor)//修改图片
//            .setProgressiveRenderingEnabled(true)//渐进加载，主要用于渐进式的JPEG图，影响图片显示速度（普通）
                .setResizeOptions(new ResizeOptions(w, h))//调整大小
//            .setSource(Uri uri)//设置图片地址
                .build();
        return imageRequest;
    }

    public static ImageRequest getImageRequest(String uri) {
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(parseUrl(uri)))
                .setAutoRotateEnabled(true)//自动旋转图片方向
                .setImageDecodeOptions(getImageDecodeOptions())//  图片解码库
                .build();
        return  imageRequest;
    }

    public static GenericDraweeHierarchy getHierarchy(Context context,  boolean overlay){
        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(context.getResources());
        RoundingParams roundingParams = new RoundingParams();
//        roundingParams.setCornersRadii(5, 5, 5, 5);
        roundingParams.setBorder(ContextCompat.getColor(context, R.color.color_mask_lighter), 1);
//        roundingParams.setRoundAsCircle(true);
        GenericDraweeHierarchy hierarchy = builder
                .setPressedStateOverlay(ContextCompat.getDrawable(context, R.color.color_mask_lighter))
                .setPlaceholderImage(ContextCompat.getDrawable(context, R.color.folder_bg_gray))
                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .setRoundingParams(roundingParams)
                .build();
        return hierarchy;
    }

    public static String parseUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("file://")) {
            return url;
        }
        return "file://"+url;
    }

    public static DraweeController getDraweeController(SimpleDraweeView view, String uri, int w, int h) {
        ImageRequest request = getImageRequest(view, uri, w, h);
        return getDraweeController(request, view);
    }

    public static DraweeController getDraweeController(ImageRequest imageRequest, SimpleDraweeView view){
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
//            .reset()//重置
                .setAutoPlayAnimations(true)//自动播放图片动画
//            .setCallerContext(callerContext)//回调
//                .setControllerListener(view.getListener())//监听图片下载完毕等
//            .setDataSourceSupplier(dataSourceSupplier)//数据源
//            .setFirstAvailableImageRequests(firstAvailableImageRequests)//本地图片复用，可加入ImageRequest数组
                .setImageRequest(imageRequest)//设置单个图片请求～～～不可与setFirstAvailableImageRequests共用，配合setLowResImageRequest为高分辨率的图
//            .setLowResImageRequest(ImageRequest.fromUri(lowResUri))//先下载显示低分辨率的图
                .setOldController(view.getController())//DraweeController复用
                .setTapToRetryEnabled(true)//点击重新加载图
                .build();
        return draweeController;
    }

    private static int width = 0;
    private static int height = 0;

    private static void getDisplay(Context context) {
        if (width <= 0 || height <= 0) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            width = dm.widthPixels;
            height = dm.heightPixels;
        }
    }

    public static int getScreenWidth(Context context) {
        getDisplay(context);
        return width;
    }

    public static int getScreenHeight(Context context) {
        getDisplay(context);
        return height;
    }

    public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotaingImageView(int angle , Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();;
        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }


}
