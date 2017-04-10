package com.jt.jsoup.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.jsoup.pojo.Item;

public class JDUtil {
	private static final Logger log = Logger.getLogger(JDUtil.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	/*
	 * 1.获取所有的三级分类
	 * 2.获取某个分类的页数
	 * 3.拿到分类下所有分页连接
	 * 4.获取某个商品分类分页下所有的商品id，连接
	 * 5.获取链接的商品详情
	 */
	
	//获取所有三级分类
	public static List<String> getAllCat3() throws IOException{
		//获取所有分类页面
		List<String> cartList = new ArrayList<String>();
		String url = "https://www.jd.com/allSort.aspx";
		Document doc = Jsoup.connect(url).get();
		//抓取所有的a标签
		Elements eles = doc.select(".clearfix dd a");
		log.info("三级分类总数："+eles.size());//1259
		
		for(Element ele : eles) {
			String name = ele.text();
			url = ele.attr("href");
			if(url.startsWith("//list.jd.com/list.html?cat=")){
				cartList.add("http:"+url);
			}	
			log.info(name +"-"+url);
		}
		log.info("规则的三级分类总数："+cartList.size());//1183
		return cartList;
		
	}
	
	//获取分类的页数
	public static Integer getPages(String cartUrl){
		try {
			Elements eles = Jsoup.connect(cartUrl).get().select("div#J_topPage span.fq-text i");
			Element ele = eles.get(0);
			String count = ele.text();
			log.info(cartUrl+"-"+count);
			return Integer.parseInt(count);
		} catch (Exception e) {
			log.error("[page error]"+cartUrl);//另外的程序搜索error异常,对这些个别连接重新抓取
			e.printStackTrace();
		}
		return 0;//忽略掉这个分类，继续爬取
	}
	//获取某个分类的所有页的链接
	public static List<String> getCartPageUrl(String cartUrl){
		List<String> cartPageUrlList = new ArrayList<String>();
		Integer pageNum = getPages(cartUrl);
		for(int i=1;i<=pageNum;i++){
			String url = cartUrl +"&page="+i;
			log.info(url);
			cartPageUrlList.add(url);
		}
		return cartPageUrlList;
	}
	
	//获取一个分页面的所有商品的id连接
	public static List<String> getItemUrl(String cartPageUrl) throws IOException{
		List<String> itemUrlList = new ArrayList<String>();
		Elements eles = Jsoup.connect(cartPageUrl).get()
		.select(".gl-i-wrap")
		.select(".j-sku-item div.p-img a[href]");
		
		for(Element ele : eles){
			String itemUrl = "http:"+ele.attr("href");
			itemUrlList.add(itemUrl);
			log.info(itemUrl);
		}
		
		return itemUrlList;
		
	}
	//获取商品详情
	public static Item getItem(String itemUrl){
		Item _item = new Item();
		Long itemId = Long.parseLong(itemUrl.substring(itemUrl.lastIndexOf("/")+1,itemUrl.length()-5));
		_item.setId(itemId);
		_item.setTitle(getTitle(itemUrl));
		//设置价格
		_item.setPrice(getItemPrice(itemId));
		//设置图片
		_item.setSellPoint(getSellPoint(itemId));
		_item.setDesc(getItemDesc(itemId));
		log.info(_item); 
		return _item;
	}
	
	//获取商品名称
	public static String getTitle(String itemUrl){
		
		try {
			Document doc = Jsoup.connect(itemUrl).get();
			Element ele = doc.select(".itemInfo-wrap .sku-name").get(0);
			return ele.text();
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	//获取某个商品的架构
	private static Long getItemPrice(Long itemId){
		try {
			String url = "http://p.3.cn/prices/mgets?skuIds=J_"+itemId;
			String json = Jsoup.connect(url).ignoreContentType(true).execute().body();
			JsonNode jsonNode = MAPPER.readTree(json);
			
			//解析完数组，获取数组第一条数据，获取它的p元素值
			Double price = jsonNode.get(0).get("p").asDouble();
			return Math.round(price*100);	//乘以100，四舍五入
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return 0L;		
	}
	//获取某个商品的图片
	private static String getImage(String itemUrl){
		try {
			Document doc = Jsoup.connect(itemUrl).get();
			Elements eles = doc.select("ul li img");
			String image = "";
			for(Element ele : eles){
				image += ele.attr("src") + ",";
			}
			image = image.substring(0, image.length()-1);
			
			return image;
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}	
	
	//获取某个商品的卖点
	private static String getSellPoint(Long itemId){
		String url = "http://ad.3.cn/ads/mgets?skuids=AD_" + itemId;
		try {
			String json = Jsoup.connect(url).ignoreContentType(true).execute().body();
			JsonNode jsonNode = MAPPER.readTree(json);
			String sellPoint = jsonNode.get(0).get("ad").asText();
			return sellPoint;
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	//获取商品介绍
	private static String getItemDesc(Long itemId){
		String url = "http://d.3.cn/desc/" + itemId;
		try {
			String jsonp = Jsoup.connect(url).ignoreContentType(true).execute().body();
			String json = jsonp.substring(9, jsonp.length()-1);	//把函数名去掉
			JsonNode jsonNode = MAPPER.readTree(json);
			String desc = jsonNode.get("content").asText();
			return desc;
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
