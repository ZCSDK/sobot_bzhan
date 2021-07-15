package com.sobot.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.SobotUIConfig;
import com.sobot.chat.activity.SobotFileDetailActivity;
import com.sobot.chat.activity.SobotPhotoActivity;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.adapter.base.SobotBaseAdapter;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotFileModel;
import com.sobot.chat.api.model.SobotUserTicketEvaluate;
import com.sobot.chat.api.model.SobotUserTicketInfo;
import com.sobot.chat.api.model.StUserDealTicketInfo;
import com.sobot.chat.api.model.StUserDealTicketReply;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.DateUtil;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.attachment.AttachmentView;
import com.sobot.chat.widget.attachment.FileAttachmentAdapter;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.attachment.SpaceItemDecoration;

import java.util.List;

/**
 * 留言记录适配器
 *
 * @author Created by jinxl on 2019/3/7.
 */
public class SobotTicketDetailAdapter extends SobotBaseAdapter<Object> {

    private Context mContext;

    private static final String[] layoutRes = {
            "sobot_ticket_detail_head_item",//详情头布局
            "sobot_ticket_detail_created_item",//已创建布局
            "sobot_ticket_detail_processing_item",//受理中布局
            "sobot_ticket_detail_completed_item",//已完成布局
    };

    //详情头
    public static final int MSG_TYPE_HEAD = 0;
    //已创建
    public static final int MSG_TYPE_TYPE1 = 1;
    //受理中
    public static final int MSG_TYPE_TYPE2 = 2;
    //已完成
    public static final int MSG_TYPE_TYPE3 = 3;

    AttachmentView.Listener listener = new AttachmentView.Listener() {
        @Override
        public void downFileLister(SobotFileModel fileModel, int position) {
            // 打开详情页面
            Intent intent = new Intent(mContext, SobotFileDetailActivity.class);
            SobotCacheFile cacheFile = new SobotCacheFile();
            cacheFile.setFileName(fileModel.getFileName());
            cacheFile.setUrl(fileModel.getFileUrl());
            cacheFile.setFileType(FileTypeConfig.getFileType(fileModel.getFileType()));
            cacheFile.setMsgId(fileModel.getFileId());
            intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);

        }

        @Override
        public void previewMp4(SobotFileModel fileModel, int position) {
            SobotCacheFile cacheFile = new SobotCacheFile();
            cacheFile.setFileName(fileModel.getFileName());
            cacheFile.setUrl(fileModel.getFileUrl());
            cacheFile.setFileType(FileTypeConfig.getFileType(fileModel.getFileType()));
            cacheFile.setMsgId(fileModel.getFileId());
            Intent intent = SobotVideoActivity.newIntent(mContext, cacheFile);
            mContext.startActivity(intent);

        }

