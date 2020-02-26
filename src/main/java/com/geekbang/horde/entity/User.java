package com.geekbang.horde.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author chenbing
 * @since 2020-02-26 14:18
 */
@Data
@NoArgsConstructor
public class User {

    private Integer id ;
    private Integer age ;
    private String name ;
    private Date createDate ;
}
