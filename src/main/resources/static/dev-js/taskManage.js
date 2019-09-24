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
			        	
			        	return "<a href='#myModal' data-toggle='modal' data-title='修改' data-unid='"+value+"' data-id='"+row.ID+"' data-name='"+row.Name+"' data-remark='"+row.Remark+"' data-reserve='"+row.Reserve+"' data-reserve2='"+row.Reserve2+"' data-reserve3='"+row.Reserve3+"' data-reserve5='"+row.Reserve5+"' data-reserve6='"+row.Reserve6+"'>" + value + "</a>";
			        }
			    }, {
			        field: 'ID',
			        title: '编号'
			    }, {
			        field: 'Name',
			        title: '名称'
			    }, {
			        field: 'Remark',
			        title: '备注'
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
			    }, {
			        field: 'Reserve6',
			        title: '是否启用'
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
		document.getElementById("unid").innerHTML = modal_relatedTarget.data("unid");
		document.getElementById("id").value = modal_relatedTarget.data("id");
		document.getElementById("name").value = modal_relatedTarget.data("name");
		document.getElementById("remark").value = modal_relatedTarget.data("remark");
		document.getElementById("reserve").value = modal_relatedTarget.data("reserve");
		document.getElementById("reserve2").value = modal_relatedTarget.data("reserve2");
		document.getElementById("reserve3").value = modal_relatedTarget.data("reserve3");
		document.getElementById("reserve5").value = modal_relatedTarget.data("reserve5");
		document.getElementById("reserve6").value = modal_relatedTarget.data("reserve6");
	    //alert(JSON.stringify($('#myModal').data()));
	})
});

var btnSave = document.getElementById("btnSave");
btnSave.onclick = function() {
};