package com.scut.weixinshare.model.source.local;

import android.os.Handler;
import android.util.Log;

import com.scut.weixinshare.db.Comment;
import com.scut.weixinshare.db.DBOperator;
import com.scut.weixinshare.model.Moment;
import com.scut.weixinshare.model.source.MomentVersion;
import com.scut.weixinshare.utils.MomentUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//实现从数据库获取本地动态数据
public class MomentDatabaseSource implements MomentLocalSource {

    private final static String TAG = "MomentDatabaseSource";
    private static MomentDatabaseSource INSTANCE = new MomentDatabaseSource();

    //单例，线程安全
    public static MomentDatabaseSource getInstance(){
        return INSTANCE;
    }

    @Override
    public void getMoments(final List<MomentVersion> momentVersions, final GetMomentsCallback callback) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBOperator dbOperator = new DBOperator();
                final List<MomentLocal> moments = new ArrayList<>();
                for(MomentVersion momentVersion : momentVersions){
                    //获取动态内容
                    com.scut.weixinshare.db.Moment momentData = dbOperator
                            .selectMoment(momentVersion.getMomentId());
                    if(momentData != null && momentData.getUpdateTime()
                            .equals(String.valueOf(momentVersion.getUpdateTime().getTime()))){
                        //动态在本地数据库存在且更新时间与最新版本的更新时间一致，继续获取动态评论
                        List<Comment> commentData = dbOperator
                                .selectCommentUnderMoment(momentVersion.getMomentId());
                        MomentLocal moment = new MomentLocal(momentData);
                        List<CommentLocal> comments = new ArrayList<>();
                        for(Comment comment : commentData){
                            comments.add(new CommentLocal(comment));
                        }
                        moment.setCommentList(comments);
                        moments.add(moment);
                    }
                }
                dbOperator.close();
                //动态数据加载完成
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onMomentsLoaded(moments);
                    }
                });
            }
        }).run();
    }

    @Override
    public void createMoment(Moment moment) {
        List<Moment> moments = new ArrayList<>();
        moments.add(moment);
        createMoments(moments);
    }

    @Override
    public void createMoments(final List<Moment> moments) {
        //子线程更新数据库，不需要返回信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBOperator dbOperator = new DBOperator();
                for(Moment moment : moments) {
                    //删除数据库中可能存在的旧版本动态
                    dbOperator.deleteMoment(moment.getMomentId());
                    List<String> commentIds = dbOperator
                            .selectCommentIdUnderMoment(moment.getMomentId());
                    if (commentIds != null && commentIds.size() > 0) {
                        if (moment.getCommentList() == null) {
                            for (String commentId : commentIds) {
                                dbOperator.deleteComment(commentId);
                            }
                        } else {
                            //数据库中存在对应动态的评论，则对比新版本动态的评论和数据库中的动态评论
                            Set<String> commentIdSetLocal = new HashSet<>(commentIds);
                            for (com.scut.weixinshare.model.Comment comment : moment.getCommentList()) {
                                if (commentIdSetLocal.contains(comment.getCommentId())) {
                                    //如果评论id重复则不做处理
                                    commentIdSetLocal.remove(comment.getCommentId());
                                } else {
                                    //如果新版本动态的评论id在数据库评论中不存在，则插入评论
                                    dbOperator.insertComment(new Comment(comment.getCommentId(),
                                            moment.getMomentId(), comment.getSendId(), comment.getRecvId(),
                                            String.valueOf(comment.getCreateTime().getTime()), comment.getContent()));
                                }
                            }
                            for (String commentId : commentIdSetLocal) {
                                //如果数据库评论id在新版本动态中不存在，则删除评论
                                dbOperator.deleteComment(commentId);
                            }
                        }
                    } else if(moment.getCommentList() != null) {
                        //数据库中不存在该动态的评论，则直接插入所有评论
                        for (com.scut.weixinshare.model.Comment comment : moment.getCommentList()) {
                            dbOperator.insertComment(new Comment(comment.getCommentId(),
                                    moment.getMomentId(), comment.getSendId(), comment.getRecvId(),
                                    String.valueOf(comment.getCreateTime().getTime()), comment.getContent()));
                        }
                    }
                    //插入新版本动态
                    dbOperator.insertMoment(new com.scut.weixinshare.db.Moment(moment.getMomentId(),
                            moment.getUserId(), String.valueOf(moment.getCreateTime().getTime()),
                            String.valueOf(moment.getUpdateTime().getTime()), moment.getLocation(),
                            MomentUtils.imageUriListToString(moment.getPicContent()),
                            moment.getTextContent()));
                }
                dbOperator.close();
            }
        }).run();
    }
}
