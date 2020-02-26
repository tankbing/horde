package com.geekbang.horde.dto;

import lombok.Data;

/**
 * @author chenbing
 * @since 2020-02-25 17:34
 */
@Data
public class Page {
    private Boolean more;
    private Long next_index;
    private Integer current;
}
