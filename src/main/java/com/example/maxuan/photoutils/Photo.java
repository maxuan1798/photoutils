package com.example.maxuan.photoutils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by maxuan on 28/2/2016.
 */
public class Photo implements Parcelable {

    public int id;
    public String url;

    public Photo(int id, String url) {
        this.id = id;
        this.url = url;
    }

    public Photo(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.url);
    }

    protected Photo(Parcel in) {
        this.id = in.readInt();
        this.url = in.readString();
    }

    public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {
        public Photo createFromParcel(Parcel source) {
            return new Photo(source);
        }

        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
}
