package guat.lxy.bigdata.smartshop.controller;

import guat.lxy.bigdata.smartshop.entity.User;
import guat.lxy.bigdata.smartshop.service.ProductFavoriteService;
import guat.lxy.bigdata.smartshop.util.CurrentUserUtil;
import guat.lxy.bigdata.smartshop.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private ProductFavoriteService favoriteService;

    @Autowired
    private CurrentUserUtil currentUserUtil;

    @PostMapping("/toggle/{productId}")
    @ResponseBody
    public Map<String, Object> toggle(@PathVariable Integer productId, Principal principal) {
        User user = currentUserUtil.getCurrentUser(principal);
        boolean favorited = favoriteService.toggle(productId, user.getId());
        return favorited ? Result.success("已收藏") : Result.success("已取消收藏");
    }
}
