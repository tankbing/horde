package com.geekbang.horde.dto;

import com.geekbang.horde.entity.Post;
import com.geekbang.horde.entity.PostLikeUser;
import lombok.Data;

import java.util.List;

/**
 * @author chenbing
 * @since 2020-02-26 22:52
 */
@Data
public class PostData {
    private Page page;
    private List<Post> postList;
}
