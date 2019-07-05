package entity;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 * @author  fiveqiao
 */

public class PageResult<E> implements Serializable {
    /**
     * 总记录数
     */
    private long total;
    /**
     * 当前页记录
     */
    private List<E> rows;

    public PageResult() {
    }

    public PageResult(long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<E> getRows() {
        return rows;
    }

    public void setRows(List<E> rows) {
        this.rows = rows;
    }
}
