package com.scut.weixinshare.presenter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.scut.weixinshare.IConst;
import com.scut.weixinshare.MyApplication;
import com.scut.weixinshare.R;
import com.scut.weixinshare.contract.UserContract;
import com.scut.weixinshare.manager.NetworkManager;
import com.scut.weixinshare.model.LoginReceive;
import com.scut.weixinshare.model.ResultBean;
import com.scut.weixinshare.model.User;
import com.scut.weixinshare.model.source.MomentUserData;
import com.scut.weixinshare.retrofit.BaseCallback;
import com.scut.weixinshare.utils.GlideUtils;
import com.scut.weixinshare.utils.MomentUtils;
import com.scut.weixinshare.view.LoginActivity;
import com.scut.weixinshare.view.MainActivity;
import com.scut.weixinshare.view.RegisterActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static com.scut.weixinshare.MyApplication.getContext;

public class UserPresenter implements UserContract.Presenter {

    private UserContract.View view;
    private User user;

    public User getUser() {
        return user;
    }

    public UserPresenter(UserContract.View view, User user){
        this.view = view;
        this.user =user;
        view.setPresenter(this);
    }
    public UserPresenter(UserContract.View view, String userid){
        this.view=view;
        setShowUser(userid);
        view.setPresenter(this);
    }
    @Override
    public void start() {
        view.showUserInfo(user);
    }

    @Override
    public void updatePortrait(File portrait) throws IOException {
        view.setPortrait(portrait);
        NetworkManager.getInstance().uploadProtrait(new BaseCallback<ResultBean>() {
            @Override
            public void onResponse(Call<ResultBean> call, Response<ResultBean> response) {

               if(response.body()!=null&response.body().getCode()==200){
                   Log.d(TAG, "上传头像成功 ");
                   Map map= (Map) response.body().getData();
                   String portrait= (String) map.get("portrait");
                 view.setPortrait(portrait);

               }
            }
            @Override
            public void onFailure(Call<ResultBean> call, Throwable t) {
                t.printStackTrace();
            }
        }, user.getUserId(),portrait);

    }

    @Override
    public void setShowUser(String userId) {
        if(!userId.equals(MyApplication.currentUser.getUserId())) {
            NetworkManager.getInstance().getUser(new BaseCallback<ResultBean>() {
                @Override
                public void onResponse(Call<ResultBean> call, Response<ResultBean> response) {
                    ResultBean resultBean=  getResultBean(response);
                    if(checkResult(getContext(),resultBean)){
                       user= (User) resultBean.getData();
                    }
                }

                @Override
                public void onFailure(Call call, Throwable t) {

                }
            },userId);
        }
        else
            user=MyApplication.currentUser;

    }

    @Override
    public void updateUserInfo() {
        NetworkManager.getInstance().updateUserInfo(new Callback<ResultBean>() {
            @Override
            public void onResponse(Call<ResultBean> call, Response<ResultBean> response) {
                ResultBean resultBean=response.body();
                Log.d(TAG, "onResponse: "+resultBean.toString());
                if (resultBean.getCode()==200)
                    Log.d(TAG, "onResponse: 修改用户信息成功");
                else
                    Log.d(TAG, "onResponse: 修改失败");
            }

            @Override
            public void onFailure(Call<ResultBean> call, Throwable t) {
                Log.d(TAG, "网络通信失败");

            }
        },MyApplication.currentUser);

    }



}
