package com.scut.weixinshare.contract;

import android.net.Uri;

import com.luck.picture.lib.entity.LocalMedia;
import com.scut.weixinshare.BasePresenter;
import com.scut.weixinshare.BaseView;

import java.util.List;

//发布动态页面交互接口
public interface ReleaseMomentContract {

    interface View extends BaseView<Presenter> {

        //显示加载对话框
        void showLoadingDialog(String text);

        //隐藏加载对话框
        void hideLoadingDialog();

        //更新显示的位置信息
        void updateLocationStatus(String location);

        //显示选择发布的图片
        void showAddedPics(List<Uri> pics);

        //显示应用提示信息
        void showReminderMessage(String text);

        //获取用户输入文字内容
        String getText();

        //显示选择当前位置界面
        void showPickLocationUI();

        //显示选择发布图片界面
        void showPictureSelectorUI(List<LocalMedia> selected);
    }

    interface Presenter extends BasePresenter {

        //获取用户当前位置信息
        void getLocation();

        //选择要发布的图片
        void selectPic();

        //发布动态
        void publish();

        //添加已选择图片
        void addPics(List<LocalMedia> selectList);
    }
}