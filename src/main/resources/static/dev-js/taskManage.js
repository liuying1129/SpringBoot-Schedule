	//设立"严格模式"的目的
    //1、消除Javascript语法的一些不合理、不严谨之处，减少一些怪异行为;
    //2、消除代码运行的一些不安全之处，保证代码运行的安全；
	//3、提高编译器效率，增加运行速度；
	//4、为未来新版本的Javascript做好铺垫
    "use strict";
    
function taskOnoff(unid,reserve6){
	
	if(!window.confirm('确定要'+(reserve6==1?'禁用':'启用')+'该任务吗?'))return;

	$.ajax({
		//默认值: true。如果需要发送同步请求，请将此选项设置为 false。注意，同步请求将锁住浏览器，用户其它操作必须等待请求完成才可以执行
		async : true,
		//默认值:"GET".请求方式 ("POST"或 "GET")，注意：其它 HTTP请求方法，如 PUT和 DELETE也可以使用，但仅部分浏览器支持
		type : 'POST',
		//默认值: "application/x-www-form-urlencoded"。发送信息至服务器时内容编码类型
		//默认值适合大多数情况。如果你明确指定$.ajax()的 content-type,那么它必定会发送给服务器（即使没有数据要发送）
		//contentType : "application/x-www-form-urlencoded",//application/json
		url : 'taskOnoff?unid='+unid+'&reserve6='+reserve6,
		//预期服务器返回的数据类型。如果不指定，jQuery将自动根据 HTTP包 MIME信息来智能判断
		dataType : 'json',
		success : function(data) {			
			
			if(!data.success){
				alert('操作失败!');
			}
			location.reload();//刷新页面
		},
		error : function(xhr, textStatus, errorThrown) {
			
			console.log("ajax请求失败,请求:taskOnoff,状态码:"+xhr.status +",状态说明:"+ textStatus+",xhr readyState:"+xhr.readyState);
		}
	});
}

function deleteTask(unid){
	
	if(!window.confirm('确定要删除该任务吗?'))return;
		
	$.ajax({
		//默认值: true。如果需要发送同步请求，请将此选项设置为 false。注意，同步请求将锁住浏览器，用户其它操作必须等待请求完成才可以执行
		async : true,
		//默认值:"GET".请求方式 ("POST"或 "GET")，注意：其它 HTTP请求方法，如 PUT和 DELETE也可以使用，但仅部分浏览器支持
		type : 'POST',
		//默认值: "application/x-www-form-urlencoded"。发送信息至服务器时内容编码类型
		//默认值适合大多数情况。如果你明确指定$.ajax()的 content-type,那么它必定会发送给服务器（即使没有数据要发送）
		//contentType : "application/x-www-form-urlencoded",//application/json
		url : 'deleteTask?unid='+unid,
		//预期服务器返回的数据类型。如果不指定，jQuery将自动根据 HTTP包 MIME信息来智能判断
		dataType : 'json',
		success : function(data) {			
			
			if(!data.success){
				alert('操作失败!');
			}
		},
		error : function(xhr, textStatus, errorThrown) {
			
			console.log("ajax请求失败,请求:deleteTask,状态码:"+xhr.status +",状态说明:"+ textStatus+",xhr readyState:"+xhr.readyState);
		}
	});
}

