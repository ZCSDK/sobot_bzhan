package com.sobot.chat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.sobot.chat.activity.base.SobotBaseActivity;
import com.sobot.chat.adapter.SobotFileListAdapter;
import com.sobot.chat.adapter.SobotTicketDetailAdapter;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotUserTicketEvaluate;
import com.sobot.chat.api.model.SobotUserTicketInfo;
import com.sobot.chat.api.model.StUserDealTicketInfo;
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.api.model.ZhiChiUploadAppFileModelResult;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.core.http.callback.StringResultCallBack;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CustomToast;
import com.sobot.chat.utils.FastClickUtils;
import com.sobot.chat.utils.ImageUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.MD5Util;
import com.sobot.chat.utils.MediaFileUtils;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.dialog.SobotReplySeletFileDialog;
import com.sobot.chat.widget.dialog.SobotTicketEvaluateDialog;
import com.sobot.chat.widget.kpswitch.util.KeyboardUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.documentfile.provider.DocumentFile;

public class SobotTicketDetailActivity extends SobotBaseActivity implements SobotTicketEvaluateDialog.SobotTicketEvaluateCallback, View.OnClickListener {
    public static final String INTENT_KEY_UID = "intent_key_uid";
    public static final String INTENT_KEY_COMPANYID = "intent_key_companyid";
    public static final String INTENT_KEY_TICKET_INFO = "intent_key_ticket_info";

    private String mUid = "";
    private String mCompanyId = "";
    private SobotUserTicketInfo mTicketInfo;

    private List<Object> mList = new ArrayList<>();
    private ListView mListView;
    private SobotTicketDetailAdapter mAdapter;

    private LinearLayout sobot_enclosure_container;
    private GridView sobot_post_msg_pic;
    private EditText sobot_post_reply_content_et;
    private Button sobot_btn_submit;
    private List<ZhiChiUploadAppFileModelResult> pic_list = new ArrayList<>();
    private SobotFileListAdapter adapter;
    private SobotReplySeletFileDialog menuWindow;

    /**
     * @param context 应用程序上下文
     * @return
     */
    public static Intent newIntent(Context context, String companyId, String uid, SobotUserTicketInfo ticketInfo) {
        Intent intent = new Intent(context, SobotTicketDetailActivity.class);
        intent.putExtra(INTENT_KEY_UID, uid);
        intent.putExtra(INTENT_KEY_COMPANYID, companyId);
        intent.putExtra(INTENT_KEY_TICKET_INFO, ticketInfo);
        return intent;
    }

    @Override
    protected int getContentViewResId() {
        return getResLayoutId("sobot_activity_ticket_detail");
    }

