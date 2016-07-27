package com.vhall.live.watch;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

class PPTDownloadTask extends AsyncTask<String, Void, Bitmap> {
    ImageView mImageView;

    public void setView(ImageView mImageView) {
        this.mImageView = mImageView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap bitmap = null; // 待返回的结果
        String url = params[0]; // 获取URL
        URLConnection connection; // 网络连接对象
        InputStream is; // 数据输入流
        try {
            connection = new URL(url).openConnection();
            is = connection.getInputStream(); // 获取输入流
            BufferedInputStream buf = new BufferedInputStream(is);
            // 解析输入流
            bitmap = BitmapFactory.decodeStream(buf);
            is.close();
            buf.close();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (mImageView != null) {
            if (mImageView.getVisibility() != View.VISIBLE)
                mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(result);
        }
    }

}
