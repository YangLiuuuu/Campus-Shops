package com.yang.web.shopadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yang.dto.ShopExecution;
import com.yang.entity.*;
import com.yang.enums.ShopStateEnum;
import com.yang.service.AreaService;
import com.yang.service.ShopCategoryService;
import com.yang.service.ShopService;
import com.yang.util.CodeUtil;
import com.yang.util.HttpServletRequestUtil;
import com.yang.util.ImageUtil;
import com.yang.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shopadmin")
public class ShopManagementController {

    @Autowired
    private ShopService shopService;

    @Autowired
    ShopCategoryService shopCategoryService;

    @Autowired
    AreaService areaService;

    @RequestMapping(value = "/getshopinitinfo",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object>getShopInitInfo(){
        Map<String,Object>modelMap = new HashMap<>();
        List<ShopCategory>shopCategories = new ArrayList<>();
        List<Area>areaList = new ArrayList<>();
        try {
            shopCategories = shopCategoryService.getShopCategoryList(new ShopCategory());
            areaList = areaService.getAreaList();
            modelMap.put("shopCategoryList",shopCategories);
            modelMap.put("areaList",areaList);
            modelMap.put("success",true);
        }catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
        }
        return modelMap;
    }

    @RequestMapping(value = "registershop",method = RequestMethod.POST)
    @ResponseBody
    private Map<String,Object>registerShop(HttpServletRequest request){
        //1.接收并转换相应的参数，包括店铺信息以及图片信息
        Map<String,Object>modelMap = new HashMap<>();
        if (!CodeUtil.checkVerifyCode(request)){
            modelMap.put("success",false);
            modelMap.put("errMsg","验证码输入错误");
            return modelMap;
        }
        String shopStr = HttpServletRequestUtil.getString(request,"shopStr");
        ObjectMapper mapper = new ObjectMapper();
        Shop shop = null;
        try {
            shop = mapper.readValue(shopStr,Shop.class);
        }catch (IOException e){
            e.printStackTrace();
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
            return modelMap;
        }
        CommonsMultipartFile shopImg = null;
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        if (commonsMultipartResolver.isMultipart(request)){
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            shopImg = (CommonsMultipartFile) multipartHttpServletRequest.getFile("shopImg");
        }else{
            modelMap.put("success",false);
            modelMap.put("errMsg","上传图片不能为空");
            return modelMap;
        }
        //2.注册店铺
        if (shop != null && shopImg!=null){
            PersonInfo owner = new PersonInfo();
            //session todo
            owner.setUserId(1L);
            shop.setOwnerId(1L);
            ShopExecution se = null;
            try {
                se = shopService.addShop(shop,shopImg.getInputStream(),shopImg.getOriginalFilename());
                if (se.getState() == ShopStateEnum.CHECK.getState()){
                    modelMap.put("success",true);
                }else {
                    modelMap.put("success",false);
                    modelMap.put("errMsg",se.getStateInfo());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return modelMap;
        }else{
            modelMap.put("success",false);
            modelMap.put("errMsg","请输入店铺信息");
            return modelMap;
        }
    }

//    private static void inputStreamtoFile(InputStream ins, File file){
//        OutputStream os = null;
//        try {
//            os = new FileOutputStream(file);
//            int bytesRead = 0;
//            byte[] buffer = new byte[1024];
//            while((bytesRead = ins.read(buffer))!=-1){
//                os.write(buffer,0,bytesRead);
//            }
//        }catch (Exception e){
//            throw new RuntimeException("调用inputStreamToFile产生异常:"+e.getMessage());
//        }finally {
//            try {
//                if (os != null) {
//                    os.close();
//                }
//                if (ins != null) {
//                    ins.close();
//                }
//            }catch (IOException e){
//                throw new RuntimeException("调用inputStreamToFile关闭IO产生异常:"+e.getMessage());
//            }
//        }
//    }
}
