package com.jt.jsoup.controller;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jt.common.service.RedisService;
import com.jt.jsoup.pojo.Item;
import com.jt.jsoup.service.ItemService;
import com.jt.jsoup.util.JDUtil;

@Controller
public class ItemController {
	@Autowired
	private RedisService redisService;
	@Autowired
	private ItemService itemService;
	
	//初始化爬虫，把所有的item链接放入redis(分片)中
	@RequestMapping("/init")
	public String init() throws IOException{
		//for(String catePageUrl : JDUtil.getAllCat3()){
			String catePageUrl = "http://list.jd.com/list.html?cat=670,677,678";
			for(String url : JDUtil.getItemUrl(catePageUrl)){
				redisService.set(url, url);
			}
//			break;             
//		}
			return "index";
	}

	@RequestMapping("/go")
	public String go() throws IOException{
		//for(String catePageUrl : JDUtil.getAllCat3()){
		String catePageUrl = "http://list.jd.com/list.html?cat=670,677,678";
			for(String url : JDUtil.getItemUrl(catePageUrl)){
				String itemUrl = redisService.get(url);
				//如果redis中没有了，代表次商品已经处理过了
				if(StringUtils.isNotEmpty(itemUrl)){
					Item item = JDUtil.getItem(itemUrl);
					itemService.saveItem(item,item.getDesc()); 
					redisService.del(url);//业务完成，删除这个url
				}
			}
//			break;
//		}
			return "index";	
	}
}
