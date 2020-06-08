package com.chedaia.boss.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chedaia.biz.base.bo.ParameterBO;
import com.chedaia.biz.device.bo.DeviceStatusExpandMergeTreeBO;
import com.chedaia.biz.device.entity.StatisticsGroupNumberEntity;
import com.chedaia.biz.user.entity.GroupEntity;

/**
 * <pre>
 * 对应前端树形控件的数据模型
 * 
 * 先创建一个树模型：createTreeModel
 * 再往模型中添加数据：addDevice
 * 再调用返回获取数据：expandTree2FlatList、expandGroupNode2FlatList、expandOneLevelGroupNode、getRootTreeModelItem
 * 
 * 注意：不要重复构建
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
public class TreeModel {

    private static Logger logger = LoggerFactory.getLogger(TreeModel.class);

    /**
     * 当前用户所有的监控的分组
     */
    private Map<Integer, GroupEntity> allGroups = new HashMap<>();

    /**
     * 记录这棵树上的所有分组节点
     */
    private Map<Integer, TreeGroupNode> allGroupNodes = new HashMap<>();

    /**
     * 这个树的根节点，设备一定是处于分组之下的，故根节点一定是分组节点
     */
    private List<TreeGroupNode> rootNodes = new ArrayList<>();

    /**
     * 用于获取虚拟分组的名称
     */
    private Map<Integer, String> propertyName = new HashMap<>();

    /**
     * 设置虚拟分组的配置，用于获取虚拟分组的名字
     * 
     * @param plist
     */
    public void setPropertyInfo(List<ParameterBO> plist) {
        if (plist == null || plist.isEmpty()) {
            return;
        }

        for (ParameterBO parameterBO : plist) {
            String idStr = parameterBO.getParameterCode();
            propertyName.put(Integer.valueOf(idStr), parameterBO.getParameterName());
        }
    }

    /**
     * 根据虚拟分组Id获取虚拟分组的名字
     * 
     * @param property
     * @return 如果不存在会返回‘未定义’
     */
    public String getPropertyName(Integer property) {
        String name = propertyName.get(property);
        if (name == null) {
            return "未定义";
        }
        return name;
    }

    /**
     * 根据分组ID查找节点
     * 
     * @param groupId
     * @return
     */
    public TreeGroupNode getTreeGroupNodeByGroupId(Integer groupId) {
        if (groupId == null) {
            return null;
        }
        return allGroupNodes.get(groupId);
    }

    /**
     * 返回根节点
     * 
     * @return
     */
    public List<TreeGroupNode> getRootNodes() {
        return rootNodes;
    }

    /**
     * 在这个树上添加节点
     * 
     * @param node
     */
    public void addGroupNode(TreeGroupNode node) {
        // 先记录这个节点
        allGroupNodes.put(node.getGroupId(), node);

        // 再加到树形结构中
        TreeGroupNode parentNode = allGroupNodes.get(node.getParentNodeId());

        if (parentNode == null) {
            // 当前这个节点是根节点
            rootNodes.add(node);
        } else {
            // 是某个节点的子节点
            parentNode.addChildGroupNode(node);
        }
    }

    /**
     * <pre>
     * 往模型树中添加设备
     * 根据设备的ID将设备添加到对应的分组的对应的虚拟分组中
     * </pre>
     */
    public void addDevice(DeviceStatusExpandMergeTreeBO device) {
        Integer groupId = device.getGroupId();
        TreeGroupNode treeNode = getTreeGroupNodeByGroupId(groupId);
        if (treeNode == null) {
            logger.warn("往模型树中添加设备时设备对应的分组不存在，分组ID:{}", groupId);
            return;
        }

        treeNode.addDevice(device);
    }

    /**
     * 构建树模型
     * 
     * @param groupList 当前用户监控的分组
     * @return
     */
    public static TreeModel createTreeModel(List<GroupEntity> groupList) {
        return createTreeModel(groupList, null);
    }

    /**
     * 构建树模型
     * 
     * @param groupList 当前用户监控的分组
     * @param statisticsGroupNumberMap 分组的统计信息，可以为null，为null时树的节点上不包含统计信息
     * @return
     */
    public static TreeModel createTreeModel(List<GroupEntity> groupList,
                                            Map<Integer, StatisticsGroupNumberEntity> statisticsGroupNumberMap) {
        TreeModel treeModel = new TreeModel();
        treeModel.buildGroupTree(groupList, statisticsGroupNumberMap);
        return treeModel;
    }

    /**
     * 构建分组树
     * 
     * @param groupList 需要构建树的分组列表
     */
    public void buildGroupTree(List<GroupEntity> groupList) {
        buildGroupTree(groupList, null);
    }

    /**
     * 构建分组树
     * 
     * @param groupList 当前用户监控的分组
     * @param statisticsGroupNumberMap 分组的统计信息，可以为null，为null时树的节点上不包含统计信息
     */
    public void buildGroupTree(List<GroupEntity> groupList,
                               Map<Integer, StatisticsGroupNumberEntity> statisticsGroupNumberMap) {

        // 先排序，可以减少递归的次数，并且保证树节点的显示顺序
        Collections.sort(groupList, getAscGroupComparator());

        // 用Map结构保存用户监控的全部分组，便于快速定位
        for (GroupEntity groupEntity : groupList) {
            allGroups.put(groupEntity.getId(), groupEntity);
        }

        // 构建一个树
        for (GroupEntity groupEntity : groupList) {
            recursionBuildTreeNode(groupEntity, statisticsGroupNumberMap);
        }

    }

    /**
     * 递归构建树节点
     * 
     * @param groupEntity
     * @param statisticsGroupNumberMap
     */
    public void recursionBuildTreeNode(GroupEntity groupEntity,
                                       Map<Integer, StatisticsGroupNumberEntity> statisticsGroupNumberMap) {
        Integer groupId = groupEntity.getId();
        // 先判断树里面是否已经存在了这个节点
        if (allGroupNodes.containsKey(groupId)) {
            return;
        }

        Integer parentGroupId = groupEntity.getParentId();
        // 判断上级节点是否存在
        TreeGroupNode parentNode = allGroupNodes.get(parentGroupId);
        if (parentNode == null) {
            GroupEntity parentGroupEntity = allGroups.get(parentGroupId);
            // 再判断监控的分组里面是否有当前节点的上级节点
            if (parentGroupEntity != null) {
                // 先创建上级的节点
                recursionBuildTreeNode(parentGroupEntity, statisticsGroupNumberMap);
            }
        }

        // 创建节点对象
        TreeGroupNode node = TreeGroupNode.createTreeNode(this,
            groupEntity,
            statisticsGroupNumberMap == null ? null : statisticsGroupNumberMap.get(groupId));

        // 将创建的节点添加到树中
        addGroupNode(node);

    }

    /**
     * 生效排列比较器
     * 
     * @return
     */
    private Comparator<GroupEntity> getAscGroupComparator() {
        return new Comparator<GroupEntity>() {

            @Override
            public int compare(GroupEntity o1, GroupEntity o2) {
                if (o1 != null && o2 != null) {
                    return o1.getId() - o2.getId();
                }
                if (o1 == null) {
                    return -1;
                }
                if (o2 == null) {
                    return 1;
                }

                return 0;
            }

        };
    }

    /**
     * 获取树的根节点
     * 
     * @return
     */
    public List<TreeModelItem> getRootTreeModelItem() {
        List<TreeModelItem> list = new ArrayList<>();

        for (TreeGroupNode groupNode : rootNodes) {
            TreeModelItem item = new TreeModelItem(groupNode);
            list.add(item);
        }

        return list;
    }

    /**
     * 将这颗树展开成扁平的列表对象
     * 
     * @param containDevice 返回的列表中是否包含设备对象，不包含也可以从虚拟节点中取出对应的设备
     * @return 有序的扁平列表
     */
    public List<TreeModelItem> expandTree2FlatList(boolean containDevice) {
        List<TreeModelItem> list = new ArrayList<>();

        for (TreeGroupNode groupNode : rootNodes) {
            TreeModelItem item = new TreeModelItem(groupNode);
            list.add(item);

            // 递归展开下级
            recursionExpandGroupNode(list, groupNode, containDevice, false);
        }

        return list;
    }

    /**
     * 将这颗树展开成扁平的列表对象<br>
     * 并且排除掉没有设备的空分组
     * 
     * @param containDevice
     * true:返回的list中有设备对象，false：list中只有分组信息，此时设备依旧可以从虚拟分组中取出
     * @return
     */
    public List<TreeModelItem> expandTreeExcludeEmptyNode(boolean containDevice) {
        List<TreeModelItem> list = new ArrayList<>();

        for (TreeGroupNode groupNode : rootNodes) {
            // 过滤掉没有设备的顶级分组
            if (groupNode.getAllDeviceCount() == 0) {
                continue;
            }

            list.add(new TreeModelItem(groupNode));

            // 递归展开下级
            recursionExpandGroupNode(list, groupNode, containDevice, true);
        }

        return list;

    }

    /**
     * 将指定的分组展开成扁平的列表对象
     * 
     * @param groupId 分组ID
     * @param containDevice 返回的列表中是否包含设备对象，不包含也可以从虚拟节点中取出对应的设备
     * @return 有序的扁平列表
     */
    public List<TreeModelItem> expandGroupNode2FlatList(int groupId, boolean containDevice, boolean excludeEmpty) {
        List<TreeModelItem> list = new ArrayList<>();

        TreeGroupNode groupNode = allGroupNodes.get(groupId);

        if (groupNode != null) {
            // 递归展开下级
            recursionExpandGroupNode(list, groupNode, containDevice, excludeEmpty);
        }

        return list;
    }

    /**
     * 只展开一个层级的分组<br>
     * 既只将分组和虚拟分组添加到list中<br>
     * 是否加入设备由containDevice 参数觉定
     * 
     * @param groupId
     * @param containDevice 返回的列表中是否包含设备对象，不包含也可以从虚拟节点中取出对应的设备
     * @return
     */
    public List<TreeModelItem> expandOneLevelGroupNode(int groupId, boolean containDevice) {
        List<TreeModelItem> list = new ArrayList<>();

        TreeGroupNode groupNode = allGroupNodes.get(groupId);

        if (groupNode == null) {
            return list;
        }

        List<TreeGroupNode> childGroupList = groupNode.getChildGroupNode();
        if (childGroupList != null && !childGroupList.isEmpty()) {
            for (TreeGroupNode childGroupNode : childGroupList) {
                list.add(new TreeModelItem(childGroupNode));
            }
        }

        // 展开虚拟分组
        expandVirtualNode(list, groupNode, containDevice);

        return list;
    }

    /**
     * 递归展开分组
     * 
     * @param list 保存展开对象的List
     * @param groupNode 要展开的分组节点
     * @param containDevice 展开的List中是否包含设备
     * @param excludeEmpty 是否排除掉没有设备的节点
     */
    private void recursionExpandGroupNode(List<TreeModelItem> list, TreeGroupNode groupNode, boolean containDevice,
                                          boolean excludeEmpty) {
        List<TreeGroupNode> childGroupList = groupNode.getChildGroupNode();
        if (childGroupList != null && !childGroupList.isEmpty()) {
            for (TreeGroupNode childGroupNode : childGroupList) {

                if (excludeEmpty && childGroupNode.getAllDeviceCount() == 0) {
                    // 排除掉没有设备的空分组
                    continue;
                }

                list.add(new TreeModelItem(childGroupNode));

                // 递归展开下级
                recursionExpandGroupNode(list, childGroupNode, containDevice, excludeEmpty);
            }
        }

        // 展开虚拟分组
        expandVirtualNode(list, groupNode, containDevice);
    }

    /**
     * 展开当前节点下的虚拟分组
     * 
     * @param list
     * @param groupNode
     * @param containDevice
     */
    private void expandVirtualNode(List<TreeModelItem> list, TreeGroupNode groupNode, boolean containDevice) {
        // 展开虚拟分组
        Map<String, TreeVirtualNode<DeviceStatusExpandMergeTreeBO>> virtualNodeMap = groupNode.getChildVirtualNodeMap();
        if (virtualNodeMap == null || virtualNodeMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, TreeVirtualNode<DeviceStatusExpandMergeTreeBO>> virtualEntity : virtualNodeMap.entrySet()) {
            TreeVirtualNode<DeviceStatusExpandMergeTreeBO> virtualNode = virtualEntity.getValue();
            // 添加虚拟组
            list.add(new TreeModelItem(virtualNode));

            if (containDevice) {
                // 添加设备
                List<DeviceStatusExpandMergeTreeBO> deviceList = virtualNode.getDeviceList();
                for (DeviceStatusExpandMergeTreeBO device : deviceList) {
                    list.add(new TreeModelItem(device));
                }
            }
        }
    }

}
