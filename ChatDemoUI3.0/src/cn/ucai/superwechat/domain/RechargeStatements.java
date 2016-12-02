package cn.ucai.superwechat.domain;

public class RechargeStatements {
    private Integer id;

    private String uname;

    private Integer count;

    private Integer rmb;

    private String rdate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname == null ? null : uname.trim();
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getRmb() {
        return rmb;
    }

    public void setRmb(Integer rmb) {
        this.rmb = rmb;
    }

    public String getRdate() {
        return rdate;
    }

    public void setRdate(String rdate) {
        this.rdate = rdate;
    }
}