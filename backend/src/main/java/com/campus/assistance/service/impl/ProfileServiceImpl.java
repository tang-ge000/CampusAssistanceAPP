package com.campus.assistance.service.impl;

import com.campus.assistance.dto.ProfileResponse;
import com.campus.assistance.entity.Product;
import com.campus.assistance.model.*;
import com.campus.assistance.repository.*;
import com.campus.assistance.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProfileServiceImpl implements ProfileService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private BrowseHistoryRepository browseHistoryRepository;
    
    @Autowired
    private DraftRepository draftRepository;
    
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private PurchaseRecordRepository purchaseRecordRepository;
    
    @Autowired
    private WantedItemRepository wantedItemRepository;
    
    @Autowired
    private ExpressOrderRepository expressOrderRepository;

    @Override
    public ProfileResponse getProfile(User user) {
        log.info("获取用户资料: {}", user.getUsername());
        
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setStudentId(user.getStudentId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        
        // 获取个人资料
        userProfileRepository.findByUser(user).ifPresent(profile -> {
            response.setAvatar(profile.getAvatar());
            response.setPhone(profile.getPhone());
            response.setAddress(profile.getAddress());
            response.setBio(profile.getBio());
        });
        
        // 获取未读消息数量
        Long unreadCount = messageRepository.countByReceiverAndIsRead(user, false);
        response.setUnreadMessageCount(unreadCount);
        
        return response;
    }

    @Override
    public ProfileResponse updateProfile(User user, Map<String, Object> profileData) {
        log.info("更新用户资料: {}", user.getUsername());
        log.info("接收到的资料数据: {}", profileData);
        
        // 获取或创建用户资料
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElse(new UserProfile());
        
        profile.setUser(user);
        
        // 更新资料
        if (profileData.containsKey("avatar")) {
            profile.setAvatar((String) profileData.get("avatar"));
        }
        
        if (profileData.containsKey("phone")) {
            profile.setPhone((String) profileData.get("phone"));
        }
        
        if (profileData.containsKey("address")) {
            profile.setAddress((String) profileData.get("address"));
        }
        
        if (profileData.containsKey("bio")) {
            profile.setBio((String) profileData.get("bio"));
        }
        
        // 保存资料
        userProfileRepository.save(profile);
        
        // 更新用户基本信息（新增）
        boolean userUpdated = false;
        
        if (profileData.containsKey("username")) {
            String newUsername = (String) profileData.get("username");
            log.info("尝试更新用户名: 当前={}, 新={}", user.getUsername(), newUsername);
            if (newUsername != null && !newUsername.trim().isEmpty() && !newUsername.equals(user.getUsername())) {
                log.info("用户名将被更新为: {}", newUsername);
                user.setUsername(newUsername);
                userUpdated = true;
            }
        }
        
        if (profileData.containsKey("email")) {
            String newEmail = (String) profileData.get("email");
            log.info("尝试更新邮箱: 当前={}, 新={}", user.getEmail(), newEmail);
            if (newEmail != null && !newEmail.trim().isEmpty() && !newEmail.equals(user.getEmail())) {
                // 检查邮箱是否已被其他用户使用
                if (userRepository.existsByEmailAndIdNot(newEmail, user.getId())) {
                    log.warn("邮箱已被其他用户使用: {}", newEmail);
                    throw new RuntimeException("该邮箱已被其他用户使用");
                }
                log.info("邮箱将被更新为: {}", newEmail);
                user.setEmail(newEmail);
                userUpdated = true;
            }
        }
        
        // 如果有更新用户基本信息，保存用户
        if (userUpdated) {
            try {
                User savedUser = userRepository.save(user);
                log.info("用户基本信息已更新并保存到数据库: id={}, username={}", savedUser.getId(), savedUser.getUsername());
            } catch (Exception e) {
                log.error("保存用户信息时发生错误: ", e);
                throw e;
            }
        } else {
            log.info("用户基本信息没有变化，不需要更新");
        }
        
        // 返回更新后的资料
        return getProfile(user);
    }

    @Override
    public List<Map<String, Object>> getMessages(User user) {
        log.info("获取用户消息: {}", user.getUsername());
        
        // 获取用户的消息
        List<Message> messages = messageRepository.findByReceiverOrderByCreatedAtDesc(user);
        
        // 将消息转换为Map
        return messages.stream().map(message -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", message.getId());
            map.put("content", message.getContent());
            map.put("type", message.getMessageType());
            map.put("isRead", message.getIsRead());
            map.put("time", message.getCreatedAt());
            
            // 如果是私信，添加发送者信息
            if (message.getSender() != null) {
                Map<String, Object> sender = new HashMap<>();
                sender.put("id", message.getSender().getId());
                sender.put("username", message.getSender().getUsername());
                map.put("sender", sender);
            }
            
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getBrowseHistory(User user) {
        log.info("获取用户浏览历史: {}", user.getUsername());
        
        // 获取用户的浏览历史
        List<BrowseHistory> history = browseHistoryRepository.findByUserOrderByBrowseTimeDesc(user);
        
        // 将浏览历史转换为Map
        return history.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("type", item.getItemType());
            map.put("itemId", item.getItemId());
            map.put("time", item.getBrowseTime());
            
            // 根据类型获取额外信息
            if ("product".equals(item.getItemType())) {
                productRepository.findById(item.getItemId()).ifPresent(product -> {
                    map.put("title", product.getTitle());
                    map.put("price", product.getPrice());
                    map.put("image", product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : null);
                });
            } else if ("express".equals(item.getItemType())) {
                expressOrderRepository.findById(item.getItemId()).ifPresent(order -> {
                    map.put("pickup", order.getPickupLocation());
                    map.put("destination", order.getDestination());
                    map.put("reward", order.getReward());
                });
            }
            
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDrafts(User user) {
        log.info("获取用户草稿: {}", user.getUsername());
        
        // 获取用户的草稿
        List<Draft> drafts = draftRepository.findByUserOrderByUpdatedAtDesc(user);
        
        // 将草稿转换为Map
        return drafts.stream().map(draft -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", draft.getId());
            map.put("type", draft.getDraftType());
            map.put("title", draft.getTitle());
            map.put("content", draft.getContent());
            map.put("updatedAt", draft.getUpdatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getFavorites(User user) {
        log.info("获取用户收藏: {}", user.getUsername());
        
        // 获取用户的收藏
        List<Favorite> favorites = favoriteRepository.findByUserOrderByCreatedAtDesc(user);
        
        // 将收藏转换为Map
        return favorites.stream().map(favorite -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", favorite.getId());
            map.put("type", favorite.getItemType());
            map.put("itemId", favorite.getItemId());
            map.put("createdAt", favorite.getCreatedAt());
            
            // 根据类型获取额外信息
            if ("product".equals(favorite.getItemType())) {
                productRepository.findById(favorite.getItemId()).ifPresent(product -> {
                    map.put("title", product.getTitle());
                    map.put("price", product.getPrice());
                    map.put("status", product.getStatus());
                    map.put("image", product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : null);
                });
            } else if ("express".equals(favorite.getItemType())) {
                expressOrderRepository.findById(favorite.getItemId()).ifPresent(order -> {
                    map.put("pickup", order.getPickupLocation());
                    map.put("destination", order.getDestination());
                    map.put("reward", order.getReward());
                    map.put("status", order.getStatus());
                });
            }
            
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getPurchasedItems(User user) {
        log.info("获取用户已购买商品: {}", user.getUsername());
        
        // 获取用户的购买记录
        List<PurchaseRecord> records = purchaseRecordRepository.findByBuyerOrderByPurchaseTimeDesc(user);
        
        // 将购买记录转换为Map
        return records.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("productId", record.getProduct().getId());
            map.put("title", record.getProduct().getTitle());
            map.put("price", record.getPrice());
            map.put("purchaseTime", record.getPurchaseTime());
            map.put("status", record.getStatus());
            map.put("image", record.getProduct().getImages() != null && !record.getProduct().getImages().isEmpty() ? record.getProduct().getImages().get(0) : null);
            
            // 添加卖家信息
            Map<String, Object> seller = new HashMap<>();
            seller.put("id", record.getSeller().getId());
            seller.put("username", record.getSeller().getUsername());
            map.put("seller", seller);
            
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getWantedItems(User user) {
        log.info("获取用户求购信息: {}", user.getUsername());
        
        // 获取用户的求购信息
        List<WantedItem> items = wantedItemRepository.findByUserOrderByCreatedAtDesc(user);
        
        // 将求购信息转换为Map
        return items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("title", item.getTitle());
            map.put("description", item.getDescription());
            map.put("maxBudget", item.getMaxBudget());
            map.put("status", item.getStatus());
            map.put("createdAt", item.getCreatedAt());
            map.put("updatedAt", item.getUpdatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getPublishedItems(User user) {
        log.info("获取用户发布商品: {}", user.getUsername());
        
        // 获取用户发布的商品，使用已有的方法
        List<Product> products = productRepository.findByUser(user);
        
        // 根据发布时间排序（使用与entity.Product兼容的方法）
        products.sort((p1, p2) -> {
            if (p1.getPublishTime() == null && p2.getPublishTime() == null) return 0;
            if (p1.getPublishTime() == null) return 1;
            if (p2.getPublishTime() == null) return -1;
            return p2.getPublishTime().compareTo(p1.getPublishTime());
        });
        
        // 将商品转换为Map
        return products.stream().map(product -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", product.getId());
            map.put("title", product.getTitle());
            map.put("description", product.getDescription());
            map.put("price", product.getPrice());
            map.put("originalPrice", product.getOriginalPrice());
            map.put("conditionLevel", product.getConditionLevel());
            // 仅使用entity.Product中存在的字段
            // map.put("isFixedPrice", product.getIsFixedPrice());
            map.put("status", product.getStatus());
            map.put("createdAt", product.getPublishTime());
            map.put("updatedAt", product.getLastModified());
            // 修改图片处理逻辑
            map.put("image", product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : null);
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getSoldItems(User user) {
        log.info("获取用户已售商品: {}", user.getUsername());
        
        // 获取用户的售出记录
        List<PurchaseRecord> records = purchaseRecordRepository.findBySellerOrderByPurchaseTimeDesc(user);
        
        // 将售出记录转换为Map
        return records.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("productId", record.getProduct().getId());
            map.put("title", record.getProduct().getTitle());
            map.put("price", record.getPrice());
            map.put("purchaseTime", record.getPurchaseTime());
            map.put("status", record.getStatus());
            map.put("image", record.getProduct().getImages() != null && !record.getProduct().getImages().isEmpty() ? record.getProduct().getImages().get(0) : null);
            
            // 添加买家信息
            Map<String, Object> buyer = new HashMap<>();
            buyer.put("id", record.getBuyer().getId());
            buyer.put("username", record.getBuyer().getUsername());
            map.put("buyer", buyer);
            
            return map;
        }).collect(Collectors.toList());
    }
} 