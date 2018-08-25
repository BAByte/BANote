package com.waterteam.musicproject.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.waterteam.musicproject.R;

/**
 * Created by BA on 2018/2/12 0012.
 *
 * @Function : 从BitMap提取颜色，并且给View设置颜色
 */

public class PaletteUtil {
    private static final String TAG = "PaletteUtil";
    private Palette.Builder builder;


    public PaletteUtil from(Bitmap bitmap) {
        builder = Palette.from(bitmap);
        builder.maximumColorCount(8) // 构建Palette时使用的最大颜色数，默认是16，风景图推荐取值8-16，人脸图像推荐取值24-32（值越大，花费的时间越长，可选择的色彩越多）
                .setRegion(0, 0, bitmap.getWidth(), 10); // 设置Palette颜色分析的图像区域
        return this;
    }

    public PaletteUtil from(ImageView imageView) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable(); // mIvPic是设置了src的ImageView控件
        if (drawable!=null) {
            Bitmap bitmap = drawable.getBitmap();
            builder = Palette.from(bitmap);

            builder = Palette.from(bitmap);
            builder.maximumColorCount(16) // 构建Palette时使用的最大颜色数，默认是16，风景图推荐取值8-16，人脸图像推荐取值24-32（值越大，花费的时间越长，可选择的色彩越多）
                    .setRegion(0, 0, bitmap.getWidth(), 10); // 设置Palette颜色分析的图像区域
        }
        return this;
    }

    private void start(final View view, final int mode) {
        builder.generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int color = Color.BLACK;
                switch (mode) {
                    // 获得 鲜艳 的色值
//                    int vibrantColor =

                    case 0:
                        // 获得 暗、柔和 的色值
                        color = palette.getDarkMutedColor(Color.BLACK);
                        break;
                    case 1:
                        // 获得 亮、柔和 的色值
                        color = palette.getDarkVibrantColor(Color.BLACK);
                        break;
                    case 2:
                        // 获得 暗、鲜艳 的色值
                        color = palette.getLightVibrantColor(Color.BLACK);
                        break;
                    case 3:
                        // 获得 亮、鲜艳 的色值
                        color = palette.getMutedColor(Color.BLACK);
                        break;
                    case 4:
                        // 获得 柔和 的色值
                        color = palette.getVibrantColor(Color.BLACK);
                        break;
                    case 5:
                        // 获取某种色调的样品,知道获取到为止
                        Palette.Swatch darkMutedSwatch;
                        Log.d(TAG, "onGenerated: 1p");
                        darkMutedSwatch = palette.getVibrantSwatch();
                        if (darkMutedSwatch == null) {
                            Log.d(TAG, "onGenerated: 2p");
                            darkMutedSwatch = palette.getLightVibrantSwatch();
                        }

                        if (darkMutedSwatch == null) {
                            Log.d(TAG, "onGenerated: 3p");
                            darkMutedSwatch = palette.getDarkVibrantSwatch();
                        }

                        if (darkMutedSwatch == null) {
                            Log.d(TAG, "onGenerated: 4p");
                            darkMutedSwatch = palette.getMutedSwatch();
                        }

                        if (darkMutedSwatch==null){
                            Log.d(TAG, "onGenerated: 5p");
                            darkMutedSwatch = palette.getLightMutedSwatch();
                        }

                        if (darkMutedSwatch==null){
                            Log.d(TAG, "onGenerated: 6p");
                            darkMutedSwatch=palette.getDarkMutedSwatch();
                        }

                        if (darkMutedSwatch != null) {
                            // 获取图片的整体颜色rgb混合值---主色调
                            int rgb = darkMutedSwatch.getRgb();
                            int red = Color.red(rgb);
                            int green = Color.green(rgb);
                            int blue = Color.blue(rgb);
                            int alpha = Color.alpha(rgb);
                            color = Color.argb(alpha, red, green, blue);
                        } else {
                            Log.d(TAG, "onGenerated: 7p");
                            color = R.color.colorAccent;
                        }

                        Log.d(TAG, "onGenerated: " + darkMutedSwatch);
                        break;

                }
                view.setBackgroundColor(color);
            }
        });
    }

    public void to(View view, int mode) {

        if (builder!=null)
            start(view, mode);
    }

    public void to(View view) {
        if (builder!=null)
            start(view, 5);
    }
}
