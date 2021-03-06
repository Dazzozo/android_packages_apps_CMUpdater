/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 */

package com.cyanogenmod.updater.misc;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.cyanogenmod.updater.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public class UpdateInfo implements Parcelable, Serializable {
    private static final long serialVersionUID = 5499890003569313403L;

    public enum Type {
        UNKNOWN,
        STABLE,
        RC,
        SNAPSHOT,
        NIGHTLY
    };

    private String mUiName;
    private String mFileName;
    private String mVersion;
    private Type mType;
    private long mBuildDate;
    private String mDownloadUrl;
    private String mMd5Sum;

    private Boolean mIsNewerThanInstalled;

    public UpdateInfo(String fileName, long date, String url,
            String md5, Type type) {
        initializeName(fileName);
        mBuildDate = date;
        mDownloadUrl = url;
        mMd5Sum = md5;
        mType = type;
    }

    public UpdateInfo(String fileName) {
        this(fileName, 0, null, null, Type.UNKNOWN);
    }

    private UpdateInfo(Parcel in) {
        readFromParcel(in);
    }

    public File getChangeLogFile(Context context) {
        return new File(context.getCacheDir(), mFileName + ".changelog");
    }

    /**
     * Get name for UI display
     */
    public String getName() {
        return mUiName;
    }

    /**
     * Get version
     */
    public String getVersion() {
        return mVersion;
    }

    /**
     * Get file name
     */
    public String getFileName() {
        return mFileName;
    }

    /**
     * Get build type
     */
    public Type getType() {
        return mType;
    }

   /**
     * Get MD5
     */
    public String getMD5Sum() {
        return mMd5Sum;
    }

    /**
     * Get build date
     */
    public long getDate() {
        return mBuildDate;
    }

    /**
     * Get download location
     */
    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public boolean isNewerThanInstalled() {
        if (mIsNewerThanInstalled != null) {
            return mIsNewerThanInstalled;
        }

        int[] installedVersion = canonicalizeVersion(Utils.getInstalledVersion(false));
        int[] ourVersion = canonicalizeVersion(mVersion);

        if (installedVersion.length < ourVersion.length) {
            installedVersion = Arrays.copyOf(installedVersion, ourVersion.length);
        } else if (installedVersion.length > ourVersion.length) {
            ourVersion = Arrays.copyOf(ourVersion, installedVersion.length);
        }

        for (int i = 0; i < ourVersion.length; i++) {
            if (ourVersion[i] > installedVersion[i]) {
                mIsNewerThanInstalled = true;
                break;
            }
            if (ourVersion[i] < installedVersion[i]) {
                mIsNewerThanInstalled = false;
                break;
            }
        }

        if (mIsNewerThanInstalled == null) {
            // Version strings match, so compare build dates.
            mIsNewerThanInstalled = mBuildDate > Utils.getInstalledBuildDate();
        }

        return mIsNewerThanInstalled;
    }

    private int[] canonicalizeVersion(String versionString) {
        String[] parts = versionString.split("\\.");
        int[] version = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            try {
                version[i] = Integer.valueOf(parts[i]);
            } catch (NumberFormatException e) {
                version[i] = 0;
            }
        }

        return version;
    }

    private void initializeName(String fileName) {
        mFileName = fileName;
        if (!TextUtils.isEmpty(fileName)) {
            mUiName = extractUiName(fileName);
            mVersion = fileName.replaceAll(".*?([0-9.]+?)-.+","$1");
        } else {
            mUiName = null;
            mVersion = null;
        }
    }

    public static String extractUiName(String fileName) {
        String deviceType = Utils.getDeviceType();
        String uiName = fileName.replaceAll("\\.zip$", "");
        return uiName.replaceAll("-" + deviceType + "-?", "");
    }

    @Override
    public String toString() {
        return "UpdateInfo: " + mFileName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof UpdateInfo)) {
            return false;
        }

        UpdateInfo ui = (UpdateInfo) o;
        return TextUtils.equals(mFileName, ui.mFileName)
                && mType.equals(ui.mType)
                && mBuildDate == ui.mBuildDate
                && TextUtils.equals(mDownloadUrl, ui.mDownloadUrl)
                && TextUtils.equals(mMd5Sum, ui.mMd5Sum);
    }

    public static final Parcelable.Creator<UpdateInfo> CREATOR = new Parcelable.Creator<UpdateInfo>() {
        public UpdateInfo createFromParcel(Parcel in) {
            return new UpdateInfo(in);
        }

        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mUiName);
        out.writeString(mFileName);
        out.writeString(mVersion);
        out.writeString(mType.toString());
        out.writeLong(mBuildDate);
        out.writeString(mDownloadUrl);
        out.writeString(mMd5Sum);
    }

    private void readFromParcel(Parcel in) {
        mUiName = in.readString();
        mFileName = in.readString();
        mVersion = in.readString();
        mType = Enum.valueOf(Type.class, in.readString());
        mBuildDate = in.readLong();
        mDownloadUrl = in.readString();
        mMd5Sum = in.readString();
    }
}
