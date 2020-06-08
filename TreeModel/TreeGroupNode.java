package com.chedaia.boss.web.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.chedaia.biz.device.bo.DeviceStatusExpandMergeTreeBO;
import com.chedaia.biz.device.entity.StatisticsGroupNumberEntity;
import com.chedaia.biz.user.entity.GroupEntity;
import com.chedaia.boss.web.bo.StatDeviceNumberBO;
import com.chedaia.common.tools.util.DateUtil;

/**
 * <pre>
 * 对应前端树形控件的数据模型
 * 树节点--分组节点
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
public class TreeGroupNode {

    /**
     * 月份组做特殊处理<br>
     * 将安装时间转成月份作为虚拟分组名称
     */
    public static final String YFZ = "月份组";

    /**
     * 树模型，可以通过树模型获取信息
     */
    private TreeModel treeModel;

    /**
     * 分组信息
     */
    private GroupEntity groupEntity;

    /**
     * 统计信息
     */
    private StatDeviceNumberBO statDeviceNumberBO;

    /**
     * 这个节点下的其他分组节点
     */
    private List<TreeGroupNode> childGroupNode = new ArrayList<>();

    /**
     * 这个节点下的虚拟分组节点<br>
     * 使用TreeMap进行自然排序
     */
    private Map<String, TreeVirtualNode<DeviceStatusExpandMergeTreeBO>> childVirtualNodeMap = new TreeMap<>();

    /**
     * 这个分组下的全部设备数量
     */
    private int allDeviceCount = 0;

    /**
     * 构造方法
     * 
     * @param treeModel
     * @param groupEntity
     */
    public TreeGroupNode(TreeModel treeModel, GroupEntity groupEntity){
        this.treeModel = treeModel;
        this.groupEntity = groupEntity;
    }

    /**
     * 构造方法
     * 
     * @param treeModel
     * @param groupEntity
     * @param statisticsInfo
     */
    public TreeGroupNode(TreeModel treeModel, GroupEntity groupEntity, StatisticsGroupNumberEntity statisticsInfo){
        this.treeModel = treeModel;
        this.groupEntity = groupEntity;
        this.statDeviceNumberBO = convertStatisticsInfo(statisticsInfo);
    }

    /**
     * 构建树节点
     * 
     * @param treeModel
     * @param groupEntity
     * @param statisticsInfo
     * @return
     */
    public static TreeGroupNode createTreeNode(TreeModel treeModel, GroupEntity groupEntity,
                                               StatisticsGroupNumberEntity statisticsInfo) {
        return new TreeGroupNode(treeModel, groupEntity, statisticsInfo);
    }

    private StatDeviceNumberBO convertStatisticsInfo(StatisticsGroupNumberEntity statisticsInfo) {
        if (statisticsInfo == null) {
            return null;
        }

        StatDeviceNumberBO deviceNumber = new StatDeviceNumberBO();

        deviceNumber.setAll(statisticsInfo.getAllDevice());
        deviceNumber.setDring(statisticsInfo.getDrivingNumber());
        deviceNumber.setOffline(statisticsInfo.getNotNoLine());
        deviceNumber.setAlarm(statisticsInfo.getAlarmNumber());
        deviceNumber.setOnline(statisticsInfo.getOnLine());
        deviceNumber.setLineDelocali(statisticsInfo.getLineDelocaliNumber());
        deviceNumber.setRisk(statisticsInfo.getRiskNumber());
        deviceNumber.setWarning(statisticsInfo.getWarningNumber());

        return deviceNumber;
    }

    private StatDeviceNumberBO createEmptyStatisticsInfo() {
        StatDeviceNumberBO deviceNumber = new StatDeviceNumberBO();

        deviceNumber.setAll(0);
        deviceNumber.setDring(0);
        deviceNumber.setOffline(0);
        deviceNumber.setAlarm(0);
        deviceNumber.setOnline(0);
        deviceNumber.setLineDelocali(0);
        deviceNumber.setRisk(0);
        deviceNumber.setWarning(0);

        return deviceNumber;
    }

    /**
     * <pre>
     * 添加分组类型的子节点
     * 
     * 如果新添加的子节点有统计信息则会将所有的上级节点的统计信息累加
     * </pre>
     * 
     * @param node
     */
    public void addChildGroupNode(TreeGroupNode node) {
        childGroupNode.add(node);
        // 节点的统计数字递增
        StatDeviceNumberBO statistics = node.getStatDeviceNumberBO();
        if (statistics != null) {
            incrementalStatisticsInfo(statistics);
        }
    }

    /**
     * 累加统计信息
     * 
     * @param statistics
     */
    public void incrementalStatisticsInfo(StatDeviceNumberBO statisticsInfo) {
        if (statisticsInfo == null) {
            return;
        }
        // 当前的统计对象
        StatDeviceNumberBO sdnbo = getStatDeviceNumberBO();

        if (sdnbo == null) {
            sdnbo = createEmptyStatisticsInfo();
            setStatDeviceNumberBO(sdnbo);
        }

        sdnbo.setAll(sdnbo.getAll() + statisticsInfo.getAll());
        sdnbo.setDring(sdnbo.getDring() + statisticsInfo.getDring());
        sdnbo.setOffline(sdnbo.getOffline() + statisticsInfo.getOffline());
        sdnbo.setAlarm(sdnbo.getAlarm() + statisticsInfo.getAlarm());
        sdnbo.setOnline(sdnbo.getOnline() + statisticsInfo.getOnline());
        sdnbo.setLineDelocali(sdnbo.getLineDelocali() + statisticsInfo.getLineDelocali());
        sdnbo.setRisk(sdnbo.getRisk() + statisticsInfo.getRisk());
        sdnbo.setWarning(sdnbo.getWarning() + statisticsInfo.getWarning());

        // 上级节点累加
        TreeGroupNode parentGroupNode = getParentGroupNode();
        if (parentGroupNode != null) {
            parentGroupNode.incrementalStatisticsInfo(statisticsInfo);
        }
    }

    /**
     * <pre>
     * 往当前分组中添加设备
     * 会将设备添加到对应的虚拟分组中
     * </pre>
     * 
     * @param device
     */
    public void addDevice(DeviceStatusExpandMergeTreeBO device) {
        if (device == null) {
            return;
        }
        String virtualNodeName = calcVirtualNodeName(device);
        TreeVirtualNode<DeviceStatusExpandMergeTreeBO> treeVirtualNode = childVirtualNodeMap.get(virtualNodeName);
        if (treeVirtualNode == null) {
            // 创建虚拟分组
            treeVirtualNode = new TreeVirtualNode<>(getGroupId(), device.getProperty(), virtualNodeName);
            childVirtualNodeMap.put(virtualNodeName, treeVirtualNode);
        }

        treeVirtualNode.addDevice(device);

        // 累加分组下的设备数
        incrementDeviceCount();
    }

    /**
     * 分组下的设备数量+1<br>
     * 向该分组添加设备时需要将上级分组的设备数量+1
     */
    public void incrementDeviceCount() {
        allDeviceCount++;

        // 上级节点累加
        TreeGroupNode parentGroupNode = getParentGroupNode();
        if (parentGroupNode != null) {
            parentGroupNode.incrementDeviceCount();
        }
    }

    /**
     * 当前分组的设备数量+=deviceCount，并且向上累加<br>
     * 仅仅统计数量时使用
     * 
     * @param deviceCount 当前分组的设备数量
     */
    public void incrementDeviceCount(int deviceCount) {
        this.allDeviceCount += deviceCount;
        // 上级节点累加
        TreeGroupNode parentGroupNode = getParentGroupNode();
        if (parentGroupNode != null) {
            parentGroupNode.incrementDeviceCount(deviceCount);
        }
    }

    /**
     * 计算虚拟分组的名字<br>
     * 如果是月份组会按照服务期计算为 yyyy-MM 格式
     * 
     * @param device
     * @return
     */
    private String calcVirtualNodeName(DeviceStatusExpandMergeTreeBO device) {
        Integer property = device.getProperty();

        String propertyName = treeModel.getPropertyName(property);

        // 月份组要改成 年-月 的格式
        if (YFZ.equals(propertyName)) {
            if (device.getInstallTime() == null) {
                propertyName = "未安装";
            } else {
                propertyName = DateUtil.parseDate(device.getInstallTime(), "yyyy-MM");
            }
        }
        return propertyName;
    }

    /**
     * 上级节点ID == 上级分组ID
     * 
     * @return
     */
    public Integer getParentNodeId() {
        return groupEntity.getParentId();
    }

    /**
     * 获取分组ID
     * 
     * @return
     */
    public Integer getGroupId() {
        return groupEntity.getId();
    }

    /**
     * 获取分组名称
     * 
     * @return
     */
    public String getGroupName() {
        return groupEntity.getName();
    }

    /**
     * 获取上级节点
     * 
     * @return
     */
    public TreeGroupNode getParentGroupNode() {
        return treeModel.getTreeGroupNodeByGroupId(getParentNodeId());
    }

    public TreeModel getTreeModel() {
        return treeModel;
    }

    public void setTreeModel(TreeModel treeModel) {
        this.treeModel = treeModel;
    }

    public StatDeviceNumberBO getStatDeviceNumberBO() {
        return statDeviceNumberBO;
    }

    public void setStatDeviceNumberBO(StatDeviceNumberBO statDeviceNumberBO) {
        this.statDeviceNumberBO = statDeviceNumberBO;
    }

    public GroupEntity getGroupEntity() {
        return groupEntity;
    }

    public void setGroupEntity(GroupEntity groupEntity) {
        this.groupEntity = groupEntity;
    }

    public Map<String, TreeVirtualNode<DeviceStatusExpandMergeTreeBO>> getChildVirtualNodeMap() {
        return childVirtualNodeMap;
    }

    public int getAllDeviceCount() {
        return allDeviceCount;
    }

    public List<TreeGroupNode> getChildGroupNode() {
        return childGroupNode;
    }

}
