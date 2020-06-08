package com.chedaia.boss.web.model;

import com.chedaia.common.tools.tree.Tree;

public class TreeNode extends Tree{
    
    private static final long serialVersionUID = 1L;
    
    private Boolean isParent = true;
    
    private String name;
    
    private Integer deviceNum = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDeviceNum() {
        return deviceNum;
    }

    public void setDeviceNum(Integer deviceNum) {
        this.deviceNum = deviceNum;
    }

    public Boolean getIsParent() {
        return isParent;
    }

    public void setIsParent(Boolean isParent) {
        this.isParent = isParent;
    }
}
