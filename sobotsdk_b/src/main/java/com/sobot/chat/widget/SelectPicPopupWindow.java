package com.sobot.chat.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.CustomToast;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.SobotBitmapUtil;
import com.sobot.chat.utils.ToastUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

@SuppressLint("ViewConstructor")
public class SelectPicPopupWindow extends PopupWindow {

	private Button sobot_btn_take_photo, sobot_btn_cancel;
	private View mView;
	private String imgUrl;
	private Context context;
	private String type;
	private LayoutInflater inflater;
	private String uid;

	public SelectPicPopupWindow(final Activity context,String uid){
		this.context = context;
		this.uid = uid;
		initView();
	}

	@SuppressWarnings("deprecation")
	public SelectPicPopupWindow(final Activity context,String url,String type) {
		super(context);
		imgUrl = url;
		this.type = type;
		this.context = context.getApplicationContext();
		initView();
	}

	private void initView(){
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(ResourceUtils.getIdByName(context,"layout","sobot_clear_history_dialog"), null);
		sobot_btn_take_photo = (Button) mView.findViewById(ResourceUtils.getIdByName(context,"id","sobot_btn_take_photo"));
		sobot_btn_cancel = (Button) mView.findViewById(ResourceUtils.getIdByName(context,"id","sobot_btn_cancel"));

		// ??????SelectPicPopupWindow???View
		this.setContentView(mView);
		// ??????SelectPicPopupWindow??????????????????
		this.setWidth(LayoutParams.FILL_PARENT);
		// ??????SelectPicPopupWindow??????????????????
		this.setHeight(LayoutParams.WRAP_CONTENT);
		// ??????SelectPicPopupWindow?????????????????????
		this.setFocusable(true);
		// ??????SelectPicPopupWindow????????????????????????
		this.setAnimationStyle(ResourceUtils.getIdByName(context,"style","AnimBottom"));
		// ???????????????ColorDrawable??????????????????
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		// ??????SelectPicPopupWindow?????????????????????
		this.setBackgroundDrawable(dw);
		// mMenuView??????OnTouchListener????????????????????????????????????????????????????????????????????????
		mView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				int height = mView.findViewById(ResourceUtils.getIdByName(context,"id","sobot_pop_layout")).getTop();
				int y = (int) event.getY();
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (y < height) {
						dismiss();
					}
				}
				return true;
			}
		});

		if(!TextUtils.isEmpty(imgUrl)){
			sobot_btn_take_photo.setTextColor(context.getResources()
					.getColor(ResourceUtils.getIdByName(context, "color", "sobot_color_evaluate_text_btn")));
			sobot_btn_cancel.setTextColor(context.getResources()
					.getColor(ResourceUtils.getIdByName(context, "color", "sobot_color_evaluate_text_btn")));
			// ????????????
			sobot_btn_cancel.setOnClickListener(savePictureOnClick);
			// ??????????????????
			sobot_btn_take_photo.setOnClickListener(savePictureOnClick);
		}
	}

	// ???????????????popupwindow???????????????
	private OnClickListener savePictureOnClick = new OnClickListener() {
		public void onClick(View v) {
			dismiss();
			if (v == sobot_btn_take_photo){
				LogUtils.i("imgUrl:" + imgUrl);
				if (type.equals("gif")){
					saveImageToGallery(context,imgUrl);
				}else{
					Bitmap bitmap = SobotBitmapUtil.compress(imgUrl, context, true);
					saveImageToGallery(context, bitmap);
				}
			}

			if (v == sobot_btn_cancel){

			}
		}
	};

	private void showHint(String content){
		CustomToast.makeText(context, content, 1000,
				ResourceUtils.getDrawableId(context,"sobot_iv_login_right")).show();
	}

	public void saveImageToGallery(Context context, Bitmap bmp) {
		if(!CommonUtils.isSdCardExist()){
			ToastUtil.showToast(context,"???????????????sd????????????");
			return;
		}
		if (bmp == null){
			ToastUtil.showToast(context, "??????????????????????????????");
			return;
		}
		String savePath = CommonUtils.getSDCardRootPath(context);
		// ??????????????????
		File appDir = new File(savePath, "sobot_pic");
		if (!appDir.exists()) {
			appDir.mkdirs();
		}
		String fileName = System.currentTimeMillis() + ".jpg";
		File file = new File(appDir, fileName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			ToastUtil.showToast(context, "??????????????????????????????");
			e.printStackTrace();
		} catch (IOException e) {
			ToastUtil.showToast(context, "????????????");
			e.printStackTrace();
		}catch (Exception e){
			ToastUtil.showToast(context, "????????????");
			e.printStackTrace();
		}

		notifyUpdatePic(file, fileName);
	}

	public void saveImageToGallery(Context context, String bmp) {
		if(!CommonUtils.isSdCardExist()){
			ToastUtil.showToast(context,"???????????????sd????????????");
			return;
		}
		if (TextUtils.isEmpty(bmp)){
			ToastUtil.showToast(context, "??????????????????????????????");
			return;
		}
		String savePath = CommonUtils.getSDCardRootPath(context);
		// ??????????????????
		File appDir = new File(savePath, "sobot_pic");
		if (!appDir.exists()) {
			appDir.mkdirs();
		}
		String fileName = System.currentTimeMillis() + ".gif";
		File file = new File(appDir, fileName);
		if(fileChannelCopy(new File(bmp),file)){
			notifyUpdatePic(file,fileName);
		}
	}

	// ????????????????????????
	public void notifyUpdatePic(File file, String fileName) {
		try {
			if (file != null && file.exists() && !TextUtils.isEmpty(fileName)) {
				MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.fromFile(file);
		intent.setData(uri);
		context.sendBroadcast(intent);
		showHint(ResourceUtils.getResString(context, "sobot_already_save_to_picture"));
	}

	/**
	 * ???????????????????????????????????????
	 *
	 * @param s
	 *            ?????????
	 * @param t
	 *            ?????????????????????
	 */
	public boolean fileChannelCopy(File s, File t) {
		boolean isSuccess = true;
		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			fi = new FileInputStream(s);
			fo = new FileOutputStream(t);
			in = fi.getChannel();//???????????????????????????
			out = fo.getChannel();//???????????????????????????
			in.transferTo(0, in.size(), out);//??????????????????????????????in???????????????????????????out??????
		} catch (IOException e) {
			isSuccess = false;
			ToastUtil.showToast(context, "????????????!");
			e.printStackTrace();
		} finally {
			try {
				if(fi!=null){
					fi.close();
				}
				if(in!=null){
					in.close();
				}
				if(fo!=null){
					fo.close();
				}
				if(out!=null){
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				isSuccess = false;
			}
		}
		return true;
	}
}