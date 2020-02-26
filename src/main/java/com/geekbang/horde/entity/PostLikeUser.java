package com.geekbang.horde.entity;

import lombok.Data;

/**
 * @author tankdev
 * @since 2020-02-26 22:44
 */
@Data
public class PostLikeUser {
    private Integer id;
    private Long userId;
    private String postId;
    private String ucode;
}
