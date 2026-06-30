package guat.lxy.bigdata.smartshop.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private String photoUrl;
    private Double price;
    private String descp;
    private Date releaseDate;
    private Integer catId;
    private String categoryName;
}
