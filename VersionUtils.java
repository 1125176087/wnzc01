package com.bkph.bangkedaiqian.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bkph.bangkedaiqian.MyApplication;
import com.bkph.bangkedaiqian.bean.BaseBean;
import com.bkph.bangkedaiqian.http.MyStringCallback;
import com.bkph.bangkedaiqian.http.Url;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 李成龙 on 2018/12/12.
 */
public class VersionUtils {


    private static BaseBean bean;
    private static ProgressDialog progressDialog;
    private static File file;

    public static void checkVersion(final Activity activity) {
        Map<String, String> map = new HashMap<>();
        map.put("methodName", "login");
        OkGo.<String>post(Url.selectVersion)
                .params("json",Utils.getJson(map))
                .execute(new MyStringCallback(activity) {
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        ToastUtils.showToast(activity,"当前版本已是最新版本");
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        super.onSuccess(response);
                        bean = JSONObject.parseObject(response.body(), BaseBean.class);
                        if (Utils.getVersionCode() < bean.versionCode) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                                    .setTitle("提示")
                                    .setMessage("发现新版本，请点击下载\n" + bean.versionName)
                                    .setPositiveButton("下载", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            downLoadFile(bean.url,activity);
                                        }
                                    });
                            builder.setCancelable(false);
                            builder.show();
                        } else {
                            ToastUtils.showToast(activity,"当前版本已是最新版本");
                        }

                    }
                });

    }


    public static void downLoadFile(String url, final Activity activity) {

        OkGo.<File>post(url).execute(new FileCallback("bangkepuhui" + bean.versionName + ".apk") {
            @Override
            public void onStart(Request<File, ? extends Request> request) {
                super.onStart(request);
                progressDialog = new ProgressDialog(activity);
                progressDialog.setTitle("正在下载...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();

            }

            @Override
            public void downloadProgress(Progress progress) {
                int a = (int) (progress.currentSize * 100 / progress.totalSize);

                progressDialog.setProgress(a);
            }

            @Override
            public void onError(Response<File> response) {
                super.onError(response);
                ToastUtils.showToast(activity,MyApplication.ERROR_WIFI);
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(Response<File> response) {
                file = response.body();
                finishNotify(activity);
                progressDialog.dismiss();
            }
        });
    }


    public static void finishNotify(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri data;
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // "net.csdn.blog.ruancoder.fileprovider"即是在清单文件中配置的authorities
            data = FileProvider.getUriForFile(activity, "com.bkph.bangkedaiqian.fileProvider", file);
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            data = Uri.fromFile(file);
        }
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(data, type);
        activity.startActivity(intent);

    }
}