    protected void initBundleData(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mUid = getIntent().getStringExtra(INTENT_KEY_UID);
            mCompanyId = getIntent().getStringExtra(INTENT_KEY_COMPANYID);
            mTicketInfo = (SobotUserTicketInfo) getIntent().getSerializableExtra(INTENT_KEY_TICKET_INFO);
        }
    }

    @Override
    protected void initView() {
        showLeftMenu(getResDrawableId("sobot_btn_back_selector"), getResString("sobot_back"), true);
        setTitle(getResString("sobot_message_details"));
        mListView = (ListView) findViewById(getResId("sobot_listview"));
        sobot_enclosure_container = (LinearLayout) findViewById(getResId("sobot_enclosure_container"));
        sobot_post_reply_content_et = (EditText) findViewById(getResId("sobot_post_reply_content_et"));
        sobot_btn_submit = (Button) findViewById(getResId("sobot_btn_submit"));
        sobot_btn_submit.setOnClickListener(this);
        if (mTicketInfo != null) {
            if (3 != mTicketInfo.getFlag()) {
                //已完成的工单不能回复
                sobot_enclosure_container.setVisibility(View.VISIBLE);
                initPicListView();
                mListView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        KeyboardUtil.hideKeyboard(mListView);
                        return false;
                    }
                });
                sobot_enclosure_container.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        KeyboardUtil.hideKeyboard(sobot_enclosure_container);
                        return false;
                    }
                });
            } else {
                sobot_enclosure_container.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void initData() {
        if (mTicketInfo == null) {
            return;
        }
        zhiChiApi.getUserDealTicketInfoList(SobotTicketDetailActivity.this, mUid, mCompanyId, mTicketInfo.getTicketId(), new StringResultCallBack<List<StUserDealTicketInfo>>() {

            @Override
            public void onSuccess(List<StUserDealTicketInfo> datas) {
                if (datas != null && datas.size() > 0) {
                    mList.clear();
                    mList.add(mTicketInfo);
                    mList.addAll(datas);
                    if (mAdapter == null) {
                        mAdapter = new SobotTicketDetailAdapter(SobotTicketDetailActivity.this, mList);
                        mListView.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                ToastUtil.showToast(SobotTicketDetailActivity.this, des);
            }
        });
    }

    /**
     * 初始化图片选择的控件
     */
    private void initPicListView() {
        sobot_post_msg_pic = (GridView) findViewById(getResId("sobot_post_msg_pic"));
        adapter = new SobotFileListAdapter(SobotTicketDetailActivity.this, pic_list);
        adapter.setListener(new SobotFileListAdapter.onItemClickListener() {
            @Override
            public void downFileLister(SobotCacheFile cacheFile) {
                if (cacheFile != null) {
                    Intent intent = new Intent(SobotTicketDetailActivity.this, SobotFileDetailActivity.class);
                    intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void previewMp4(SobotCacheFile cacheFile) {
                if (cacheFile != null) {
                    Intent intent = SobotVideoActivity.newIntent(SobotTicketDetailActivity.this, cacheFile);
                    startActivity(intent);
                }
            }

            @Override
            public void previewPic(String imageUrL) {
                if (!TextUtils.isEmpty(imageUrL)) {
                    Intent intent = new Intent(SobotTicketDetailActivity.this, SobotPhotoActivity.class);
                    intent.putExtra("imageUrL", imageUrL);
                    startActivity(intent);
                }
            }

            @Override
            public void deleteItem(int postion) {
                pic_list.remove(postion);
                adapter.restDataView();
            }
        });
        sobot_post_msg_pic.setAdapter(adapter);
        sobot_post_msg_pic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyboardUtil.hideKeyboard(view);
                if (pic_list.get(position).getViewState() == 0) {
                    // 选择文件
                    if (!checkStoragePermission()) {
                        return;
                    }
                    menuWindow = new SobotReplySeletFileDialog(SobotTicketDetailActivity.this, itemsOnClick);
                    menuWindow.show();
                }
            }
        });
        adapter.restDataView();
    }

    // 为弹出窗口popupwindow实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        public void onClick(View v) {
            menuWindow.dismiss();
            if (v.getId() == getResId("btn_take_reply_pic")) {
                selectPicFromLocal();
            }
            if (v.getId() == getResId("btn_pick_reply_video")) {
                selectVideoFromLocal();
            }
            if (v.getId() == getResId("btn_pick_reply_file")) {
                Intent intent = new Intent(SobotTicketDetailActivity.this, SobotChooseFileActivity.class);
                startActivityForResult(intent, ZhiChiConstant.REQUEST_COCE_TO_CHOOSE_FILE);
            }
        }
    };


    @Override
    public void submitEvaluate(final int score, final String remark) {
        zhiChiApi.addTicketSatisfactionScoreInfo(SobotTicketDetailActivity.this, mUid, mCompanyId, mTicketInfo.getTicketId(), score, remark, new StringResultCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                for (int i = 0; i < mList.size(); i++) {
                    Object obj = mList.get(i);
                    if (obj instanceof StUserDealTicketInfo) {
                        StUserDealTicketInfo data = (StUserDealTicketInfo) mList.get(i);
                        if (data.getFlag() == 3 && data.getEvaluate() != null) {
                            SobotUserTicketEvaluate evaluate = data.getEvaluate();
                            evaluate.setScore(score);
                            evaluate.setRemark(remark);
                            evaluate.setEvalution(true);
                            mAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                ToastUtil.showToast(getApplicationContext(), des);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == ZhiChiConstant.REQUEST_CODE_picture) {
                    if (data != null && data.getData() != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage == null) {
                            selectedImage = ImageUtils.getUri(data, SobotTicketDetailActivity.this);
                        }
                        String path = ImageUtils.getPath(this, selectedImage);
                        if (MediaFileUtils.isVideoFileType(path)) {
                            MediaPlayer mp = new MediaPlayer();
                            try {
                                mp.setDataSource(this, selectedImage);
                                mp.prepare();
                                int videoTime = mp.getDuration();
                                if (videoTime / 1000 > 15) {
                                    ToastUtil.showToast(this, getResString("sobot_upload_vodie_length"));
                                    return;
                                }
                                SobotDialogUtils.startProgressDialog(this);
//                            ChatUtils.sendPicByFilePath(this,path,sendFileListener,false);
                                String fName = MD5Util.encode(path);
                                String filePath = null;
                                try {
                                    filePath = FileUtil.saveImageFile(this, selectedImage, fName + FileUtil.getFileEndWith(path), path);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ToastUtil.showToast(this, ResourceUtils.getResString(this, "sobot_pic_type_error"));
                                    return;
                                }
                                sendFileListener.onSuccess(filePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            SobotDialogUtils.startProgressDialog(this);
                            ChatUtils.sendPicByUriPost(this, selectedImage, sendFileListener, false);
                        }
                    } else {
                        ToastUtil.showToast(SobotTicketDetailActivity.this, getResString("sobot_did_not_get_picture_path"));
                    }
                }
            }
            if (data != null) {
                switch (requestCode) {
                    case ZhiChiConstant.REQUEST_COCE_TO_CHOOSE_FILE:
                        Uri selectedFileUri = data.getData();
                        if (null == selectedFileUri) {
                            File selectedFile = (File) data.getSerializableExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE);
                            uploadFile(selectedFile, selectedFile != null ? selectedFile.getName() : "");
                        } else {
                            if (selectedFileUri == null) {
                                selectedFileUri = ImageUtils.getUri(data, SobotTicketDetailActivity.this);
                            }
                            String path = ImageUtils.getPath(SobotTicketDetailActivity.this, selectedFileUri);
                            if (TextUtils.isEmpty(path)) {
                                ToastUtil.showToast(SobotTicketDetailActivity.this, ResourceUtils.getResString(SobotTicketDetailActivity.this, "sobot_pic_type_error"));
                                return;
                            }
                            File selectedFile = new File(path);
                            String fName = MD5Util.encode(selectedFile.getAbsolutePath());
                            String filePath = null;
                            try {
                                filePath = FileUtil.saveImageFile(SobotTicketDetailActivity.this, selectedFileUri, fName + FileUtil.getFileEndWith(selectedFile.getAbsolutePath()), selectedFile.getAbsolutePath());
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastUtil.showToast(SobotTicketDetailActivity.this, ResourceUtils.getResString(SobotTicketDetailActivity.this, "sobot_pic_type_error"));
                                return;
                            }
                            if (TextUtils.isEmpty(filePath)) {
                                ToastUtil.showToast(SobotTicketDetailActivity.this, ResourceUtils.getResString(SobotTicketDetailActivity.this, "sobot_pic_type_error"));
                                return;
                            }
                            selectedFile = new File(filePath);
                            DocumentFile documentFile = DocumentFile.fromSingleUri(SobotTicketDetailActivity.this, selectedFileUri);
                            uploadFile(selectedFile, documentFile != null ? documentFile.getName() : "");
                        }
                    default:
                        break;
                }
            }
        } catch (Exception e) {
        }
    }

    private ChatUtils.SobotSendFileListener sendFileListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(String filePath) {
            uploadFile(new File(filePath), "");
        }

        @Override
        public void onError() {
            SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
        }
    };

    protected void uploadFile(final File selectedFile, final String fileName) {
        if (selectedFile != null && selectedFile.exists()) {
            // 发送文件
            LogUtils.i(selectedFile.toString());
            SobotDialogUtils.startProgressDialog(SobotTicketDetailActivity.this);
            zhiChiApi.fileUploadForPostMsg(SobotTicketDetailActivity.this, mCompanyId, selectedFile.getPath(), new ResultCallBack<ZhiChiMessage>() {
                @Override
                public void onSuccess(ZhiChiMessage zhiChiMessage) {
                    SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
                    if (zhiChiMessage.getData() != null) {
                        ZhiChiUploadAppFileModelResult item = new ZhiChiUploadAppFileModelResult();
                        item.setFileUrl(zhiChiMessage.getData().getUrl());
                        item.setFileLocalPath(selectedFile.getPath());
                        if (!TextUtils.isEmpty(fileName)) {
                            item.setFileName(fileName);
                        } else {
                            item.setFileName(selectedFile.getName());
                        }
                        item.setFileType(ChatUtils.getFileType(selectedFile));
                        item.setViewState(1);
                        adapter.addData(item);
                    }
                }

                @Override
                public void onFailure(Exception e, String des) {
                    SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
                    ToastUtil.showToast(SobotTicketDetailActivity.this, des);
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {

                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (v == sobot_btn_submit) {
            //提交
            KeyboardUtil.hideKeyboard(sobot_btn_submit);
            if (StringUtils.isEmpty(sobot_post_reply_content_et.getText().toString().trim())) {
                Toast.makeText(SobotTicketDetailActivity.this, ResourceUtils.getResString(SobotTicketDetailActivity.this, "sobot_please_input_reply_no_empty"), Toast.LENGTH_SHORT).show();
                return;
            }
            if (FastClickUtils.isCanClick()) {
                SobotDialogUtils.startProgressDialog(SobotTicketDetailActivity.this);
                zhiChiApi.replyTicketContent(this, mUid, mTicketInfo.getTicketId(), sobot_post_reply_content_et.getText().toString(), getFileStr(), mCompanyId, new StringResultCallBack<String>() {
                    @Override
                    public void onSuccess(String s) {
                        LogUtils.e(s);
                        CustomToast.makeText(SobotTicketDetailActivity.this, ResourceUtils.getResString(SobotTicketDetailActivity.this, "sobot_submit_success_tip"), 1000, ResourceUtils.getDrawableId(SobotTicketDetailActivity.this, "sobot_iv_login_right")).show();
                        try {
                            Thread.sleep(500);//睡眠一秒  延迟拉取数据
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        pic_list.clear();
                        adapter.notifyDataSetChanged();
                        initPicListView();
                        sobot_post_reply_content_et.setText("");
                        SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
                        initData();
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        ToastUtil.showCustomToast(SobotTicketDetailActivity.this, ResourceUtils.getResString(SobotTicketDetailActivity.this, "sobot_submit_error_tip"));
                        e.printStackTrace();
                        SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
                    }
                });
            }
        }
    }

    public String getFileStr() {
        String tmpStr = "";
        ArrayList<ZhiChiUploadAppFileModelResult> tmpList = adapter.getPicList();
        for (int i = 0; i < tmpList.size(); i++) {
            tmpStr += tmpList.get(i).getFileUrl() + ";";
        }
        return tmpStr;
    }
}