package com.youaiyihu.disklrucache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class MainActivity extends AppCompatActivity {

    private String data = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button1)
    public void getGDataFromServer(View v) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://gank.io/api/data/福利/")
                .build();

        DiskService diskService = retrofit.create(DiskService.class);
        Call<ResponseBody> call = diskService.getData(10, 1);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    data = response.body().string();
                    //  Log.e("lxx", response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private static final String KEY = "home";
    private DiskLruCache mDiskLruCache = null;

    @OnClick(R.id.button2)
    public void saveToCache(View v) {

        try {
            File cacheDir = getCacheDir(getApplication(), "mydata");
            if (!cacheDir.exists()) {
                cacheDir.mkdir();
            }
            mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(this), 1, 1024 * 1024 * 10);
            DiskLruCache.Editor edit = mDiskLruCache.edit(KEY);

            if (edit != null) {
                edit.set(0, data);
                edit.commit();
            }
            mDiskLruCache.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String strData = "";

    @OnClick(R.id.button3)
    public void readCache(View v) {
        try {
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(KEY);
            if (snapShot != null) {
                strData = snapShot.getString(0);
                Toast.makeText(this, strData, Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ACache mACache = null;

    @OnClick(R.id.button4)
    public void saveToACache(View v) {


        mACache = ACache.get(MainActivity.this, 10 * 1024 * 1024, 100);

        mACache.put("data", data, ACache.TIME_HOUR*2);
    }


    @OnClick(R.id.button5)
    public void readFromACache(View v) {
        strData = "";
        strData = mACache.getAsString("data");
        Toast.makeText(this, strData, Toast.LENGTH_LONG).show();

    }

    /**
     * 获取缓存的路径
     *
     * @param context
     * @param uniqueName
     * @return
     */
    private File getCacheDir(Context context, String uniqueName) {
        String cachePath;
//        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
//                || !Environment.isExternalStorageRemovable()) {
//            cachePath = getExternalCacheDir().getPath();
//        } else {
        cachePath = context.getCacheDir().getPath();
        //  }

        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 获取app 版本号
     *
     * @param context
     * @return
     */
    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }


    public interface DiskService {
        @GET("{count}/{page}")
        Call<ResponseBody> getData(
                @Path("count") int count,
                @Path("page") int page
        );
    }


}
