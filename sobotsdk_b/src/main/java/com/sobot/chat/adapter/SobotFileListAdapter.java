package com.sobot.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sobot.chat.activity.SobotFileDetailActivity;
import com.sobot.chat.activity.SobotPhotoActivity;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.adapter.base.SobotBaseAdapter;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotFileModel;
import com.sobot.chat.api.model.ZhiChiUploadAppFileModelResult;
import com.sobot.chat.utils.FileSizeUtil;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.SobotBitmapUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.attachment.AttachmentView;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.image.SobotRCRelativeLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.sobot.chat.utils.FileSizeUtil.SIZETYPE_KB;
import static com.sobot.chat.utils.FileSizeUtil.SIZETYPE_MB;

/**
 * 留言 回复上传 附件
 */

public class SobotFileListAdapter extends SobotBaseAdapter<ZhiChiUploadAppFileModelResult> {


    public SobotFileListAdapter(Context context, List<ZhiChiUploadAppFileModelResult> list) {
        super(context, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ZhiChiUploadAppFileModelResult message = list.get(position);
        SobotFileHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(ResourceUtils.getIdByName(context, "layout", "sobot_file_item"), null);
            viewHolder = new SobotFileHolder(context, convertView, listener, position);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SobotFileHolder) convertView.getTag();
            viewHolder.setPosition(position);
        }
        viewHolder.bindData(message);
        return convertView;
    }

    @Override
    public ZhiChiUploadAppFileModelResult getItem(int position) {
        if (position < 0 || position >= list.size()) {
            return null;
        }
        return list.get(position);
    }

    public void addData(ZhiChiUploadAppFileModelResult data) {
        if (list == null) {
            return;
        }
        int lastIndex = (list.size() - 1) < 0 ? 0 : list.size() - 1;
        list.add(lastIndex, data);
        if (list.size() >= 5) {
            ZhiChiUploadAppFileModelResult lastBean = list.get(lastIndex);
            if (lastBean != null && 0 == lastBean.getViewState()) {
                list.remove(lastIndex);
            }
        }
        restDataView();
    }



    public void addDatas(List<ZhiChiUploadAppFileModelResult> tmpList) {
        list.clear();
        list.addAll(tmpList);
        restDataView();
    }

    public void restDataView() {
        if (list.size() == 0) {
            ZhiChiUploadAppFileModelResult addFile = new ZhiChiUploadAppFileModelResult();
            addFile.setViewState(0);
            list.add(addFile);
        } else {
            int lastIndex = (list.size() - 1) < 0 ? 0 : list.size() - 1;
            ZhiChiUploadAppFileModelResult result = list.get(lastIndex);
            if (list.size() < 5 && result.getViewState() != 0) {
                ZhiChiUploadAppFileModelResult addFile = new ZhiChiUploadAppFileModelResult();
                addFile.setViewState(0);
                list.add(addFile);
            }
        }
        notifyDataSetChanged();
    }

    public ArrayList<ZhiChiUploadAppFileModelResult> getPicList() {
        ArrayList<ZhiChiUploadAppFileModelResult> tmplist = new ArrayList<>();//所有图片的地址
        for (int i = 0; i < list.size(); i++) {
            ZhiChiUploadAppFileModelResult picFile = list.get(i);
            if (picFile.getViewState() != 0) {
                tmplist.add(picFile);
            }
        }
        return tmplist;
    }

    @Override
    public int getCount() {
        if (list.size() < 6) {
            return list.size();
        } else {
            return 5;
        }
    }


    private static class SobotFileHolder {
        private Context mContext;
        private TextView sobot_file_name;
        private ImageView sobot_iv_pic_add;
        private ImageView sobot_file_type_icon;
        private SobotRCRelativeLayout sobot_attachment_root_view;
        private ImageView imageView;
        private ImageView sobot_iv_pic_del;
        private onItemClickListener listener;
        private int position;

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        SobotFileHolder(Context context, View convertView, onItemClickListener listener, int position) {
            this.mContext = context;
            this.listener = listener;
            this.position = position;
            sobot_attachment_root_view = (SobotRCRelativeLayout) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_attachment_root_view"));
            sobot_file_name = (TextView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_file_name"));
            sobot_iv_pic_add = (ImageView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_iv_pic_add"));
            sobot_iv_pic_del = (ImageView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_iv_pic_del"));
            sobot_file_type_icon = (ImageView) convertView.findViewById(ResourceUtils.getIdByName(context, "id", "sobot_file_type_icon"));
            imageView = (ImageView) convertView.findViewById(ResourceUtils.getResId(context, "sobot_file_image_view"));
        }

        void bindData(final ZhiChiUploadAppFileModelResult message) {
            if (message.getViewState() == 0) {
                sobot_attachment_root_view.setVisibility(GONE);
                imageView.setVisibility(GONE);
                sobot_iv_pic_add.setVisibility(VISIBLE);
                sobot_iv_pic_del.setVisibility(GONE);
            } else {
                final int filetype = message.getFileType();
                sobot_iv_pic_add.setVisibility(GONE);
                sobot_iv_pic_del.setVisibility(VISIBLE);
                sobot_iv_pic_del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.deleteItem(position);
                        }
                    }
                });
                if (filetype == ZhiChiConstant.MSGTYPE_FILE_IMG) {
                    imageView.setVisibility(VISIBLE);
                    sobot_attachment_root_view.setVisibility(GONE);
                    SobotBitmapUtil.display(mContext, message.getFileUrl(), imageView);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (listener != null) {
                                listener.previewPic(message.getFileUrl());
                            }
                        }
                    });
                } else {
                    imageView.setVisibility(GONE);
                    sobot_attachment_root_view.setVisibility(VISIBLE);
                    sobot_file_name.setText(message.getFileName());
                    sobot_file_type_icon.setImageResource(FileTypeConfig.getFileIcon(mContext, message.getFileType()));
                    sobot_attachment_root_view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (filetype == ZhiChiConstant.MSGTYPE_FILE_MP4) {
                                SobotCacheFile cacheFile = new SobotCacheFile();
                                cacheFile.setFileName(message.getFileName());
                                cacheFile.setUrl(message.getFileUrl());
                                cacheFile.setFileType(message.getFileType());
                                cacheFile.setMsgId(message.getFileUrl());
                                cacheFile.setFileSize(FileSizeUtil.getFileOrFilesSize(message.getFileLocalPath(),SIZETYPE_KB)+"KB");
                                if (listener != null) {
                                    listener.previewMp4(cacheFile);
                                }
                            } else if (filetype == ZhiChiConstant.MSGTYPE_FILE_IMG) {
                                if (listener != null) {
                                    listener.previewPic(message.getFileUrl());
                                }
                            } else {
                                SobotCacheFile cacheFile = new SobotCacheFile();
                                cacheFile.setFileName(message.getFileName());
                                cacheFile.setUrl(message.getFileUrl());
                                cacheFile.setFileType(message.getFileType());
                                cacheFile.setMsgId(message.getFileUrl());
                                cacheFile.setFileSize(FileSizeUtil.getFileOrFilesSize(message.getFileLocalPath(),SIZETYPE_KB)+"KB");
                                if (listener != null) {
                                    listener.downFileLister(cacheFile);
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private onItemClickListener listener;

    public void setListener(onItemClickListener listener) {
        this.listener = listener;
    }

    public interface onItemClickListener {
        void downFileLister(SobotCacheFile fileModel);

        void previewMp4(SobotCacheFile fileModel);

        void previewPic(String imageUrL);

        void deleteItem(int postion);
    }
}