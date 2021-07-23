package com.sobot.chat.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sobot.chat.activity.SobotPhotoListActivity;
import com.sobot.chat.activity.SobotPostCategoryActivity;
import com.sobot.chat.activity.SobotPostMsgActivity;
import com.sobot.chat.adapter.SobotPicListAdapter;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.model.CommonModelBase;
import com.sobot.chat.api.model.PostParamModel;
import com.sobot.chat.api.model.SobotFieldModel;
import com.sobot.chat.api.model.SobotLeaveMsgConfig;
import com.sobot.chat.api.model.SobotLeaveMsgParamModel;
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.api.model.ZhiChiUploadAppFileModelResult;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.core.http.callback.StringResultCallBack;
import com.sobot.chat.listener.ISobotCusField;
import com.sobot.chat.presenter.StCusFieldPresenter;
import com.sobot.chat.presenter.StPostMsgPresenter;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.ThankDialog;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.dialog.SobotSelectPicDialog;
import com.sobot.chat.widget.kpswitch.util.KeyboardUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 留言界面
 *
 * @author Created by jinxl on 2019/3/7.
 */
public class SobotPostMsgFragment extends SobotBaseFragment implements View.OnClickListener, ISobotCusField {
    private View mRootView;

    private EditText sobot_post_email, sobot_et_content, sobot_post_phone;
    private TextView sobot_tv_post_msg, sobot_post_email_lable, sobot_post_phone_lable, sobot_post_lable, sobot_post_question_type;
    private ImageView sobot_img_clear_email, sobot_img_clear_phone;
    private View sobot_frist_line;
    private Button sobot_btn_submit;
    private GridView sobot_post_msg_pic;
    private LinearLayout sobot_enclosure_container, sobot_post_customer_field;
    private RelativeLayout sobot_post_email_rl, sobot_post_phone_rl, sobot_post_question_rl;
    private List<ZhiChiUploadAppFileModelResult> pic_list = new ArrayList<>();
    private SobotPicListAdapter adapter;
    private SobotSelectPicDialog menuWindow;

    private ArrayList<SobotFieldModel> mFields;

    private LinearLayout sobot_post_msg_layout;

    private SobotLeaveMsgConfig mConfig;
    private String uid = "";
    private String mGroupId = "";
    private boolean flag_exit_sdk;

