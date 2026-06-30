package guat.lxy.bigdata.smartshop.util;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * PageInfo 的可序列化包装器（PageInfo 本身不适合 Jackson JSON 序列化）。
 * 用于把分页结果存入 Redis 缓存。
 */
public class SerializablePage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;

    public SerializablePage() {
    }

    public static <T> SerializablePage<T> of(PageInfo<T> pageInfo) {
        SerializablePage<T> page = new SerializablePage<>();
        page.list = new ArrayList<>(pageInfo.getList());
        page.total = pageInfo.getTotal();
        page.pageNum = pageInfo.getPageNum();
        page.pageSize = pageInfo.getPageSize();
        page.pages = pageInfo.getPages();
        return page;
    }

    public PageInfo<T> toPageInfo() {
        Page<T> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.setPages(pages);
        if (list != null) {
            page.addAll(list);
        }
        return new PageInfo<>(page);
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
