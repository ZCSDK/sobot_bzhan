package com.sobot.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.sobot.chat.adapter.base.SobotBasePagerAdapter;
import com.sobot.chat.api.model.ZhiChiUploadAppFileModelResult;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotBitmapUtil;
import com.sobot.chat.widget.gif.GifView;
import com.sobot.chat.widget.photoview.PhotoView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by jinxl on 2017/4/10.
 */

public class SobotImageScaleAdapter extends SobotBasePagerAdapter<ZhiChiUploadAppFileModelResult> {

    public SobotImageScaleAdapter(Context context, ArrayList<ZhiChiUploadAppFileModelResult> list) {
        super(context, list);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (!TextUtils.isEmpty(list.get(position).getFileLocalPath())
                && (list.get(position).getFileLocalPath().endsWith(".gif") || list.get(position).getFileLocalPath().endsWith(".GIF"))) {
            GifView gifView = new GifView(context);
            showGif(list.get(position).getFileLocalPath(),gifView,context);
            container.addView(gifView);
            return gifView;
        } else {
            PhotoView imageView = new PhotoView(context);
            SobotBitmapUtil.display(context, list.get(position).getFileLocalPath(), imageView);
            //将ImageView加入到ViewPager中
            container.addView(imageView);
            return imageView;
        }

    }

    private void showGif(String savePath,GifView gifView,Context context) {
        FileInputStream in = null;
        Bitmap bitmap;
        try {
            in = new FileInputStream(savePath);
            bitmap = BitmapFactory.decodeFile(savePath);
            gifView.setGifImageType(GifView.GifImageType.COVER);
            gifView.setGifImage(in);
            int screenWidth = ScreenUtils
                    .getScreenWH(context)[0];
            int screenHeight = ScreenUtils
                    .getScreenWH(context)[1];
            int w = ScreenUtils.formatDipToPx(context,
                    bitmap.getWidth());
            int h = ScreenUtils.formatDipToPx(context,
                    bitmap.getHeight());
            if (w == h) {
                if (w > screenWidth) {
                    w = screenWidth;
                    h = w;
                }
            } else {
                if (w > screenWidth) {
                    w = screenWidth;
                    h = h * (screenWidth / w);
                } else if (h > screenHeight) {
                    w = w * (screenHeight / h);
                    h = screenHeight;
                }
            }
            LogUtils.i("bitmap" + w + "*" + h);
            gifView.setShowDimension(w, h);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    w, h);
            gifView.setLayoutParams(layoutParams);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}