$(document).ready(function() {
		
	$.ajax({
		//默认值: true。如果需要发送同步请求，请将此选项设置为 false。注意，同步请求将锁住浏览器，用户其它操作必须等待请求完成才可以执行
		async : true,
		//默认值:"GET".请求方式 ("POST"或 "GET")，注意：其它 HTTP请求方法，如 PUT和 DELETE也可以使用，但仅部分浏览器支持
		type : 'POST',
		//默认值: "application/x-www-form-urlencoded"。发送信息至服务器时内容编码类型
		//默认值适合大多数情况。如果你明确指定$.ajax()的 content-type,那么它必定会发送给服务器（即使没有数据要发送）
		//contentType : "application/x-www-form-urlencoded",//application/json
		url : 'queryJobList',
		//预期服务器返回的数据类型。如果不指定，jQuery将自动根据 HTTP包 MIME信息来智能判断
		dataType : 'json',
		success : function(data) {			
			
			$('#jobList').bootstrapTable('load', data.response);
			
			$('#jobList').bootstrapTable({
				
			    data: data.response,
		        columns: [{
			        field: 'Unid',
			        title: '唯一编号',
			        formatter: function formatter(value, row, index, field) {
			        	
			        	return "<a href='#myModal' data-toggle='modal' data-title='修改' data-unid='"+value+"' data-id='"+row.ID+"' data-name='"+row.Name+"' data-remark='"+row.Remark+"' data-reserve='"+row.Reserve+"' data-reserve2='"+row.Reserve2+"' data-reserve3='"+row.Reserve3+"' data-reserve5='"+(typeof row.Reserve5=='undefined'?'':row.Reserve5)+"'>" + value + "</a>";
			        }
			    }, {
			        field: 'ID',
			        title: '编号'
			    }, {
			        field: 'Unid',
			        formatter: function formatter(value, row, index, field) {
			        	
			        	//href=''则点击链接后会刷新自身页面
			        	return "<a href='' onclick='deleteTask("+value+")'>删除</a>";
			        }
			    }, {
			        field: 'Name',
			        title: '名称'
			    }, {
			        field: 'Remark',
			        title: '备注'
			    }, {
			        field: 'Reserve6',
			        formatter: function formatter(value, row, index, field) {
			        	
			        	return "<img src='../images/"+(value==1?'stop':'start')+".png' alt='任务状态' title='"+(value==1?'已启用':'已禁用')+"【"+value+"】,点击"+(value==1?'禁用':'启用')+"' class='hvr-grow' onclick='taskOnoff("+row.Unid+","+value+")' />";
			        }
			    }, {
			        field: 'Reserve',
			        title: 'Class或DataBase'
			    }, {
			        field: 'Reserve2',
			        title: '类名或SQL'
			    }, {
			        field: 'Reserve3',
			        title: 'cron表达式'
			    }, {
			    	field: 'Reserve5',
			        title: 'JDBC连接字符串UNID'
			    }]
			});						
		},
		error : function(xhr, textStatus, errorThrown) {
			
			console.log("ajax请求失败,请求:queryJobList,状态码:"+xhr.status +",状态说明:"+ textStatus+",xhr readyState:"+xhr.readyState);
		}
	});
	
	//模式窗口的shown事件
	$('#myModal').on('shown.bs.modal', function (e) {
		
		var modal_relatedTarget = $(e.relatedTarget);
				
		document.getElementById("myModalTitle").innerHTML = modal_relatedTarget.data("title");
		
		//新增时需要typeof判断
		document.getElementById("unid").innerHTML = typeof modal_relatedTarget.data("unid")=="undefined"?"":modal_relatedTarget.data("unid");
		document.getElementById("id").value = typeof modal_relatedTarget.data("id")=="undefined"?"":modal_relatedTarget.data("id");
		document.getElementById("name").value = typeof modal_relatedTarget.data("name")=="undefined"?"":modal_relatedTarget.data("name");
		document.getElementById("remark").value = typeof modal_relatedTarget.data("remark")=="undefined"?"":modal_relatedTarget.data("remark");
		document.getElementById("reserve").value = typeof modal_relatedTarget.data("reserve")=="undefined"?"":modal_relatedTarget.data("reserve");
		document.getElementById("reserve2").value = typeof modal_relatedTarget.data("reserve2")=="undefined"?"":modal_relatedTarget.data("reserve2");
		document.getElementById("reserve3").value = typeof modal_relatedTarget.data("reserve3")=="undefined"?"":modal_relatedTarget.data("reserve3");
		document.getElementById("reserve5").value = typeof modal_relatedTarget.data("reserve5")=="undefined"?"":modal_relatedTarget.data("reserve5");
	    //alert(JSON.stringify($('#myModal').data()));
	})
});

var btnSave = document.getElementById("btnSave");
btnSave.onclick = function() {
	
	var unid = document.getElementById("unid").innerHTML;
	var id = document.getElementById("id").value;
	var name =document.getElementById("name").value;
	var remark = document.getElementById("remark").value;
	var reserve = document.getElementById("reserve").value;
	var reserve2 = document.getElementById("reserve2").value;
	var reserve3 = document.getElementById("reserve3").value;
	var reserve5 = document.getElementById("reserve5").value;
		
	$.ajax({
		//默认值: true。如果需要发送同步请求，请将此选项设置为 false。注意，同步请求将锁住浏览器，用户其它操作必须等待请求完成才可以执行
		async : true,
		//默认值:"GET".请求方式 ("POST"或 "GET")，注意：其它 HTTP请求方法，如 PUT和 DELETE也可以使用，但仅部分浏览器支持
		type : 'POST',
		//默认值: "application/x-www-form-urlencoded"。发送信息至服务器时内容编码类型
		//默认值适合大多数情况。如果你明确指定$.ajax()的 content-type,那么它必定会发送给服务器（即使没有数据要发送）
		//contentType : "application/x-www-form-urlencoded",//application/json
		url : 'saveTask?unid='+unid+'&id='+id+'&name='+name+'&remark='+remark+'&reserve='+reserve+'&reserve2='+reserve2+'&reserve3='+reserve3+'&reserve5='+reserve5,
		//预期服务器返回的数据类型。如果不指定，jQuery将自动根据 HTTP包 MIME信息来智能判断
		dataType : 'json',
		success : function(data) {			
			
			if(!data.success){
				alert('操作失败!');
			}
			location.reload();//刷新页面
		},
		error : function(xhr, textStatus, errorThrown) {
			
			console.log("ajax请求失败,请求:saveTask,状态码:"+xhr.status +",状态说明:"+ textStatus+",xhr readyState:"+xhr.readyState);
		}
	});
};