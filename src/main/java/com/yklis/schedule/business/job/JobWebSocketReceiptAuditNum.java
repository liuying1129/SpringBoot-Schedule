package com.yklis.schedule.business.job;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.yklis.schedule.misc.WebSocketReceiptAuditNum;
import com.yklis.schedule.util.SpringUtils;

public class JobWebSocketReceiptAuditNum implements Command {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void execute(Map<String, Object> map) {
		
        JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
        
        int receiptAuditNum;
    	try{
    		//sql要求：
    		//1、有且仅有一条记录
    		//2、有且仅有一个字段
    		//3、字段在DB中的类型不限
    		receiptAuditNum = jdbcTemplate.queryForObject("select COUNT(*) from SJ_RK_Fu WITH(NOLOCK) where Audit_Date is null",int.class);
    	}catch(Exception e){
            logger.error("jdbcTemplate.queryForObject失败:"+e.toString());
            return;
    	}
        
        //发送信息
        for (WebSocketReceiptAuditNum wsItem : WebSocketReceiptAuditNum.wsSet) {
            try {
           	
            	wsItem.getSession().getBasicRemote().sendText(String.valueOf(receiptAuditNum));
            } catch (IOException e) {
            	logger.error("待审核耗材入库单数量WebSocket sendText错误");
            }
        }                        
	}
}
