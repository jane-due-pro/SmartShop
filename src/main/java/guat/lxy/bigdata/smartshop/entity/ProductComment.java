package guat.lxy.bigdata.smartshop.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class ProductComment implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private Integer productId;
    private Integer userId;
    private Integer parentId;
    private String content;
    private Integer rating;
    private Date createTime;

    /** JOIN t_user，非DB字段 */
    private String username;

    /** 子回复列表，非DB字段，Java层组装 */
    private List<ProductComment> replies;

    /** 点赞数，非DB字段，查询时填充 */
    private Integer likeCount;

    /** 当前用户是否已点赞，非DB字段 */
    private Boolean liked;
}
