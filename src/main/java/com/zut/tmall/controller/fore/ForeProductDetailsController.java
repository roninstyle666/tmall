package com.zut.tmall.controller.fore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zut.tmall.controller.BaseController;
import com.zut.tmall.entity.*;
import com.zut.tmall.service.*;
import com.zut.tmall.util.PageUtil;
import com.zut.tmall.entity.*;
import com.zut.tmall.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 产品详情页
 */
@Controller
public class ForeProductDetailsController extends BaseController {
    @Resource(name = "productService")
    private ProductService productService;
    @Resource(name = "userService")
    private UserService userService;
    @Resource(name = "productImageService")
    private ProductImageService productImageService;
    @Resource(name = "categoryService")
    private CategoryService categoryService;
    @Resource(name = "propertyValueService")
    private PropertyValueService propertyValueService;
    @Resource(name = "propertyService")
    private PropertyService propertyService;

    @Resource(name = "productOrderItemService")
    private ProductOrderItemService productOrderItemService;

    //转到前台天猫-产品详情页
    @RequestMapping(value = "product/{pid}", method = RequestMethod.GET)
    public String goToPage(HttpSession session, Map<String, Object> map,
                           @PathVariable("pid") String pid /*产品ID*/) {
        logger.info("检查用户是否登录");
        Object userId = checkUser(session);
        if (userId != null) {
            logger.info("获取用户信息");
            User user = userService.get(Integer.parseInt(userId.toString()));
            map.put("user", user);
        }
        logger.info("获取产品ID");
        Integer product_id = Integer.parseInt(pid);
        logger.info("获取产品信息");
        Product product = productService.get(product_id);
        if (product == null || product.getProduct_isEnabled() == 1) {
            return "redirect:/404";
        }
        logger.info("获取产品子信息-分类信息");
        product.setProduct_category(categoryService.get(product.getProduct_category().getCategory_id()));
        logger.info("获取产品子信息-预览图片信息");
        List<ProductImage> singleProductImageList = productImageService.getList(product_id, (byte) 0, null);
        product.setSingleProductImageList(singleProductImageList);
        logger.info("获取产品子信息-详情图片信息");
        List<ProductImage> detailsProductImageList = productImageService.getList(product_id, (byte) 1, null);
        product.setDetailProductImageList(detailsProductImageList);
        logger.info("获取产品子信息-产品属性值信息");
        List<PropertyValue> propertyValueList = propertyValueService.getList(new PropertyValue().setPropertyValue_product(product), null);
        logger.info("获取产品子信息-分类信息对应的属性列表");
        List<Property> propertyList = propertyService.getList(new Property().setProperty_category(product.getProduct_category()), null);
        logger.info("属性列表和属性值列表合并");
        for (Property property : propertyList) {
            for (PropertyValue propertyValue : propertyValueList) {
                if (property.getProperty_id().equals(propertyValue.getPropertyValue_property().getProperty_id())) {
                    List<PropertyValue> property_value_item = new ArrayList<>(1);
                    property_value_item.add(propertyValue);
                    property.setPropertyValueList(property_value_item);
                    break;
                }
            }
        }
        logger.info("获取分类列表");
        List<Category> categoryList = categoryService.getList(null, new PageUtil(0, 3));

        map.put("categoryList", categoryList);
        map.put("propertyList", propertyList);
        map.put("product", product);
        logger.info("转到前台-产品详情页");
        return "fore/productDetailsPage";
    }
    //按产品ID加载产品属性列表-ajax
    @Deprecated
    @ResponseBody
    @RequestMapping(value = "property/{pid}", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    public String loadProductPropertyList(@PathVariable("pid") String pid/*产品ID*/) {
        logger.info("获取产品ID");
        Integer product_id = Integer.parseInt(pid);

        logger.info("获取产品详情-属性值信息");
        Product product = new Product();
        product.setProduct_id(product_id);
        List<PropertyValue> propertyValueList = propertyValueService.getList(new PropertyValue().setPropertyValue_product(product), null);

        logger.info("获取产品详情-分类信息对应的属性列表");
        List<Property> propertyList = propertyService.getList(new Property().setProperty_category(product.getProduct_category()), null);

        logger.info("属性列表和属性值列表合并");
        for (Property property : propertyList) {
            for (PropertyValue propertyValue : propertyValueList) {
                if (property.getProperty_id().equals(propertyValue.getPropertyValue_property().getProperty_id())) {
                    List<PropertyValue> property_value_item = new ArrayList<>(1);
                    property_value_item.add(propertyValue);
                    property.setPropertyValueList(property_value_item);
                    break;
                }
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("propertyList", JSONArray.parseArray(JSON.toJSONString(propertyList)));

        return jsonObject.toJSONString();
    }

}
