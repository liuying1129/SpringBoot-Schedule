package com.yklis.schedule.misc;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@ServerEndpoint("/websocket/receiptAuditNum")
@Component
public class WebSocketReceiptAuditNum {
	
    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
	//final
	//如果是基本数据类型的变量，则其数值一旦在初始化之后便不能更改
	//如果是引用类型的变量，则在对其初始化之后便不能再让其指向另一个对象.但是对象本身是可以被修改的
    public static final CopyOnWriteArraySet<WebSocketReceiptAuditNum> wsSet = new CopyOnWriteArraySet<>();
      
    /**
     * 有连接时的触发函数
     * @param session.可选参数，session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     * 使用@PathParam注解进行参数获取
     */
	@OnOpen
	public void onOpen(Session session){

        logger.info("WebSocket设备监控客户端已经连接");
        
		this.session = session;
	    
		wsSet.add(this);
	}
	
    //连接关闭时的调用方法
    @OnClose
    public void onClose(){
    	
        logger.info("WebSocket设备监控客户端已经断开");
        wsSet.remove(this);
    }
    
    /**
     * 收到消息时执行
     * @param message.客户端发送过来的消息
     * @param session.可选参数
     * @throws IOException
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {    	
    }
    
    /**
     * 连接错误时执行
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
    	
        logger.info("WebSocket设备监控客户端发生错误");
        error.printStackTrace();
    }
    
    public Session getSession() {
        return session;
    }
}