        @Override
        public void previewPic(String fileUrl, String fileName, int position) {
            Intent intent = new Intent(context, SobotPhotoActivity.class);
            intent.putExtra("imageUrL", fileUrl);
            context.startActivity(intent);
        }


    };

    public SobotTicketDetailAdapter(Context context, List list) {
        super(context, list);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object data = list.get(position);
        if (data != null) {
            int itemType = getItemViewType(position);
            convertView = initView(convertView, itemType, position, data);
            BaseViewHolder holder = (BaseViewHolder) convertView.getTag();
            holder.bindData(data, position);
        }
        return convertView;
    }

    private View initView(View convertView, int itemType, int position, final Object data) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(ResourceUtils.getIdByName(context, "layout", layoutRes[itemType]), null);
            BaseViewHolder holder;
            switch (itemType) {
                case MSG_TYPE_HEAD: {
                    holder = new HeadViewHolder(context, convertView);
                    break;
                }
                case MSG_TYPE_TYPE1: {
                    holder = new Type1ViewHolder(context, convertView);
                    break;
                }
                case MSG_TYPE_TYPE2: {
                    holder = new Type2ViewHolder(context, convertView);
                    break;
                }
                case MSG_TYPE_TYPE3: {
                    holder = new Type3ViewHolder(context, convertView);
                    break;
                }
                default:
                    holder = new HeadViewHolder(context, convertView);
                    break;
            }
            convertView.setTag(holder);
        }
        return convertView;
    }

    /**
     * @return 返回有多少种UI布局样式
     */
    @Override
    public int getViewTypeCount() {
        if (layoutRes.length > 0) {
            return layoutRes.length;
        }
        return super.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        Object data = list.get(position);
        if (data instanceof SobotUserTicketInfo) {
            return MSG_TYPE_HEAD;
        } else if (data instanceof StUserDealTicketInfo) {
            StUserDealTicketInfo item = (StUserDealTicketInfo) data;
            if (item.getFlag() == 1) {
                return MSG_TYPE_TYPE1;
            } else if (item.getFlag() == 2) {
                return MSG_TYPE_TYPE2;
            } else if (item.getFlag() == 3) {
                return MSG_TYPE_TYPE3;
            }
        }
        return MSG_TYPE_HEAD;
    }

    static abstract class BaseViewHolder {
        Context mContext;

        BaseViewHolder(Context context, View view) {
            mContext = context;
        }

        abstract void bindData(Object data, int position);
    }

    class HeadViewHolder extends BaseViewHolder {
        private TextView tv_title;
        private TextView tv_content;

        private Context mContext;

        HeadViewHolder(Context context, View view) {
            super(context, view);
            mContext = context;
            tv_title = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_title"));
            tv_content = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_content"));
        }

        void bindData(Object item, int position) {
            SobotUserTicketInfo data = (SobotUserTicketInfo) item;
            tv_content.setText(TextUtils.isEmpty(data.getContent()) ? "" : Html.fromHtml(data.getContent()));
        }
    }

    class Type1ViewHolder extends BaseViewHolder {
        private TextView sobot_tv_icon;
        private TextView sobot_tv_time;
        private TextView sobot_tv_icon2;
        private TextView sobot_tv_status;

        Type1ViewHolder(Context context, View view) {
            super(context, view);
            sobot_tv_icon = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_icon"));
            sobot_tv_icon2 = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_icon2"));
            sobot_tv_status = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_status"));
            sobot_tv_time = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_time"));
        }

        void bindData(Object item, int position) {
            if (position == 1) {
                sobot_tv_icon.setSelected(true);
                sobot_tv_time.setSelected(true);
                sobot_tv_icon2.setSelected(true);
                sobot_tv_status.setSelected(true);
                sobot_tv_time.setSelected(true);
            }

            StUserDealTicketInfo data = (StUserDealTicketInfo) item;
            sobot_tv_time.setText(data.getTimeStr());
        }
    }

    class Type2ViewHolder extends BaseViewHolder {
        private TextView sobot_tv_icon;
        private TextView sobot_tv_time;
        private TextView sobot_tv_icon2;
        private TextView sobot_tv_status;
        private TextView sobot_tv_content;
        private TextView sobot_tv_description;
        private LinearLayout sobot_ll_container;
        private RecyclerView recyclerView;

        Type2ViewHolder(Context context, View view) {
            super(context, view);
            sobot_tv_icon = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_icon"));
            sobot_tv_icon2 = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_icon2"));
            sobot_tv_status = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_status"));
            sobot_tv_time = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_time"));
            sobot_tv_content = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_content"));
            sobot_tv_description = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_description"));
            sobot_ll_container = (LinearLayout) view.findViewById(ResourceUtils.getResId(context, "sobot_ll_container"));
            recyclerView = (RecyclerView) view.findViewById(ResourceUtils.getResId(context, "sobot_attachment_file_layout"));
            GridLayoutManager gridlayoutmanager = new GridLayoutManager(context, 2);
            recyclerView.addItemDecoration(new SpaceItemDecoration(ScreenUtils.dip2px(mContext, 10), ScreenUtils.dip2px(mContext, 10), 0, SpaceItemDecoration.GRIDLAYOUT));
            recyclerView.setLayoutManager(gridlayoutmanager);
        }

        void bindData(Object item, int position) {
            if (position == 1) {
                sobot_tv_icon.setSelected(true);
                sobot_tv_time.setSelected(true);
                sobot_tv_icon2.setSelected(true);
                sobot_tv_status.setSelected(true);
                sobot_tv_time.setSelected(true);
                sobot_ll_container.setSelected(true);
            }
            StUserDealTicketInfo data = (StUserDealTicketInfo) item;
            StUserDealTicketReply reply = data.getReply();
            if (reply != null) {
                if (reply.getStartType() == 0) {
                    //客服
                    sobot_tv_status.setVisibility(View.VISIBLE);
                    sobot_tv_description.setText("客服回复");
                    if (TextUtils.isEmpty(reply.getReplyContent())) {
                        sobot_tv_content.setText( "客服已经成功收到您的问题，请耐心等待" );
                    } else {
                        HtmlTools.getInstance(context).setRichText(sobot_tv_content, reply.getReplyContent().replaceAll("\n", "<br/>"), getLinkTextColor());
                    }

                } else {
                    //客户
                    sobot_tv_status.setVisibility(View.GONE);
                    sobot_tv_description.setText("客户回复");
                    sobot_tv_content.setText(TextUtils.isEmpty(reply.getReplyContent()) ? "无" : Html.fromHtml(reply.getReplyContent()));
                }
                sobot_tv_time.setText(DateUtil.toDate(reply.getReplyTime() * 1000, DateUtil.DATE_FORMAT));
                recyclerView.setAdapter(new FileAttachmentAdapter(context, data.getFileList(), ContextCompat.getColor(context, ResourceUtils.getResColorId(context, "sobot_robot_msg_text_color")), listener));

            } else {
                sobot_tv_status.setVisibility(View.GONE);
                sobot_tv_description.setText("客服回复");
                sobot_tv_content.setText(data.getContent());
                sobot_tv_time.setText(data.getTimeStr());
            }
        }
    }

    class Type3ViewHolder extends BaseViewHolder implements View.OnClickListener {
        private TextView sobot_tv_icon;
        private TextView sobot_tv_time;
        private TextView sobot_tv_icon2;
        private TextView sobot_tv_status;
        private TextView sobot_tv_content;
        //评价按钮
        private TextView sobot_tv_evaluate;
        private TextView sobot_tv_score;
        private LinearLayout sobot_ll_score;
        private TextView sobot_tv_remark;
        private LinearLayout sobot_ll_remark;
        private RecyclerView recyclerView;

        SobotUserTicketEvaluate mEvaluate;

        Type3ViewHolder(Context context, View view) {
            super(context, view);
            sobot_tv_icon = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_icon"));
            sobot_tv_icon2 = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_icon2"));
            sobot_tv_status = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_status"));
            sobot_tv_time = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_time"));
            sobot_tv_content = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_content"));
            sobot_tv_evaluate = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_evaluate"));
            sobot_tv_score = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_score"));
            sobot_ll_score = (LinearLayout) view.findViewById(ResourceUtils.getResId(context, "sobot_ll_score"));
            sobot_tv_remark = (TextView) view.findViewById(ResourceUtils.getResId(context, "sobot_tv_remark"));
            sobot_ll_remark = (LinearLayout) view.findViewById(ResourceUtils.getResId(context, "sobot_ll_remark"));
            sobot_tv_evaluate.setOnClickListener(this);
            recyclerView = (RecyclerView) view.findViewById(ResourceUtils.getResId(context, "sobot_attachment_file_layout"));
            GridLayoutManager gridlayoutmanager = new GridLayoutManager(context, 2);
            recyclerView.addItemDecoration(new SpaceItemDecoration(ScreenUtils.dip2px(mContext, 10), ScreenUtils.dip2px(mContext, 10), 0, SpaceItemDecoration.GRIDLAYOUT));
            recyclerView.setLayoutManager(gridlayoutmanager);
        }

        void bindData(Object item, int position) {
            if (position == 1) {
                sobot_tv_icon.setSelected(true);
                sobot_tv_time.setSelected(true);
                sobot_tv_icon2.setSelected(true);
                sobot_tv_status.setSelected(true);
                sobot_tv_time.setSelected(true);
            }

            StUserDealTicketInfo data = (StUserDealTicketInfo) item;
            sobot_tv_time.setText(data.getTimeStr());
            sobot_tv_content.setText(TextUtils.isEmpty(data.getContent()) ? "" : Html.fromHtml(data.getContent()));
            mEvaluate = data.getEvaluate();
            if (mEvaluate.isOpen()) {
                if (mEvaluate.isEvalution()) {
                    //已评价
                    sobot_tv_evaluate.setVisibility(View.GONE);
                    List<SobotUserTicketEvaluate.TicketScoreInfooListBean> infooList = mEvaluate.getTicketScoreInfooList();
                    if (infooList != null && infooList.size() >= mEvaluate.getScore()) {
                        sobot_ll_score.setVisibility(View.VISIBLE);
                        sobot_tv_score.setText(infooList.get(5 - mEvaluate.getScore()).getScoreExplain());
                    } else {
                        sobot_ll_score.setVisibility(View.GONE);
                    }

                    if (TextUtils.isEmpty(mEvaluate.getRemark())) {
                        sobot_ll_remark.setVisibility(View.GONE);
                    } else {
                        sobot_ll_remark.setVisibility(View.VISIBLE);
                        sobot_tv_remark.setText(mEvaluate.getRemark());
                    }
                } else {
                    sobot_tv_evaluate.setVisibility(View.VISIBLE);

                    sobot_ll_score.setVisibility(View.GONE);
                    sobot_ll_remark.setVisibility(View.GONE);
                }
            } else {
                sobot_tv_evaluate.setVisibility(View.GONE);
                sobot_ll_score.setVisibility(View.GONE);
                sobot_ll_remark.setVisibility(View.GONE);
            }
            recyclerView.setAdapter(new FileAttachmentAdapter(context, data.getFileList(), ContextCompat.getColor(context, ResourceUtils.getResColorId(context, "sobot_robot_msg_text_color")), listener));
        }

        @Override
        public void onClick(View v) {
            if (v == sobot_tv_evaluate && mEvaluate != null) {
                if (mContext instanceof Activity) {
                    ChatUtils.showTicketEvaluateDialog((Activity) mContext, mEvaluate);
                }
            }
        }
    }

    //左右两边气泡内链接文字的字体颜色
    protected int getLinkTextColor() {

        if (SobotUIConfig.DEFAULT != SobotUIConfig.sobot_chat_left_link_textColor) {
            return SobotUIConfig.sobot_chat_left_link_textColor;
        } else {
            return ResourceUtils.getIdByName(mContext, "color", "sobot_color_link");
        }
    }

}