package com.example.maxuan.photoutils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by maxuan on 9/3/2016.
 */
public class PhotoPreviewActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{

    public static void Preview(Activity activity, ArrayList<String> urls, int position) {
        Intent intent = new Intent(activity, PhotoPreviewActivity.class);
        intent.putExtra("position", position);
        intent.putStringArrayListExtra("urls", urls);
        intent.putExtra("mode", 2);
        activity.startActivityForResult(intent, REQUEST_DELETE_CODE);
    }

    public static void Preview(Context context, ArrayList<Photo> photos, int position) {
        ArrayList<String> urls = new ArrayList<>();
        for (Photo photo:photos) {
            urls.add(photo.url);
        }
        Intent intent = new Intent(context, PhotoPreviewActivity.class);
        intent.putExtra("position", position);
        intent.putStringArrayListExtra("urls", urls);
        intent.putExtra("mode", 1);
        context.startActivity(intent);
    }

    private static final String ISLOCKED_ARG = "isLocked";
    private ViewPager mViewPager;
    private TextView indicator;
    private int position;
    private int mode = 1;
    private ArrayList<String> urls;
    ImagePagerAdapter adapter;

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        indicator.setText(String.format("%d/%d", position+1, urls.size()));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public static enum Mode{PREVIEW_ONLY, DELETE}
    public static final int REQUEST_DELETE_CODE = 13;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        indicator = (TextView) findViewById(R.id.textIndicator);
        urls = new ArrayList<>();
        adapter = new ImagePagerAdapter();
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(this);
        if (getIntent() != null) {
            position = getIntent().getIntExtra("position", 0);
            mode = getIntent().getIntExtra("mode", 1);
            ArrayList<String> list = getIntent().getStringArrayListExtra("urls");
            if (list != null) urls.addAll(list);
            adapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(position);
        }
        indicator.setVisibility(urls.size() > 1 ? View.VISIBLE : View.GONE);
        indicator.setText(String.format("%d/%d", 1, urls.size()));
        if (savedInstanceState != null) {
            boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
            ((HackyViewPager) mViewPager).setLocked(isLocked);
        }

        ActionBar actionBar = getSupportActionBar();
        if (mode == 1) {
            actionBar.hide();
        }else{
            setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
        }
    }

    private void onResult() {
        if (mode == 2) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("urls", urls);
            setResult(RESULT_OK, intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mode == 2) getMenuInflater().inflate(R.menu.menu_preview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("确定删除？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("删除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    urls.remove(mViewPager.getCurrentItem());
                    if (urls.isEmpty()) {
                        onResult();
                        finish();
                    }
                    adapter.notifyDataSetChanged();
                }
            }).create().show();
            return true;
        }else if (item.getItemId() == android.R.id.home){
            onResult();
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ImagePagerAdapter extends PagerAdapter implements PhotoViewAttacher.OnPhotoTapListener{

        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            photoView.setImageUri(urls.get(position));
            if (mode == 1) photoView.setOnPhotoTapListener(this);
            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void onPhotoTap(View view, float x, float y) {
            finish();
        }
    }

    private boolean isViewPagerActive() {
        return (mViewPager != null && mViewPager instanceof HackyViewPager);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (isViewPagerActive()) {
            outState.putBoolean(ISLOCKED_ARG, ((HackyViewPager) mViewPager).isLocked());
        }
        super.onSaveInstanceState(outState);
    }

}

