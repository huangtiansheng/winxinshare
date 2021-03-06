package com.scut.weixinshare.model.source.local;

import com.scut.weixinshare.model.Moment;
import com.scut.weixinshare.model.source.MomentVersion;

import java.util.List;

//动态数据本地来源接口
public interface MomentLocalSource {

    //监听获取多条动态回调接口
    interface GetMomentsCallback{

        void onMomentsLoaded(List<MomentLocal> moments);

    }

    //获取与传入Version相同的所有动态
    void getMoments(List<MomentVersion> momentVersions, GetMomentsCallback callback);

    //创建动态，不提供监听信息
    void createMoment(Moment moment);

    //创建多条动态，不提供监听信息
    void createMoments(List<Moment> moments);

}
