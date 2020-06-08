package com.chedaia.boss.web.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * <pre>
 * 对应前端树形控件的数据模型
 * 树节点--虚拟分组
 * </pre>
 * 
 * @version
 * 
 * <pre>
 * Author	Version		Date		Changes
 * wangwh 	1.0  		2019年1月10日 	Created
 * </pre>
 * 
 * @author wwh
 * @since 1.
 */
public class TreeVirtualNode<T> {

    /**
     * 该虚拟分组所属的分组ID，在层级结构上属于它的上级
     */
    private Integer parentGroupId;

    /**
     * 虚拟组的类型
     */
    private Integer property;

    /**
     * 节点的名称，虚拟组的名称，月份组对应的是【yyyy-mm】
     */
    private String nodeName;

    /**
     * 当前虚拟分组下全部设备
     */
    private List<T> deviceList = new ArrayList<>();

    public TreeVirtualNode(Integer parentGroupId, Integer property, String nodeName){
        this.parentGroupId = parentGroupId;
        this.property = property;
        this.nodeName = nodeName;
    }

    /**
     * 添加一个设备到当前的虚拟分组中
     * 
     * @param device
     */
    public void addDevice(T device) {
        deviceList.add(device);
    }

    /**
     * 获取这个虚拟分组下的设备数量
     * 
     * @return
     */
    public int getDeviceCount() {
        return deviceList.size();
    }

    public Integer getParentGroupId() {
        return parentGroupId;
    }

    public void setParentGroupId(Integer parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    public Integer getProperty() {
        return property;
    }

    public void setProperty(Integer property) {
        this.property = property;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public List<T> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<T> deviceList) {
        this.deviceList = deviceList;
    }

    /**
     * 使用指定的排序器对当前已经添加到列表中的数据进行排序
     * 
     * @param comparator
     */
    public void sortDeviceList(Comparator<? super T> comparator) {
        if (deviceList != null && !deviceList.isEmpty()) {
            deviceList.sort(comparator);
        }
    }

}
