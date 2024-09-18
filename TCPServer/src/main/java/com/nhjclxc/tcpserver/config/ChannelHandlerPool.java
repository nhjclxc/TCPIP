package com.nhjclxc.tcpserver.config;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * netty信道池，netty信道与用户id要一一对应
 */
public class ChannelHandlerPool {

    private ChannelHandlerPool() {  }

    /**
     * 保存当前的所有信道
     */
    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 信道与用户关联
     */
    private static final Map<Long, ChannelId> userChannelIdMap = new ConcurrentHashMap<>(32);
    private static final Map<Long, List<ChannelId>> companyChannelIdListMap = new ConcurrentHashMap<>(32);
    private static final Map<Long, List<ChannelId>> projectChannelIdListMap = new ConcurrentHashMap<>(32);


    /**
     * 客户端链接到netty服务时，保存该用户的信道
     *
     * @param channel 信道
     * @author 罗贤超
     */
    public static void saveChannel(Long userId, Channel channel) {
        channelGroup.add(channel);
        userChannelIdMap.put(userId, channel.id());
    }

    public static void saveChannelByCompanyId(Long companyId, Channel channel) {
        channelGroup.add(channel);
        List<ChannelId> channelByCompanyId = getChannelByCompanyId(companyId);
        channelByCompanyId.add(channel.id());
        companyChannelIdListMap.put(companyId, channelByCompanyId);
    }

    public static List<ChannelId> getChannelByCompanyId(Long companyId) {
        if (companyChannelIdListMap.containsKey(companyId)){
            return companyChannelIdListMap.get(companyId);
        }
        return new ArrayList<>();
    }

    public static void saveChannelByProjectId(Long projectId, Channel channel) {
        channelGroup.add(channel);
        List<ChannelId> channelByProjectId = getChannelByProjectId(projectId);
        channelByProjectId.add(channel.id());
        projectChannelIdListMap.put(projectId, channelByProjectId);
    }

    public static List<ChannelId> getChannelByProjectId(Long projectId) {
        if (projectChannelIdListMap.containsKey(projectId)){
            return projectChannelIdListMap.get(projectId);
        }
        return new ArrayList<>();
    }

    public static List<Channel> getChannelList(List<ChannelId> ChannelIdList) {
        List<Channel> channelList = new ArrayList<>();
        for (ChannelId channelId : ChannelIdList) {
            if (channelId != null){
                Channel channel = channelGroup.find(channelId);
                if (channel != null) {
                    channelList.add(channel);
                }
            }
        }
        return channelList;
    }


    /**
     * 客户端关闭连接时，移除该用户的信道
     *
     * @param channel 信道
     * @author 罗贤超
     */
    public static void removeChannel(Channel channel) {
        // 移除信道池里面的信道
        boolean flag = channelGroup.remove(channel);

        // 移除用户与信道对应关系
        // 信道还存在才去移除
        if (flag){
            ChannelId removeChannelId = channel.id();
            // 移除用户 Map<Long, ChannelId> userChannelIdMap
            Iterator<Map.Entry<Long, ChannelId>> iterator = userChannelIdMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, ChannelId> entry = iterator.next();
                ChannelId value = entry.getValue();
                if (value.equals(removeChannelId)) {
                    iterator.remove();
                    break;
                }
            }

            // 移除公司 Map<Long, List<ChannelId>> companyChannelIdListMap
            for (Iterator<Map.Entry<Long, List<ChannelId>>> iterator2 = companyChannelIdListMap.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<Long, List<ChannelId>> next = iterator2.next();
                List<ChannelId> channelIdList = next.getValue();
                channelIdList.removeIf(channelId -> channelId.equals(removeChannelId));

                // 如果 channelIdList 为空，可以选择在此处将 entry 从 map 中删除
                // if (channelIdList.isEmpty()) {
                //     iterator.remove();
                // }
            }


            // 移除项目 Map<Long, List<ChannelId>> projectChannelIdListMap
            for (Iterator<Map.Entry<Long, List<ChannelId>>> iterator2 = projectChannelIdListMap.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<Long, List<ChannelId>> next = iterator2.next();
                List<ChannelId> channelIdList = next.getValue();
                channelIdList.removeIf(channelId -> channelId.equals(removeChannelId));
            }

        }
    }

    /**
     * 获取信道
     *
     * @param id 信道id
     * @return 信道
     * @author 罗贤超
     */
    public static Channel getChannel(ChannelId id) {
        return channelGroup.find(id);
    }

    /**
     * 获取某个用户的信道
     *
     * @param userId userId
     * @return 信道
     * @author 罗贤超
     */
    public static Channel getChannel(Long userId) {
        if (userChannelIdMap.containsKey(userId)){
            ChannelId channelId = userChannelIdMap.get(userId);
            return channelGroup.find(channelId);
        }
        //todo 保存聊天记录当用户上线的时候发给他
        return null;
    }

    /**
     * 获取所有信道
     */
    public static List<Channel> getAllChannel() {
        return new ArrayList<>(channelGroup);
    }

}
