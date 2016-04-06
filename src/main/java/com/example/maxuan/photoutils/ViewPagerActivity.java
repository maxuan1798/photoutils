/**
 * ****************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
package com.example.maxuan.photoutils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Lock/Unlock button is added to the ActionBar.
 * Use it to temporarily disable ViewPager navigation in order to correctly interact with ImageView by gestures.
 * Lock/Unlock state of ViewPager is saved and restored on configuration changes.
 *
 * Julia Zudikova
 */

public class ViewPagerActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener{

    public static void BrowserPhotos(Activity activity, ArrayList<Photo> photos, int index, int maxCount, SparseArray<Photo> selectedPhotos) {
        Intent intent = new Intent(activity, ViewPagerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray("selectedPhotos", selectedPhotos);
        bundle.putParcelableArrayList("photos", photos);
        bundle.putInt("position", index);
        bundle.putInt("maxCount", index);
        intent.putExtra("data", bundle);
        activity.startActivityForResult(intent, REQUEST_PAGE_SELECT_CODE);
    }
    public static final int REQUEST_PAGE_SELECT_CODE = 12;
    private static final String ISLOCKED_ARG = "isLocked";
    private ViewPager mViewPager;
    private ImageView cancelBtn;
    private RecyclerView recyclerView;
    private CheckBox selectCheckBox;
    private Button completeBtn;
    private int position;
    PhotoPagerAdapter adapter;
    SelectedPhotoAdapter selectedPhotoAdapter;
    ArrayList<Photo> photos;
    SparseIntArray posArray;
    SparseArray<Photo> selectedPhotoArray;
    private int maxCount = 9;
    private final String format = "(%d/9)完成";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_viewpager);
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        cancelBtn = (ImageView) findViewById(R.id.selectCancel);
        cancelBtn.setOnClickListener(this);
        selectCheckBox = (CheckBox) findViewById(R.id.checkbox);
        selectCheckBox.setOnClickListener(this);
        completeBtn = (Button) findViewById(R.id.completeBtn);
        completeBtn.setOnClickListener(this);
        photos = new ArrayList<>();
        posArray = new SparseIntArray(10);
        adapter = new PhotoPagerAdapter(this, photos);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(this);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra("data");
            if (bundle == null) return;
            selectedPhotoArray = bundle.getSparseParcelableArray("selectedPhotos");
            completeBtn.setText(String.format(format, selectedPhotoArray.size()));
            List<Photo> list = bundle.getParcelableArrayList("photos");
            if (list != null) photos.addAll(list);
            for (int i=0 ; i < photos.size(); ++i) {
                posArray.put(photos.get(i).id, i);
            }
            position = bundle.getInt("position", 0);
            maxCount = bundle.getInt("maxCount", 9);
            adapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(position);
            selectCheckBox.setChecked(selectedPhotoArray.indexOfKey(photos.get(position).id) >= 0);
        }

        selectedPhotoAdapter = new SelectedPhotoAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(selectedPhotoAdapter);
        recyclerView.setVisibility(selectedPhotoArray.size() > 0 ? View.VISIBLE : View.GONE);
        if (savedInstanceState != null) {
            boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
            ((HackyViewPager) mViewPager).setLocked(isLocked);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    private void handleSelectedPhotoView(final boolean show) {
        recyclerView.animate()
                .translationY(0)
                .alpha(show?1.0f:0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        recyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.selectCancel) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("放弃选择？")
                    .setPositiveButton("放弃", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }).create().show();
        }else if (v.getId() == R.id.checkbox) {
            Photo photo = photos.get(mViewPager.getCurrentItem());
            int pos = selectedPhotoArray.indexOfKey(photo.id);
            if (selectCheckBox.isChecked() && pos < 0) {
                if (selectedPhotoArray.size() < 9) {
                    selectedPhotoArray.append(photo.id, photo);
                    selectedPhotoAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(selectedPhotoArray.indexOfKey(photo.id));
                }else{
                    Toast.makeText(this, "最多选择九张图片！", Toast.LENGTH_SHORT).show();
                    selectCheckBox.setChecked(false);
                }
            }else if (!selectCheckBox.isChecked() && pos >= 0) {
                selectedPhotoArray.remove(photo.id);
                selectedPhotoAdapter.notifyItemRemoved(pos);
//                recyclerView.scrollToPosition(pos>0?pos-1:0);
            }
            completeBtn.setText(String.format(format, selectedPhotoArray.size()));
            handleSelectedPhotoView(selectedPhotoArray.size() != 0);
        }else if (v.getId() == R.id.completeBtn) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSparseParcelableArray("selectedPhotos", selectedPhotoArray);
            intent.putExtra("data", bundle);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Photo photo = photos.get(position);
        int pos = selectedPhotoArray.indexOfKey(photo.id);
        selectCheckBox.setChecked(pos >= 0);
        if (selectedPhotoAdapter != null && pos >=0 ) {
            selectedPhotoAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(pos);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public class SelectedPhotoAdapter extends RecyclerView.Adapter<SelectedPhotoViewHolder> {

        LayoutInflater inflater;

        public SelectedPhotoAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public SelectedPhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SelectedPhotoViewHolder(inflater.inflate(R.layout.layout_image, parent, false));
        }

        @Override
        public void onBindViewHolder(SelectedPhotoViewHolder holder, int position) {
            Photo photo = selectedPhotoArray.valueAt(position);
            holder.image.setController(PhotoUtils.getDraweeController(holder.image, photo.url, 160, 160));
            holder.sign.setVisibility(photos.get(mViewPager.getCurrentItem()).id == photo.id ? View.GONE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return selectedPhotoArray.size();
        }
    }

    public class SelectedPhotoViewHolder extends RecyclerView.ViewHolder {

        public SimpleDraweeView image;
        public View sign;

        public SelectedPhotoViewHolder(View itemView) {
            super(itemView);
            image = (SimpleDraweeView) itemView.findViewById(R.id.image);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Photo photo = selectedPhotoArray.valueAt(getAdapterPosition());
                    int pos = posArray.get(photo.id);
                    mViewPager.setCurrentItem(pos, false);
                }
            });
            sign = itemView.findViewById(R.id.sign);
        }
    }

    public class PhotoPagerAdapter extends PagerAdapter implements PhotoViewAttacher.OnViewTapListener{

        Context context;
        List<Photo> photoList;

        public PhotoPagerAdapter(Context context, ArrayList<Photo> list) {
            this.photoList = list;
            this.context = context;
        }

        @Override
        public int getCount() {
            return photoList == null ? 0 : photoList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            photoView.setImageUri(PhotoUtils.parseUrl(photoList.get(position).url));
            photoView.setOnViewTapListener(this);
            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void onViewTap(View view, float x, float y) {
            if (selectedPhotoArray.size() > 0)
            handleSelectedPhotoView(recyclerView.getVisibility() != View.VISIBLE);
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
