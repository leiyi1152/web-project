package com.icloud.font.bsactivity.web;

import com.alibaba.fastjson.JSON;
import com.icloud.annotation.LoginUser;
import com.icloud.common.R;
import com.icloud.common.util.StringUtil;
import com.icloud.modules.bsactivity.entity.BsactivityAd;
import com.icloud.modules.bsactivity.entity.BsactivityShop;
import com.icloud.modules.bsactivity.service.BsactivityAdService;
import com.icloud.modules.bsactivity.service.BsactivityShopService;
import com.icloud.modules.wx.entity.WxUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * 扫码兑换
 */
@Controller
@RequestMapping("/frontpage/bsactivity/shop")
public class ShopController {

    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @Autowired
    private BsactivityShopService bsactivityShopService;
    @Autowired
    private BsactivityAdService bsactivityAdService;


    /**
     * 进入店铺列表
     * @return
     */
    @RequestMapping("/list")
    public String list(){
        return "modules/front/bsactivity/shoplist";
    }

    /**
     * 获取店铺列表信息
     * @return
     */
    @RequestMapping("/listinfo")
    @ResponseBody
    public R listinfo(@LoginUser WxUser wxUser,BsactivityShop bsactivityShop){
        try {
            log.info("listinfo_param_bsactivityShop="+JSON.toJSONString(bsactivityShop));
            log.info("listinfo_param_wxUser="+JSON.toJSONString(wxUser));
            if(bsactivityShop.getLat()!=null && bsactivityShop.getLnt()!=null){
                wxUser.setLat(bsactivityShop.getLat().toString());
                wxUser.setLnt(bsactivityShop.getLnt().toString());
                request.getSession().setAttribute("wx_user",wxUser);
            }else if(StringUtil.checkStr(wxUser.getLat()) && StringUtil.checkStr(wxUser.getLnt())){
                bsactivityShop.setLat(new BigDecimal(wxUser.getLat()));
                bsactivityShop.setLnt(new BigDecimal(wxUser.getLnt()));
            }else {
                bsactivityShop.setLat(new BigDecimal("23.90"));
                bsactivityShop.setLnt(new BigDecimal("106.62"));
            }
            log.info("listinfo_params_query="+ JSON.toJSONString(bsactivityShop));
            List<BsactivityShop> list = bsactivityShopService.selectByposition(bsactivityShop);
            log.info("listinfo_result="+ JSON.toJSONString(list));
            return R.ok().put("shoplist",list);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("queryGoodsByqcode_result="+ e.getMessage());
            return R.error(e.getMessage());
        }

    }

    /**
     * 获取店铺列表信息
     * @return
     */
    @RequestMapping("/adlist")
    @ResponseBody
    public R adlist(@LoginUser WxUser wxUser){
        try {
            List<BsactivityAd> list = bsactivityAdService.list();
            log.info("adlist_result="+ JSON.toJSONString(list));
            return R.ok().put("adlist",list);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("adlist="+ e.getMessage());
            return R.error(e.getMessage());
        }

    }
}