    private int flag_exit_type = -1;
    private ThankDialog d;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        public void handleMessage(final android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    if (flag_exit_type == 1) {
                        finishPageOrSDK(true);
                    } else if (flag_exit_type == 2) {
                        getActivity().setResult(200);
                        finishPageOrSDK(false);
                    } else {
                        finishPageOrSDK(flag_exit_sdk);
                    }
                    break;
            }
        }
    };

    public static SobotPostMsgFragment newInstance(Bundle data) {
        Bundle arguments = new Bundle();
        arguments.putBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION, data);
        SobotPostMsgFragment fragment = new SobotPostMsgFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments().getBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION);
            if (bundle != null) {
                uid = bundle.getString(StPostMsgPresenter.INTENT_KEY_UID);
                mGroupId = bundle.getString(StPostMsgPresenter.INTENT_KEY_GROUPID);
                flag_exit_type = bundle.getInt(ZhiChiConstant.FLAG_EXIT_TYPE, -1);
                flag_exit_sdk = bundle.getBoolean(ZhiChiConstant.FLAG_EXIT_SDK, false);
                mConfig = (SobotLeaveMsgConfig) bundle.getSerializable(StPostMsgPresenter.INTENT_KEY_CONFIG);
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(getResLayoutId("sobot_fragment_post_msg"), container, false);
        initView(mRootView);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initData();
        super.onActivityCreated(savedInstanceState);
    }

    protected void initView(View rootView) {
        sobot_post_phone = (EditText) rootView.findViewById(getResId("sobot_post_phone"));
        sobot_post_email = (EditText) rootView.findViewById(getResId("sobot_post_email"));
        sobot_frist_line = rootView.findViewById(getResId("sobot_frist_line"));
        sobot_et_content = (EditText) rootView.findViewById(getResId("sobot_post_et_content"));
        sobot_tv_post_msg = (TextView) rootView.findViewById(getResId("sobot_tv_post_msg"));
        sobot_post_email_lable = (TextView) rootView.findViewById(getResId("sobot_post_email_lable"));
        sobot_post_phone_lable = (TextView) rootView.findViewById(getResId("sobot_post_phone_lable"));
        sobot_post_lable = (TextView) rootView.findViewById(getResId("sobot_post_question_lable"));
        String test = "<font color='#8B98AD'>" + getResString("sobot_problem_types") + "</font>" + "<font color='#f9676f'>&#8201*</font>";
        sobot_post_lable.setText(Html.fromHtml(test));
        sobot_post_question_type = (TextView) rootView.findViewById(getResId("sobot_post_question_type"));
        sobot_post_question_type.setOnClickListener(this);
        sobot_post_msg_layout = (LinearLayout) rootView.findViewById(getResId("sobot_post_msg_layout"));
        sobot_img_clear_email = (ImageView) rootView.findViewById(getResId("sobot_img_clear_email"));
        sobot_img_clear_phone = (ImageView) rootView.findViewById(getResId("sobot_img_clear_phone"));
        sobot_img_clear_phone.setOnClickListener(this);
        sobot_img_clear_email.setOnClickListener(this);
        sobot_enclosure_container = (LinearLayout) rootView.findViewById(getResId("sobot_enclosure_container"));
        sobot_post_customer_field = (LinearLayout) rootView.findViewById(getResId("sobot_post_customer_field"));


        sobot_post_email_rl = (RelativeLayout) rootView.findViewById(getResId("sobot_post_email_rl"));
        sobot_post_phone_rl = (RelativeLayout) rootView.findViewById(getResId("sobot_post_phone_rl"));
        sobot_post_question_rl = (RelativeLayout) rootView.findViewById(getResId("sobot_post_question_rl"));
        sobot_btn_submit = (Button) rootView.findViewById(getResId("sobot_btn_submit"));
        sobot_btn_submit.setOnClickListener(this);
        sobot_post_customer_field.setVisibility(View.GONE);

        if (mConfig.isEmailShowFlag()) {
            sobot_post_email_rl.setVisibility(View.VISIBLE);
        } else {
            sobot_post_email_rl.setVisibility(View.GONE);
        }

        if (mConfig.isTelShowFlag()) {
            sobot_post_phone_rl.setVisibility(View.VISIBLE);
        } else {
            sobot_post_phone_rl.setVisibility(View.GONE);
        }

        if (mConfig.isEmailShowFlag() && mConfig.isTelShowFlag()) {
            sobot_frist_line.setVisibility(View.VISIBLE);
        } else {
            sobot_frist_line.setVisibility(View.GONE);
        }

        if (mConfig.isTelFlag()) {
            sobot_post_phone.setText(SharedPreferencesUtil.getStringData(getContext(), "sobot_user_phone", ""));
        }

        if (mConfig.isEmailFlag()) {
            sobot_post_email.setText(SharedPreferencesUtil.getStringData(getContext(), "sobot_user_email", ""));
        }

        if (mConfig.isEnclosureShowFlag()) {
            sobot_enclosure_container.setVisibility(View.VISIBLE);
            initPicListView();
        } else {
            sobot_enclosure_container.setVisibility(View.GONE);
        }

        if (mConfig.isTicketTypeFlag()) {
            sobot_post_question_rl.setVisibility(View.VISIBLE);
        } else {
            sobot_post_question_rl.setVisibility(View.GONE);
            sobot_post_question_type.setTag(mConfig.getTicketTypeId());
        }

    }

    protected void initData() {
        zhiChiApi.getTemplateFieldsInfo(SobotPostMsgFragment.this, uid, mConfig.getTemplateId(), new StringResultCallBack<SobotLeaveMsgParamModel>() {

            @Override
            public void onSuccess(SobotLeaveMsgParamModel result) {
                if (result != null) {
                    if (result.getField() != null && result.getField().size() != 0) {
                        mFields = result.getField();
                        StCusFieldPresenter.addWorkOrderCusFields(SobotPostMsgFragment.this.getContext(), mFields, sobot_post_customer_field, SobotPostMsgFragment.this);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                try {
                    showHint(getString(ResourceUtils.getResStrId(getContext(), "sobot_try_again")), false);
                } catch (Exception e1) {

                }
            }

        });

        msgFilter();
        editTextSetHint();
    }

    /**
     * 提交
     */
    private void setCusFieldValue() {
        StCusFieldPresenter.formatCusFieldVal(getContext(), sobot_post_customer_field, mFields);
        checkSubmit();
    }


    private void checkSubmit() {
        String userPhone = "", userEamil = "";

        if (sobot_post_question_rl.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(sobot_post_question_type.getText().toString())) {
                showHint(getResString("sobot_problem_types") + "  " + getResString("sobot__is_null"), false);
                return;
            }
        }

        if (mFields != null && mFields.size() != 0) {
            for (int i = 0; i < mFields.size(); i++) {
                if (1 == mFields.get(i).getCusFieldConfig().getFillFlag()) {
                    if (TextUtils.isEmpty(mFields.get(i).getCusFieldConfig().getValue())) {
                        showHint(mFields.get(i).getCusFieldConfig().getFieldName() + "  " + getResString("sobot__is_null"), false);
                        return;
                    }
                }
            }
        }

        if (TextUtils.isEmpty(sobot_et_content.getText().toString().trim())) {
            showHint(getResString("sobot_problem_description") + "  " + getResString("sobot__is_null"), false);
            return;
        }

        if (mConfig.isEnclosureShowFlag() && mConfig.isEnclosureFlag()) {
            if (TextUtils.isEmpty(getFileStr())) {
                showHint(getResString("sobot_please_load"), false);
                return;
            }
        }

        if (mConfig.isEmailShowFlag()) {
            if (mConfig.isEmailFlag()) {
                if (!TextUtils.isEmpty(sobot_post_email.getText().toString().trim())) {
                    userEamil = sobot_post_email.getText().toString().trim();
                    if (!ScreenUtils.isEmail(userEamil)) {
                        showHint(getResString("sobot_email_dialog_hint"), false);
                        return;
                    }
                } else {
                    showHint(getResString("sobot_input_email_hint"), false);
                    return;
                }
            } else {
                userEamil = sobot_post_email.getText().toString().trim();
                if (!TextUtils.isEmpty(userEamil) && !ScreenUtils.isEmail(userEamil)) {
                    showHint(getResString("sobot_email_dialog_hint"), false);
                    return;
                }
            }
        }

        if (mConfig.isTelShowFlag()) {
            if (mConfig.isTelFlag()) {
                if (!TextUtils.isEmpty(sobot_post_phone.getText().toString().trim())) {
                    userPhone = sobot_post_phone.getText().toString();
                } else {
                    showHint(getResString("sobot_input_phone_hint"), false);
                    return;
                }
            } else {
                userPhone = sobot_post_phone.getText().toString().trim();
            }
        }

        postMsg(userPhone, userEamil);
    }

    @SuppressWarnings("deprecation")
    public void showHint(String content, final boolean flag) {
        if (isAdded()) {
            KeyboardUtil.hideKeyboard(getActivity().getCurrentFocus());
            if (d != null) {
                d.dismiss();
            }
            ThankDialog.Builder customBuilder = new ThankDialog.Builder(getContext());
            customBuilder.setMessage(content);
            d = customBuilder.create();
            d.show();

            Window window = d.getWindow();
            if (window != null) {
                WindowManager.LayoutParams lp = window.getAttributes();

                float dpToPixel = ScreenUtils.dpToPixel(
                        getContext().getApplicationContext(), 1);
                lp.width = (int) (dpToPixel * 200); // 设置宽度
                d.getWindow().setAttributes(lp);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isAdded()) {
                            if (d != null) {
                                d.dismiss();
                            }
                            if (flag) {
                                handler.sendEmptyMessage(1);
                            }
                        }
                    }
                }, 2000);
            }
        }
    }

    @Override
    public void onClick(View view) {

        if (view == sobot_img_clear_email) {
            sobot_post_email.setText("");
            sobot_img_clear_email.setVisibility(View.GONE);
        }

        if (view == sobot_img_clear_phone) {
            sobot_post_phone.setText("");
            sobot_img_clear_phone.setVisibility(View.GONE);
        }

        if (view == sobot_post_question_type) {
            if (mConfig.getType() != null && mConfig.getType().size() != 0) {
                Intent intent = new Intent(getContext(), SobotPostCategoryActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("types", mConfig.getType());
                if (sobot_post_question_type != null &&
                        !TextUtils.isEmpty(sobot_post_question_type.getText().toString()) &&
                        sobot_post_question_type.getTag() != null &&
                        !TextUtils.isEmpty(sobot_post_question_type.getTag().toString())) {
                    bundle.putString("typeName", sobot_post_question_type.getText().toString());
                    bundle.putString("typeId", sobot_post_question_type.getTag().toString());
                }
                intent.putExtra("bundle", bundle);
                startActivityForResult(intent, ZhiChiConstant.work_order_list_display_type_category);
            }
        }

        if (view == sobot_btn_submit) {
            setCusFieldValue();
        }
    }

    public void onBackPressed() {
        if (flag_exit_type == 1 || flag_exit_type == 2) {
            finishPageOrSDK(false);
        } else {
            finishPageOrSDK(flag_exit_sdk);
        }
    }

    private void postMsg(String userPhone, String userEamil) {

        PostParamModel postParam = new PostParamModel();
        postParam.setTemplateId(mConfig.getTemplateId());
        postParam.setUid(uid);
        postParam.setTicketContent(sobot_et_content.getText().toString());
        postParam.setCustomerEmail(userEamil);
        postParam.setCustomerPhone(userPhone);
        postParam.setCompanyId(mConfig.getCompanyId());
        postParam.setFileStr(getFileStr());
        postParam.setGroupId(mGroupId);
        if (sobot_post_question_type.getTag() != null && !TextUtils.isEmpty(sobot_post_question_type.getTag().toString())) {
            postParam.setTicketTypeId(sobot_post_question_type.getTag().toString());
        }

        postParam.setExtendFields(StCusFieldPresenter.getSaveFieldVal(mFields));

        zhiChiApi.postMsg(SobotPostMsgFragment.this, postParam, new StringResultCallBack<CommonModelBase>() {
            @Override
            public void onSuccess(CommonModelBase base) {
                if (Integer.parseInt(base.getStatus()) == 0) {
                    showHint(base.getMsg(), false);
                } else if (Integer.parseInt(base.getStatus()) == 1) {
                    KeyboardUtil.hideKeyboard(getActivity().getCurrentFocus());
                    Intent intent = new Intent();
                    intent.setAction(SobotPostMsgActivity.SOBOT_ACTION_SHOW_COMPLETED_VIEW);
                    CommonUtils.sendLocalBroadcast(getContext(), intent);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                try {
                    showHint(getString(ResourceUtils.getResStrId(getContext(), "sobot_try_again")), false);
                } catch (Exception e1) {

                }
            }
        });
    }

    /**
     * 返回键监听
     */
    public void onBackPress() {
        if (getView() != null) {
            KeyboardUtil.hideKeyboard(((ViewGroup) getView()).getFocusedChild());
        }

        if (flag_exit_type == 1 || flag_exit_type == 2) {
            finishPageOrSDK(false);
        } else {
            finishPageOrSDK(flag_exit_sdk);
        }
    }

    private void finishPageOrSDK(boolean flag) {
        if (!flag) {
            if (getSobotActivity() == null) {
                Activity tempActivity = MyApplication.getInstance().getLastActivity();
                if (tempActivity != null && tempActivity instanceof SobotPostMsgActivity) {
                    tempActivity.finish();
                    tempActivity.overridePendingTransition(ResourceUtils.getIdByName(tempActivity
                            , "anim", "push_right_in"),
                            ResourceUtils.getIdByName(tempActivity, "anim", "push_right_out"));
                }
            } else {
                getSobotActivity().finish();
                getSobotActivity().overridePendingTransition(ResourceUtils.getIdByName(getSobotActivity()
                        , "anim", "push_right_in"),
                        ResourceUtils.getIdByName(getSobotActivity(), "anim", "push_right_out"));
            }
        } else {
            MyApplication.getInstance().exit();
        }
    }

    @Override
    public void onDestroy() {
        SobotDialogUtils.stopProgressDialog(getContext());
        if (d != null) {
            d.dismiss();
        }
        super.onDestroy();
    }

    /**
     * 初始化图片选择的控件
     */
    private void initPicListView() {
        sobot_post_msg_pic = (GridView) mRootView.findViewById(getResId("sobot_post_msg_pic"));
        adapter = new SobotPicListAdapter(getContext(), pic_list);
        sobot_post_msg_pic.setAdapter(adapter);
        sobot_post_msg_pic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyboardUtil.hideKeyboard(view);
                if (pic_list.get(position).getViewState() == 0) {
                    menuWindow = new SobotSelectPicDialog(getActivity(), itemsOnClick);
                    menuWindow.show();
                } else {
                    LogUtils.i("当前选择图片位置：" + position);
                    Intent intent = new Intent(getContext(), SobotPhotoListActivity.class);
                    intent.putExtra(ZhiChiConstant.SOBOT_KEYTYPE_PIC_LIST, adapter.getPicList());
                    intent.putExtra(ZhiChiConstant.SOBOT_KEYTYPE_PIC_LIST_CURRENT_ITEM, position);
                    startActivityForResult(intent, ZhiChiConstant.SOBOT_KEYTYPE_DELETE_FILE_SUCCESS);
                }
            }
        });
        adapter.restDataView();
    }

    //对msg过滤
    private void msgFilter() {
        if (!TextUtils.isEmpty(mConfig.getMsgTmp())) {
            mConfig.setMsgTmp(mConfig.getMsgTmp().replace("<br/>", "").replace("\n", "").replace("<p>", "").replace("</p>", ""));
        }

        if (!TextUtils.isEmpty(mConfig.getMsgTxt())) {
            mConfig.setMsgTxt(mConfig.getMsgTxt().replace("<br/>", "").replace("\n", "").replace("<p>", "").replace("</p>", ""));
        }

        sobot_et_content.setHint(Html.fromHtml(mConfig.getMsgTmp()));
        HtmlTools.getInstance(getContext().getApplicationContext()).setRichText(sobot_tv_post_msg, mConfig.getMsgTxt(),
                ResourceUtils.getIdByName(getContext(), "color", "sobot_postMsg_url_color"));
        sobot_post_msg_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtil.hideKeyboard(sobot_post_msg_layout);
            }
        });
    }

    //设置editText的hint提示字体
    private void editTextSetHint() {
        String mustFill = "<font color='red'>&#8201*</font>";

        if (mConfig.isEmailFlag()) {
            sobot_post_email_lable.setText(Html.fromHtml("<font color='#8B98AD'>" + getResString("sobot_email") + "</font>" + mustFill));
        } else {
            sobot_post_email_lable.setText(Html.fromHtml("<font color='#8B98AD'>" + getResString("sobot_email") + "</font>"));
        }

        if (mConfig.isTelFlag()) {
            sobot_post_phone_lable.setText(Html.fromHtml("<font color='#8B98AD'>" + getResString("sobot_phone") + "</font>" + mustFill));
        } else {
            sobot_post_phone_lable.setText(Html.fromHtml("<font color='#8B98AD'>" + getResString("sobot_phone") + "</font>"));
        }
    }

    private ChatUtils.SobotSendFileListener sendFileListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(final String filePath) {
            zhiChiApi.fileUploadForPostMsg(SobotPostMsgFragment.this, mConfig.getCompanyId(), filePath, new ResultCallBack<ZhiChiMessage>() {
                @Override
                public void onSuccess(ZhiChiMessage zhiChiMessage) {
                    SobotDialogUtils.stopProgressDialog(getActivity());
                    if (zhiChiMessage.getData() != null) {
                        ZhiChiUploadAppFileModelResult item = new ZhiChiUploadAppFileModelResult();
                        item.setFileUrl(zhiChiMessage.getData().getUrl());
                        item.setFileLocalPath(filePath);
                        item.setViewState(1);
                        adapter.addData(item);
                    }
                }

                @Override
                public void onFailure(Exception e, String des) {
                    SobotDialogUtils.stopProgressDialog(getActivity());
                    showHint(des, false);
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {

                }
            });
        }

        @Override
        public void onError() {
            SobotDialogUtils.stopProgressDialog(getContext());
        }
    };

    public String getFileStr() {
        String tmpStr = "";
        if (!mConfig.isEnclosureShowFlag()) {
            return tmpStr;
        }

        ArrayList<ZhiChiUploadAppFileModelResult> tmpList = adapter.getPicList();
        for (int i = 0; i < tmpList.size(); i++) {
            tmpStr += tmpList.get(i).getFileUrl() + ";";
        }
        return tmpStr;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ZhiChiConstant.REQUEST_CODE_picture) { // 发送本地图片
                if (data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    SobotDialogUtils.startProgressDialog(getActivity());
                    ChatUtils.sendPicByUriPost(getContext(), selectedImage, sendFileListener, false);
                } else {
                    showHint(getResString("sobot_did_not_get_picture_path"), false);
                }
            } else if (requestCode == ZhiChiConstant.REQUEST_CODE_makePictureFromCamera) {
                if (cameraFile != null && cameraFile.exists()) {
                    SobotDialogUtils.startProgressDialog(getActivity());
                    ChatUtils.sendPicByFilePath(getContext(), cameraFile.getAbsolutePath(), sendFileListener, true);
                } else {
                    showHint(getResString("sobot_pic_select_again"), false);
                }
            }
        }

        if (data != null) {

            StCusFieldPresenter.onStCusFieldActivityResult(getContext(), data, mFields, sobot_post_customer_field);

            switch (requestCode) {
                case ZhiChiConstant.SOBOT_KEYTYPE_DELETE_FILE_SUCCESS://图片预览
                    List<ZhiChiUploadAppFileModelResult> tmpList = (List<ZhiChiUploadAppFileModelResult>) data.getExtras().getSerializable(ZhiChiConstant.SOBOT_KEYTYPE_PIC_LIST);
                    adapter.addDatas(tmpList);
                    break;
                case ZhiChiConstant.work_order_list_display_type_category:
                    if (!TextUtils.isEmpty(data.getStringExtra("category_typeId"))) {
                        String typeName = data.getStringExtra("category_typeName");
                        String typeId = data.getStringExtra("category_typeId");
                        sobot_post_question_type.setText(typeName);
                        sobot_post_question_type.setTag(typeId);
                    }
                    break;
                default:
                    break;
            }
        }
    }


    // 为弹出窗口popupwindow实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        public void onClick(View v) {
            menuWindow.dismiss();
            if (v.getId() == getResId("btn_take_photo")) {
                LogUtils.i("拍照");
                selectPicFromCameraBySys();
            }
            if (v.getId() == getResId("btn_pick_photo")) {
                LogUtils.i("选择照片");
                selectPicFromLocal();
            }
        }
    };

    @Override
    public void onClickCusField(View view, int fieldType, SobotFieldModel cusField) {
        switch (fieldType) {
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_TYPE:
                StCusFieldPresenter.openTimePicker(getActivity(), view, fieldType);
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SPINNER_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_RADIO_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CHECKBOX_TYPE:
                StCusFieldPresenter.startSobotCusFieldActivity(getActivity(), SobotPostMsgFragment.this, cusField);
                break;
            default:
                break;
        }
    }
}
