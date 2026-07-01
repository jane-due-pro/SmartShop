package guat.lxy.bigdata.smartshop.service.impl;

import guat.lxy.bigdata.smartshop.entity.ProductComment;
import guat.lxy.bigdata.smartshop.mapper.ProductCommentMapper;
import guat.lxy.bigdata.smartshop.service.ProductCommentService;
import guat.lxy.bigdata.smartshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductCommentServiceImpl implements ProductCommentService {

    @Autowired
    private ProductCommentMapper commentMapper;

    @Autowired
    private UserService userService;

    @Override
    public List<ProductComment> findByProductId(Integer productId) {
        List<ProductComment> all = commentMapper.findByProductId(productId);

        // 分离顶级评论和子回复，组装父子结构
        List<ProductComment> topComments = new ArrayList<>();
        Map<Integer, List<ProductComment>> childMap = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(ProductComment::getParentId));

        for (ProductComment c : all) {
            if (c.getParentId() == null) {
                c.setReplies(childMap.getOrDefault(c.getId(), Collections.emptyList()));
                topComments.add(c);
            }
        }
        return topComments;
    }

    @Override
    public boolean save(ProductComment comment) {
        if (comment.getParentId() == null && comment.getRating() == null) {
            comment.setRating(5);
        }
        return commentMapper.insert(comment) > 0;
    }

    @Override
    @Transactional
    public boolean deleteById(Integer id, String currentUsername) {
        ProductComment comment = commentMapper.findById(id);
        if (comment == null) {
            return false;
        }
        // 权限校验：ADMIN可删任意，普通用户只能删自己的
        boolean isAdmin = userService.getUserRoles(
                userService.findByUsername(currentUsername).getId())
                .stream().anyMatch(r -> r.getRole().equals("ROLE_admin"));
        if (!isAdmin && !comment.getUsername().equals(currentUsername)) {
            return false;
        }
        // 先删子回复的点赞 → 再删子回复 → 再删自身的点赞 → 最后删自身
        commentMapper.deleteLikesByParentId(id);
        commentMapper.deleteByParentId(id);
        commentMapper.deleteLikesByCommentId(id);
        return commentMapper.deleteById(id) > 0;
    }

    @Override
    public int countByProductId(Integer productId) {
        return commentMapper.countByProductId(productId);
    }

    @Override
    public Double avgRatingByProductId(Integer productId) {
        return commentMapper.avgRatingByProductId(productId);
    }

    @Override
    public boolean toggleLike(Integer commentId, Integer userId) {
        int count = commentMapper.countLikes(commentId);
        // 尝试删除，如果没删到则插入（简易 toggle）
        int deleted = commentMapper.deleteLike(commentId, userId);
        if (deleted > 0) {
            return false; // 取消点赞
        }
        commentMapper.insertLike(commentId, userId);
        return true; // 点赞成功
    }

    @Override
    public void fillLikeInfo(List<ProductComment> comments, Integer userId) {
        if (comments == null || comments.isEmpty()) return;

        // 收集所有评论ID（顶级 + 子回复）
        List<Integer> allIds = new ArrayList<>();
        for (ProductComment c : comments) {
            allIds.add(c.getId());
            if (c.getReplies() != null) {
                for (ProductComment r : c.getReplies()) {
                    allIds.add(r.getId());
                }
            }
        }
        if (allIds.isEmpty()) return;

        // 批量查点赞数（逐个查，简单可靠）
        for (Integer id : allIds) {
            // 会在下面 fillSingle 里处理
        }

        // 批量查当前用户已点赞的评论ID
        Set<Integer> likedIds = new HashSet<>();
        if (userId != null) {
            String idsStr = allIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            likedIds = new HashSet<>(commentMapper.findLikedCommentIds(userId, idsStr));
        }

        // 填充
        for (ProductComment c : comments) {
            c.setLikeCount(commentMapper.countLikes(c.getId()));
            c.setLiked(likedIds.contains(c.getId()));
            if (c.getReplies() != null) {
                for (ProductComment r : c.getReplies()) {
                    r.setLikeCount(commentMapper.countLikes(r.getId()));
                    r.setLiked(likedIds.contains(r.getId()));
                }
            }
        }
    }
}
