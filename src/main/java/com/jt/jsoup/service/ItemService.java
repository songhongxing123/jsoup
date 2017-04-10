package com.jt.jsoup.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.jsoup.pojo.Item;
import com.jt.jsoup.pojo.ItemDesc;
import com.jt.jsoup.mapper.ItemDescMapper;
import com.jt.jsoup.mapper.ItemMapper;

@Service
public class ItemService extends BaseService<Item>{

	@Autowired
	private ItemMapper itemMapper;
	@Autowired
	private ItemDescMapper itemDescMapper;
	
	//新增
	public void saveItem(Item item,String desc){
		item.setCreated(new Date());
		item.setUpdated(item.getCreated());
		item.setStatus(1);
		itemMapper.insertSelective(item);
		
		ItemDesc itemDesc = new ItemDesc();
		itemDesc.setItemId(item.getId());
		itemDesc.setItemDesc(desc);
		itemDesc.setCreated(item.getCreated());
		itemDesc.setUpdated(item.getCreated());
		itemDescMapper.insertSelective(itemDesc);
	}
	
}
