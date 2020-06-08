package com.chedaia.boss.web.model;

import com.chedaia.biz.device.bo.DeviceStatusExpandMergeTreeBO;

/**
 * <pre>
 * 用于遍历树
 * 将树展开成一个扁平的list
 * </pre>
 * 
 * @version
 * 
 * <pre>
 * Author	Version		Date		Changes
 * wangwh 	1.0  		2019年1月11日 	Created
 * </pre>
 * 
 * @author wwh
 * @since 1.
 */
public class TreeModelItem {

    public enum TreeModelItemType {
        GROUP_NODE,
        VIRTUAL_NODE,
        DATA_NODE
    }

    private TreeModelItemType type;

    private TreeGroupNode groupNode;

    private TreeVirtualNode<DeviceStatusExpandMergeTreeBO> virtualNode;

    private DeviceStatusExpandMergeTreeBO dataNode;

    public TreeModelItem(TreeGroupNode groupNode){
        this.groupNode = groupNode;
        this.type = TreeModelItemType.GROUP_NODE;
    }

    public TreeModelItem(TreeVirtualNode<DeviceStatusExpandMergeTreeBO> virtualNode){
        this.virtualNode = virtualNode;
        this.type = TreeModelItemType.VIRTUAL_NODE;

    }

    public TreeModelItem(DeviceStatusExpandMergeTreeBO dataNode){
        this.dataNode = dataNode;
        this.type = TreeModelItemType.DATA_NODE;
    }

    public void setGroupNode(TreeGroupNode groupNode) {
        this.groupNode = groupNode;
        this.type = TreeModelItemType.GROUP_NODE;
    }

    public void setVirtualNode(TreeVirtualNode<DeviceStatusExpandMergeTreeBO> virtualNode) {
        this.virtualNode = virtualNode;
        this.type = TreeModelItemType.VIRTUAL_NODE;

    }

    public void setDataNode(DeviceStatusExpandMergeTreeBO dataNode) {
        this.dataNode = dataNode;
        this.type = TreeModelItemType.DATA_NODE;

    }

    public TreeModelItemType getType() {
        return type;
    }

    public TreeVirtualNode<DeviceStatusExpandMergeTreeBO> getVirtualNode() {
        return virtualNode;
    }

    public DeviceStatusExpandMergeTreeBO getDataNode() {
        return dataNode;
    }

    public TreeGroupNode getGroupNode() {
        return groupNode;
    }

    @Override
    public String toString() {
        StringBuffer sbf = new StringBuffer();
        switch (type) {
            case GROUP_NODE:
                sbf.append("分组节点     ");
                sbf.append(groupNode.getGroupEntity().getName());
                sbf.append("  分组ID：");
                sbf.append(groupNode.getGroupEntity().getId());
                sbf.append("  上级ID：");
                sbf.append(groupNode.getGroupEntity().getParentId());
                sbf.append("  统计信息：");
                sbf.append(groupNode.getStatDeviceNumberBO());

                break;
            case VIRTUAL_NODE:
                sbf.append("虚拟分组节点 ");
                sbf.append(virtualNode.getNodeName());
                sbf.append("  设备数量");
                sbf.append(virtualNode.getDeviceCount());
                break;
            case DATA_NODE:
                sbf.append("数据节点     ");
                sbf.append(dataNode.toString());
                break;

            default:
                break;
        }

        return sbf.toString();
    }
}
