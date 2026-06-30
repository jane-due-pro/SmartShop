package guat.lxy.bigdata.smartshop.config;

import com.github.pagehelper.PageInfo;

import java.io.Serializable;
import java.util.List;

/**
 * PageInfo 的可序列化包装类。
 *
 * 原因：com.github.pagehelper.PageInfo 没有 implements Serializable，
 *      配合 JdkSerializationRedisSerializer 写入 Redis 会抛 NotSerializableException。
 *      这里只暴露前端需要的字段，序列化、反序列化均安全。
 */
public class SerializablePage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int pageNum;
    private int pageSize;
    private long total;
    private int pages;
    private List<T> list;

    public SerializablePage() {}

    public SerializablePage(int pageNum, int pageSize, long total, int pages, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = pages;
        this.list = list;
    }

    public static <T> SerializablePage<T> of(PageInfo<T> p) {
        if (p == null) return null;
        return new SerializablePage<>(p.getPageNum(), p.getPageSize(),
                p.getTotal(), p.getPages(), p.getList());
    }

    public PageInfo<T> toPageInfo() {
        PageInfo<T> p = new PageInfo<>();
        p.setPageNum(pageNum);
        p.setPageSize(pageSize);
        p.setTotal(total);
        p.setPages(pages);
        p.setList(list);
        return p;
    }

    public int getPageNum() { return pageNum; }
    public int getPageSize() { return pageSize; }
    public long getTotal() { return total; }
    public int getPages() { return pages; }
    public List<T> getList() { return list; }
}