package com.geekbang.horde.entity;

import lombok.Data;
import org.beetl.sql.core.annotatoin.AssignID;

import java.util.Date;
import java.util.List;

/**
 * @author chenbing
 * @since 2020-02-26 22:21
 */
@Data
public class Post {
    @AssignID
    private String id;
    private Long userId;
    private Integer likeCount;
    private Date createTime;

    private List<PostLikeUser> postLikeUserList;
}
