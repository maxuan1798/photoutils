package com.example.maxuan.photoutils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by asus on 2015/12/30.
 */
public class PictureSelectActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    public static void openGallery(Activity activity, int maxCount) {
        Intent intent = new Intent(activity, PictureSelectActivity.class);
        intent.putExtra(MAX_COUNT, maxCount);
        activity.startActivityForResult(intent, GALLARY_REQUEST);
    }

    public static List<String> onPhotoSelected(int requestCode, int resultCode, Intent data) {
        List<String> photos = new ArrayList<>();
        if (requestCode == GALLARY_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                List<String> lists = data.getStringArrayListExtra(SELECTED_PHOTOS);
                if (lists != null && !lists.isEmpty()) photos.addAll(lists);
            }
        }
        return photos;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ViewPagerActivity.REQUEST_PAGE_SELECT_CODE) {
            if (resultCode != RESULT_OK || data == null) return;
            Bundle bundle = data.getBundleExtra("data");
            if (bundle != null) {
                mSelectedArray = bundle.getSparseParcelableArray("selectedPhotos");
                galleryAdapter.notifyDataSetChanged();
            }
        }
    }

    public static final int GALLARY_REQUEST = 25;
    public static final String SELECTED_PHOTOS = "photos";
    public static final String MAX_COUNT = "max_count";
    private static final int IMAGE_LOADER = 0;

    private ArrayList<String> mFolders;
    private ArrayList<Photo> mPhotos;
    private SparseArray<Photo> mSelectedArray;
    private LinkedHashMap<String, ArrayList<Photo>> mFoldersMap = new LinkedHashMap<String, ArrayList<Photo>>();

    private int mMaxSelectCount = 9;
    String mDefaultFolder = "所有图片";

    private RecyclerView recyclerGallery;
    private RecyclerView recyclerFolder;

    TextView mFolderName;
    TextView mPreviewBtn;
    FrameLayout mFolderLayout;

    private PhotoGalleryAdapter galleryAdapter;
    private GridLayoutManager manager;

    private FolderAdapter folderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.str_photoutils_photo_select);
        setContentView(R.layout.layout_photo_select);

        mMaxSelectCount = getIntent().getIntExtra(MAX_COUNT, 9);

        initView();

        initData();

        manager = new GridLayoutManager(this, 3);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position == 0) ? manager.getSpanCount() : 1;
            }
        });
        recyclerGallery.addItemDecoration(new MarginDecoration(this));
        recyclerGallery.setLayoutManager(manager);
        recyclerGallery.getRecycledViewPool().setMaxRecycledViews(galleryAdapter.VIEW_ITEM, 20);
        recyclerGallery.setAdapter(galleryAdapter);

        recyclerFolder.addItemDecoration(new MarginDecoration2(this, R.dimen.folder_item_margin));
        recyclerFolder.setLayoutManager(new LinearLayoutManager(this));
        recyclerFolder.setAdapter(folderAdapter);

        //从本地加载图片信息
        getSupportLoaderManager().initLoader(IMAGE_LOADER, null, this);
    }

    private void initView() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.color_bar));
        recyclerGallery = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerFolder = (RecyclerView) findViewById(R.id.recycler_folder);
        mFolderName = (TextView) findViewById(R.id.folderName);
        mFolderName.setOnClickListener(this);
        mPreviewBtn = (TextView) findViewById(R.id.preView);
        mPreviewBtn.setOnClickListener(this);
        mFolderLayout = (FrameLayout) findViewById(R.id.folderLayout);
    }

    private void initData() {
        mFolders = new ArrayList<>();
        mPhotos = new ArrayList<>();
        mSelectedArray = new SparseArray<Photo>(10);
        galleryAdapter = new PhotoGalleryAdapter(this);
        folderAdapter = new FolderAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_complete, menu);
        MenuItem item = menu.findItem(R.id.complete);
        Button completeBtn = (Button) item.getActionView().findViewById(R.id.completeBtn);
        completeBtn.setOnClickListener(this);
        String format = "完成(%d/%d)";
        completeBtn.setText(String.format(format, mSelectedArray.size(), mMaxSelectCount));
        completeBtn.setEnabled(mSelectedArray.size() > 0);
        String formatPreview = "预览(%d)";
        mPreviewBtn.setText(String.format(formatPreview, mSelectedArray.size()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFolderList() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.listview_fade_in);
        mFolderLayout.startAnimation(fadeIn);
        mFolderLayout.setVisibility(View.VISIBLE);
        mFolderName.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.icon_down), null);
    }

    private void hideFolderList() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.listview_fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFolderLayout.setVisibility(View.INVISIBLE);
                mFolderName.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_up), null);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mFolderLayout.startAnimation(fadeOut);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case IMAGE_LOADER:
                String[] projection = {MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME};
                String selection = "";
                String[] selectionArgs = null;
                return new CursorLoader(
                        this,   // Parent activity context
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,        // Table to query
                        projection,     // Projection to return
                        selection,            // No selection clause
                        selectionArgs,            // No selection arguments
                        MediaStore.MediaColumns.DATE_ADDED + " DESC"             // Default sort order
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!mFoldersMap.isEmpty()) return;
        if(loader == null || data == null) return;
        mFolders.clear();
        mFoldersMap.clear();
        mPhotos.clear();
        ArrayList<Photo> allPhoto = new ArrayList<Photo>();
        mFolders.add(mDefaultFolder);

        while (data.moveToNext()) {
            String s0 = data.getString(0);
            String s1 = data.getString(1);
            String s2 = data.getString(2);

            String s = String.format("%s,%s,%s", s0, s1, s2);
//            Log.i("TAG", "sss " + s);
            if (s1.endsWith(".png") || s1.endsWith(".jpg") || s1.endsWith(".PNG") || s1.endsWith(".JPG") || s1.endsWith(".jpeg") || s1.endsWith(".JPEG")) {
                s1 = "file://" + s1;
            }else{
                continue;
            }

            Photo imageInfo = new Photo(Integer.valueOf(s0), s1);
            ArrayList<Photo> value = mFoldersMap.get(s2);
            if (value == null) {
                value = new ArrayList<Photo>();
                mFoldersMap.put(s2, value);
                mFolders.add(s2);
            }
            value.add(imageInfo);
            allPhoto.add(imageInfo);
        }
        mFoldersMap.put(mDefaultFolder, allPhoto);
        mPhotos.addAll(allPhoto);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                galleryAdapter.notifyDataSetChanged();
                String folderName = mFolders.get(0);
                mFolderName.setText(folderName);
                folderAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.folderName) {
            if (mFolderLayout.getVisibility() == View.VISIBLE) {
                hideFolderList();
            }else{
                showFolderList();
            }
        }else if (v.getId() == R.id.completeBtn) {
            ArrayList<String> photoList = new ArrayList<>();
            int i, size = mSelectedArray.size();
            for (i = 0; i < size; ++i) {
                photoList.add(mSelectedArray.valueAt(i).url);
            }
            Intent intent = new Intent();
            intent.putStringArrayListExtra(SELECTED_PHOTOS, photoList);
            setResult(RESULT_OK, intent);
            finish();
        }else if (v.getId() == R.id.preView) {
            if (mSelectedArray.size() == 0) return;
            ArrayList<Photo> prePhotos = new ArrayList<>();
            int i, size = mSelectedArray.size();
            for (i = 0; i < size; ++i) {
                prePhotos.add(mSelectedArray.valueAt(i));
            }
            ViewPagerActivity.BrowserPhotos(this, prePhotos, 0, size, mSelectedArray);
        }
    }

    public class FolderAdapter extends RecyclerView.Adapter<FolderViewHolder> {

        LayoutInflater inflater;

        public FolderAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FolderViewHolder(inflater.inflate(R.layout.layout_folder_item, parent, false));
        }

        @Override
        public void onBindViewHolder(FolderViewHolder holder, int position) {
            String folderName = mFolders.get(position);
            holder.folderName.setText(folderName);
            boolean empty = mFoldersMap.get(folderName).isEmpty();
            if (!empty) {
                String folderImage = mFoldersMap.get(folderName).get(0).url;
                holder.folderImage.setController(PhotoUtils.getDraweeController(holder.folderImage, folderImage, 100, 100));
            }
        }

        @Override
        public int getItemCount() {
            return mFolders.size();
        }
    }

    public class FolderViewHolder extends RecyclerView.ViewHolder {

        public SimpleDraweeView folderImage;
        public TextView folderName;

        public FolderViewHolder(View itemView) {
            super(itemView);
            folderImage = (SimpleDraweeView) itemView.findViewById(R.id.folderImage);
            folderName = (TextView) itemView.findViewById(R.id.folderName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    hideFolderList();
                    if (mDefaultFolder.equals(mFolders.get(pos))) return;
                    mDefaultFolder = mFolders.get(pos);
                    mSelectedArray.clear();
                    mPhotos.clear();
                    mPhotos.addAll(mFoldersMap.get(mDefaultFolder));
                    mFolderName.setText(mDefaultFolder);
                    galleryAdapter.notifyDataSetChanged();
                    manager.scrollToPosition(0);
                    invalidateOptionsMenu();
                }
            });
        }
    }

    public class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

        public int VIEW_HEADER = 1;
        public int VIEW_ITEM = 2;

        LayoutInflater inflater;

        private int gridWidth;
        private int headerWidth;
        private int headerHeight;

        Context context;

        public PhotoGalleryAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            int w = getResources().getDisplayMetrics().widthPixels;
            this.gridWidth = w / 4;
            this.headerWidth = w / 2;
            this.headerHeight = context.getResources().getDimensionPixelSize(R.dimen.rectangle_image_height);
            Log.d("TAG", gridWidth+" "+headerWidth);
        }

        public boolean isHeader(int position) {
            return position == 0;
        }

        @Override
        public int getItemViewType(int position) {
            return isHeader(position) ? VIEW_HEADER : VIEW_ITEM;
        }

        @Override
        public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PhotoViewHolder(inflater.inflate(
                    viewType == VIEW_HEADER
                            ? R.layout.layout_rectangle_image : R.layout.layout_square_image1, parent, false));
        }

        @Override
        public void onBindViewHolder(PhotoViewHolder holder, int position) {
            Photo photo = mPhotos.get(position);
            holder.checkBox.setChecked(mSelectedArray.indexOfKey(photo.id) >= 0);
            if (!TextUtils.isEmpty(photo.url)) {
                DraweeController controller = PhotoUtils.getDraweeController(holder.photoView, photo.url,
                        isHeader(position) ? headerWidth : gridWidth,
                        isHeader(position) ? headerHeight : gridWidth);
                controller.setHierarchy(PhotoUtils.getHierarchy(context, false));
                holder.photoView.setController(controller);
            }
        }

        @Override
        public int getItemCount() {
            return mPhotos.size();
        }

    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {

        public SimpleDraweeView photoView;
        public ImageView mask;
        public CheckBox checkBox;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            photoView = (SimpleDraweeView) itemView.findViewById(R.id.image);
            mask = (ImageView) itemView.findViewById(R.id.mask);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Photo photo = mPhotos.get(getAdapterPosition());
                    if (isChecked && mSelectedArray.indexOfKey(photo.id) < 0) {
                        if (mSelectedArray.size() >= mMaxSelectCount) {
                            Toast.makeText(getApplicationContext(), "最多选择"+mMaxSelectCount+"张图片", Toast.LENGTH_SHORT).show();
                            checkBox.setChecked(false);
                            return;
                        }
                        mSelectedArray.put(photo.id, photo);
                    }else if (!isChecked && mSelectedArray.indexOfKey(photo.id) >= 0) {
                        mSelectedArray.remove(photo.id);
                    }
                    mask.setVisibility(isChecked?View.VISIBLE:View.GONE);
                    invalidateOptionsMenu();
                }
            });
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewPagerActivity.BrowserPhotos(PictureSelectActivity.this, mPhotos, getAdapterPosition(), mMaxSelectCount, mSelectedArray);
                }
            });
        }

    }

}
