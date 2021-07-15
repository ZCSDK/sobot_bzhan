package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.ConsultingContent;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.SobotBitmapUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MessageHolderBase;

/**
 * 商品卡片
 * Created by jinxl on 2019/4/24.
 */
public class CardMessageHolder extends MessageHolderBase implements View.OnClickListener {
    private ImageView mPic;
    private TextView mTitle;
    private TextView mLabel;
    private View mContainer;
    private int defaultPicResId;
    private ConsultingContent mConsultingContent;

    public CardMessageHolder(Context context, View convertView) {
        super(context, convertView);
        mContainer = convertView.findViewById(ResourceUtils.getResId(context, "sobot_ll_content"));
        mPic = (ImageView) convertView.findViewById(ResourceUtils.getResId(context, "sobot_goods_pic"));
        mTitle = (TextView) convertView.findViewById(ResourceUtils.getResId(context, "sobot_goods_title"));
        mLabel = (TextView) convertView.findViewById(ResourceUtils.getResId(context, "sobot_goods_label"));
        defaultPicResId = ResourceUtils.getDrawableId(context, "sobot_icon_consulting_default_pic");
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        mConsultingContent = message.getConsultingContent();
        if (message.getConsultingContent() != null) {
            SobotBitmapUtil.display(context, CommonUtils.encode(message.getConsultingContent().getSobotGoodsImgUrl())
                    , mPic, defaultPicResId, defaultPicResId);
            mTitle.setText(message.getConsultingContent().getSobotGoodsTitle());
            mLabel.setText(message.getConsultingContent().getSobotGoodsLable());
            if (isRight) {
                try {
                    msgStatus.setClickable(true);
                    if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_SUCCESS) {// 成功的状态
                        msgStatus.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.GONE);
                        msgProgressBar.setVisibility(View.GONE);
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_ERROR) {
                        frameLayout.setVisibility(View.VISIBLE);
                        msgStatus.setVisibility(View.VISIBLE);
                        msgProgressBar.setVisibility(View.GONE);
//                        msgStatus.setOnClickListener(new ReSendTextLisenter(context, message
//                                .getId(), content, msgStatus, msgCallBack));
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_LOADING) {
                        frameLayout.setVisibility(View.VISIBLE);
                        msgProgressBar.setVisibility(View.VISIBLE);
                        msgStatus.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        mContainer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mContainer && mConsultingContent != null) {
            if (SobotOption.hyperlinkListener != null) {
                SobotOption.hyperlinkListener.onUrlClick(mConsultingContent.getSobotGoodsFromUrl());
                return;
            }
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra("url", mConsultingContent.getSobotGoodsFromUrl());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }
}
