package com.example.maxuan.photoutils;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class MarginDecoration2 extends RecyclerView.ItemDecoration {
  private int margin;

  public MarginDecoration2(Context context) {
    margin = context.getResources().getDimensionPixelSize(R.dimen.gallery_item_margin);
  }

  public MarginDecoration2(Context context, int id) {
    margin = context.getResources().getDimensionPixelSize(id);
  }

  @Override
  public void getItemOffsets(
      Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
      outRect.set(margin, 0, margin, margin);
  }
}