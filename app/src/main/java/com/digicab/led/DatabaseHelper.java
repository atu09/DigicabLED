package com.digicab.led;

import android.content.Context;
import android.util.Log;

import com.digicab.led.models.MediaItem;

import java.util.ArrayList;
import java.util.List;

import in.cubestack.android.lib.storm.annotation.Database;
import in.cubestack.android.lib.storm.criteria.Restriction;
import in.cubestack.android.lib.storm.criteria.Restrictions;
import in.cubestack.android.lib.storm.criteria.StormRestrictions;
import in.cubestack.android.lib.storm.service.BaseService;
import in.cubestack.android.lib.storm.service.StormService;

@Database(name = "Digicab", tables = {MediaItem.class})
public class DatabaseHelper {

    private StormService service;
    private String TAG = "database";

    public DatabaseHelper(Context context) {
        this.service = new BaseService(context, DatabaseHelper.class);
    }

    public void saveData(MediaItem media) {
        try {
            service.save(media);
        } catch (Exception e) {
            e.printStackTrace();
            checkLog(TAG, e.getMessage(), e.getCause());
        }
    }

    public void saveList(List<MediaItem> mediaList) {
        try {
            service.save(mediaList);
        } catch (Exception e) {
            e.printStackTrace();
            checkLog(TAG, e.getMessage(), e.getCause());
        }
    }

    public void updateData(MediaItem media) {
        try {
            service.update(media);
        } catch (Exception e) {
            e.printStackTrace();
            checkLog(TAG, e.getMessage(), e.getCause());
        }
    }

    public void deleteTable(Class<?> classInstance) {
        try {
            service.truncateTable(classInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkLog(String TAG, Object data, Throwable throwable) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.d(TAG + ">>", data.toString(), throwable);
            } else {
                Log.d(TAG + ">>", data.toString());
            }
        }
    }

    public List<MediaItem> getMediaList() {
        try {
            List<MediaItem> list = service.findAll(MediaItem.class);
            if (list != null) {
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public MediaItem getMedia(int media_id) {
        try {
            Restrictions restrictions = StormRestrictions.restrictionsFor(MediaItem.class);
            Restriction restriction = restrictions.equals("id", media_id);
            return service.find(MediaItem.class, restriction).get(0);
        } catch (Exception e) {
            e.printStackTrace();
            checkLog(TAG, e.getMessage(), e.getCause());
        }
        return null;
    }

    public List<MediaItem> getOnlineMediaList() {
        try {
            Restrictions restrictions = StormRestrictions.restrictionsFor(MediaItem.class);
            Restriction restriction1 = restrictions.isNull("download");
            Restriction restriction2 = restrictions.equals("download", "");
            Restriction restriction = restrictions.or(restriction1, restriction2);

            return service.find(MediaItem.class, restriction);
        } catch (Exception e) {
            e.printStackTrace();
            checkLog(TAG, e.getMessage(), e.getCause());
        }
        return new ArrayList<>();
    }
}
