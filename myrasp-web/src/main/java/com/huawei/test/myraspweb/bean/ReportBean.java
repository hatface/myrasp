package com.huawei.test.myraspweb.bean;

import java.util.Map;

public class ReportBean {

    private String type;
    private Map para;
    public enum VULN{

        VULN("VULNRABlITY"),
        NOT("NOT_VULNRABlITY"),
        POSSIBLE("POSSIBLE");

        private String vulnStatus;

        VULN(String vulnStatus){this.vulnStatus=vulnStatus;}
    }
    private VULN vuln;

    public ReportBean() {
    }

    public ReportBean(String type, Map para, VULN vuln) {
        this.type = type;
        this.para = para;
        this.vuln = vuln;
    }

    public ReportBean(String type, Map para) {
        this.type = type;
        this.para = para;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map getPara() {
        return para;
    }

    public void setPara(Map para) {
        this.para = para;
    }

    public VULN getVuln() {
        return vuln;
    }

    public void setVuln(VULN vuln) {
        this.vuln = vuln;
    }
}
