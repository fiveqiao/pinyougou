package com.pinyougou.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
public class TbSeckillOrder implements Serializable{
    private Long id;
    private Long seckillId;
    private BigDecimal money;
    private String userId;
    private String sellerId;
    private Date createTime;
    private Date payTime;
    private String status;
    private String receiverAddress;
    private String receiverMobile;
    private String receiver;
    private String transactionId;
}
