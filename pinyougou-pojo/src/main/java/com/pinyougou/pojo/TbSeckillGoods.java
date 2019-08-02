package com.pinyougou.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Data
@Accessors(chain = true)
public class TbSeckillGoods implements Serializable{
    private Long id;
    private Long goodsId;
    private Long itemId;
    private String title;
    private String smallPic;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String sellerId;
    private Date createTime;
    private Date checkTime;
    private String status;
    private Date startTime;
    private Date endTime;
    private Integer num;
    private Integer stockCount;
    private String introduction;
}
